package net.thesilkminer.mc.fermion.test.asm.transformer;

import com.google.common.collect.ImmutableList;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.descriptor.FieldDescriptor;
import net.thesilkminer.mc.fermion.asm.api.descriptor.MethodDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.prefab.transformer.RuntimeFieldAccessTransformer;

import javax.annotation.Nonnull;

public final class TestRuntimeFieldAccessTransformer extends RuntimeFieldAccessTransformer {

    public TestRuntimeFieldAccessTransformer(@Nonnull final LaunchPlugin owner) {
        super(
                TransformerData.Builder.create()
                        .setOwningPlugin(owner)
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
