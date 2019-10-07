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

package net.thesilkminer.mc.fermion.companion.asm.transformer;

import com.google.common.collect.ImmutableList;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.descriptor.MethodDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.prefab.transformer.SingleTargetMethodTransformer;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiFunction;

public final class TransformingUtilitiesTransformer extends SingleTargetMethodTransformer {

    public TransformingUtilitiesTransformer() {
        super(
                TransformerData.Builder.create()
                        .setOwningPluginId("fermion.asm")
                        .setName("transforming_utilities")
                        .setDescription("This is a fundamental part of the API: do not disable")
                        .build(),
                ClassDescriptor.of("net.thesilkminer.mc.fermion.api.TransformingUtilities"),
                MethodDescriptor.of("transformedFieldName", ImmutableList.of(), ClassDescriptor.of(String.class))
        );
    }

    @Nonnull
    @Override
    protected BiFunction<Integer, MethodVisitor, MethodVisitor> getMethodVisitorCreator() {
        return (v, parent) -> new MethodVisitor(v, null) {
            @Override
            public void visitCode() {
                parent.visitCode();
                final Label l0 = new Label();
                parent.visitLabel(l0);
                parent.visitLineNumber(10 + 3, l0);
                parent.visitLdcInsn("_re_syst_patch_successful");
                parent.visitInsn(Opcodes.ARETURN);
                parent.visitMaxs(1, 0);
                parent.visitEnd();
            }

            @Nullable
            @Override
            public AnnotationVisitor visitAnnotation(@Nonnull final String descriptor, final boolean visible) {
                return parent.visitAnnotation(descriptor, visible);
            }
        };
    }
}
