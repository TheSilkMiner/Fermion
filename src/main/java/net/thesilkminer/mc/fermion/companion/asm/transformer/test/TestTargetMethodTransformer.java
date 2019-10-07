/*
 * Copyright (C) 2019  TheSilkMiner
 *
 * This file is part of Fermion.
 *
 * Fermion is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Fermion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Fermion.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact information:
 * E-mail: thesilkminer <at> outlook <dot> com
 */

package net.thesilkminer.mc.fermion.companion.asm.transformer.test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.descriptor.MethodDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.prefab.transformer.TargetMethodTransformer;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.BiFunction;

public final class TestTargetMethodTransformer extends TargetMethodTransformer {

    private static final Logger LOGGER = LogManager.getLogger("TestTargetMethodTransformer");

    private static final MethodDescriptor SETUP_METHOD_DESCRIPTOR = MethodDescriptor.of("onPreInitialization",
            ImmutableList.of(ClassDescriptor.of("net.minecraftforge.fml.common.event.FMLPreInitializationEvent")),
            ClassDescriptor.of(void.class));
    private static final MethodDescriptor CONSTRUCTOR = MethodDescriptor.of("<init>",
            ImmutableList.of(),
            ClassDescriptor.of(void.class));

    public TestTargetMethodTransformer() {
        super(
                TransformerData.Builder.create()
                        .setOwningPluginId("fermion.asm")
                        .setName("test_target_method_transformer")
                        .setDescription("This is a test for the Target Method Transformer prefab")
                        .setDisabledByDefault()
                        .build(),
                ClassDescriptor.of("net.thesilkminer.mc.fermion.Fermion"),
                SETUP_METHOD_DESCRIPTOR, CONSTRUCTOR
        );
    }

    @Nonnull
    @Override
    protected Map<MethodDescriptor, BiFunction<MethodDescriptor, Pair<Integer, MethodVisitor>, MethodVisitor>> getMethodVisitorCreators() {
        return ImmutableMap.of(
                SETUP_METHOD_DESCRIPTOR, (desc, pair) -> new MethodVisitor(pair.getLeft(), pair.getRight()) {
                    @Override
                    public void visitLdcInsn(@Nonnull final Object value) {
                        if (value instanceof String) {
                            final String ldcConstant = (String) value;
                            if ("FMLPreInitializationEvent".equalsIgnoreCase(ldcConstant)) {
                                super.visitLdcInsn(ldcConstant + " (Hey there, TestTargetMethodTransformer here)");
                                return;
                            }
                        }
                        super.visitLdcInsn(value);
                    }
                },
                CONSTRUCTOR, (desc, pair) -> new MethodVisitor(pair.getLeft(), pair.getRight()) {
                    @Override
                    public void visitFieldInsn(final int opcode, @Nonnull final String owner, @Nonnull final String name,
                                               @Nonnull final String descriptor) {
                        final String newName = (opcode == Opcodes.GETSTATIC && "LOGGER".equals(name)) ? "TRANSFORMER_LOGGER" : name;
                        LOGGER.info("Visiting field with name '" + name + "' became '" + newName + "'");
                        super.visitFieldInsn(opcode, owner, newName, descriptor);
                    }
                }
        );
    }
}
