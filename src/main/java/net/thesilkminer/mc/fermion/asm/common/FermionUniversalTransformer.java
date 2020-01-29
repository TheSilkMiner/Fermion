/*
 * Copyright (C) 2020  TheSilkMiner
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

package net.thesilkminer.mc.fermion.asm.common;

import com.google.common.collect.ImmutableSet;
import net.thesilkminer.mc.fermion.asm.api.configuration.TransformerConfiguration;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.Transformer;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.common.utility.Log;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

final class FermionUniversalTransformer implements Transformer {
    static final String TRANSFORMER_NAME = "fermion.asm.service:universal";
    private static final Log L = Log.of(TRANSFORMER_NAME);

    @Nonnull
    @Override
    public TransformerData getData() {
        return TransformerData.Builder.create()
                .setOwningPluginId("fermion.asm.service")
                .setName("universal")
                .setDescription("The universal plugin")
                .build();
    }

    @Nonnull
    @Override
    public Set<ClassDescriptor> getClassesToTransform() {
        return ImmutableSet.of();
    }

    @Nonnull
    @Override
    public Supplier<TransformerConfiguration> provideConfiguration() {
        return () -> TransformerConfiguration.Builder.create().build();
    }

    @Nonnull
    @Override
    public BiFunction<Integer, ClassVisitor, ClassVisitor> getClassVisitorCreator() {
        return (v, w) -> new ClassVisitor(v, w) {
            @Override
            public void visitEnd() {
                final FieldVisitor fv = super.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC,
                        "_re_syst_patch_successful", "Z", null, null);
                fv.visitEnd();
                L.i("Successfully injected field into class");
                super.visitEnd();
            }
        };
    }
}
