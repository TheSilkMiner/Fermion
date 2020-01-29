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

package net.thesilkminer.mc.fermion.test.asm.transformer;

import com.google.common.collect.ImmutableList;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.descriptor.MethodDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.prefab.transformer.RuntimeMethodAccessTransformer;

import javax.annotation.Nonnull;

public final class TestRuntimeMethodAccessTransformer extends RuntimeMethodAccessTransformer {

    public TestRuntimeMethodAccessTransformer(@Nonnull final LaunchPlugin owner) {
        super(
                TransformerData.Builder.create()
                        .setOwningPlugin(owner)
                        .setName("test_runtime_method_at")
                        .setDescription("This is a test for the RuntimeMethodAT")
                        .setDisabledByDefault()
                        .build(),
                TargetDescriptor.Builder.create()
                        .setTargetMethod(
                                ClassDescriptor.of("net.thesilkminer.mc.fermion.OtherClass"),
                                MethodDescriptor.of(
                                        "print",
                                        ImmutableList.of(ClassDescriptor.of(String.class)),
                                        ClassDescriptor.of(String.class)
                                )
                        )
                        .setAccessorMethod(
                                ClassDescriptor.of("net.thesilkminer.mc.fermion.hook.OtherClassHook"),
                                MethodDescriptor.of(
                                        "print",
                                        ImmutableList.of(
                                                ClassDescriptor.of("net.thesilkminer.mc.fermion.OtherClass"),
                                                ClassDescriptor.of(String.class)
                                        ),
                                        ClassDescriptor.of(String.class)
                                )
                        )
                        .build(),
                TargetDescriptor.Builder.create()
                        .setTargetMethod(
                                ClassDescriptor.of("net.thesilkminer.mc.fermion.OtherClass"),
                                MethodDescriptor.of(
                                        "getId",
                                        ImmutableList.of(ClassDescriptor.of(Object.class)),
                                        ClassDescriptor.of(int.class)
                                ),
                                true
                        )
                        .setAccessorMethod(
                                ClassDescriptor.of("net.thesilkminer.mc.fermion.hook.OtherClassHook"),
                                MethodDescriptor.of(
                                        "getIdThroughMethod",
                                        ImmutableList.of(ClassDescriptor.of(Object.class)),
                                        ClassDescriptor.of(int.class)
                                )
                        )
                        .build()
        );
    }
}
