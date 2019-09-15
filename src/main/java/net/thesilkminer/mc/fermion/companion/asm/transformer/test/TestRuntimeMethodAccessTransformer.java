package net.thesilkminer.mc.fermion.companion.asm.transformer.test;

import com.google.common.collect.ImmutableList;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.descriptor.MethodDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.prefab.transformer.RuntimeMethodAccessTransformer;

public final class TestRuntimeMethodAccessTransformer extends RuntimeMethodAccessTransformer {

    public TestRuntimeMethodAccessTransformer() {
        super(
                TransformerData.Builder.create()
                        .setOwningPluginId("fermion.asm")
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
