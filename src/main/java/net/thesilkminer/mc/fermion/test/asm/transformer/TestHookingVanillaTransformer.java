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

package net.thesilkminer.mc.fermion.test.asm.transformer;

import com.google.common.collect.ImmutableList;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;
import net.thesilkminer.mc.fermion.asm.api.MappingUtilities;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.descriptor.MethodDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.prefab.transformer.SingleTargetMethodTransformer;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

public final class TestHookingVanillaTransformer extends SingleTargetMethodTransformer {

    public TestHookingVanillaTransformer(@Nonnull final LaunchPlugin owner) {
        super(
                TransformerData.Builder.create()
                        .setOwningPlugin(owner)
                        .setName("test_hooking_vanilla_transformer")
                        .setDescription("This adds a hook in PotionEffect that gets called on loading to test whether transforming Vanilla works")
                        .setDisabledByDefault()
                        .build(),
                ClassDescriptor.of("net.minecraft.potion.PotionEffect"),
                MethodDescriptor.of("<clinit>",
                        ImmutableList.of(),
                        ClassDescriptor.of(void.class))
        );
    }

    @Nonnull
    @Override
    protected BiFunction<Integer, MethodVisitor, MethodVisitor> getMethodVisitorCreator() {
        return (v, mv) -> new MethodVisitor(v, mv) {
            @Override
            @SuppressWarnings("SpellCheckingInspection")
            public void visitInsn(final int opcode) {
                if (opcode != Opcodes.RETURN) {
                    super.visitInsn(opcode);
                    return;
                }

                final Label l0 = new Label();
                super.visitLabel(l0);
                super.visitLineNumber(10 + 3, l0);
                super.visitFieldInsn(Opcodes.GETSTATIC,
                        "net/minecraft/potion/PotionEffect",
                        MappingUtilities.INSTANCE.mapField("field_180155_a"),
                        "Lorg/apache/logging/log4j/Logger;");
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "net/thesilkminer/mc/fermion/test/hook/PotionEffectHook",
                        "logTest",
                        "(Lorg/apache/logging/log4j/Logger;)V",
                        false);
                final Label l1 = new Label();
                super.visitLabel(l1);
                super.visitLineNumber(10 + 4, l1);
                super.visitInsn(opcode);
            }
        };
    }
}
