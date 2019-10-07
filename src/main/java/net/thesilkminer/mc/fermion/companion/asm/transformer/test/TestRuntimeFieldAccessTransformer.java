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
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.descriptor.FieldDescriptor;
import net.thesilkminer.mc.fermion.asm.api.descriptor.MethodDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.prefab.transformer.RuntimeFieldAccessTransformer;

public final class TestRuntimeFieldAccessTransformer extends RuntimeFieldAccessTransformer {

    public TestRuntimeFieldAccessTransformer() {
        super(
                TransformerData.Builder.create()
                        .setOwningPluginId("fermion.asm")
                        .setName("test_runtime_field_at")
                        .setDescription("This is a test for the RuntimeFieldAT")
                        .setDisabledByDefault()
                        .build(),
                TargetDescriptor.Builder.create()
                        .setTargetField(
                                ClassDescriptor.of("net.thesilkminer.mc.fermion.OtherClass"),
                                FieldDescriptor.of("parameter", ClassDescriptor.of(String.class))
                        )
                        .setAccessorMethod(
                                ClassDescriptor.of("net.thesilkminer.mc.fermion.hook.OtherClassHook"),
                                MethodDescriptor.of(
                                        "getParameter",
                                        ImmutableList.of(ClassDescriptor.of("net.thesilkminer.mc.fermion.OtherClass")),
                                        ClassDescriptor.of(String.class))
                        )
                        .build(),
                TargetDescriptor.Builder.create()
                        .setTargetField(
                                ClassDescriptor.of("net.thesilkminer.mc.fermion.OtherClass"),
                                FieldDescriptor.of("ID", ClassDescriptor.of(int.class), true)
                        )
                        .setAccessorMethod(
                                ClassDescriptor.of("net.thesilkminer.mc.fermion.hook.OtherClassHook"),
                                MethodDescriptor.of(
                                        "getId",
                                        ImmutableList.of(),
                                        ClassDescriptor.of(int.class)
                                )
                        )
                        .build()
        );
    }
}
