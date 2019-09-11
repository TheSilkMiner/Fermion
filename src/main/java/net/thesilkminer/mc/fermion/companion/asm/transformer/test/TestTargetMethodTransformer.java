package net.thesilkminer.mc.fermion.companion.asm.transformer.test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.descriptor.MethodDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.prefab.transformer.TargetMethodTransformer;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.MethodVisitor;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.BiFunction;

public final class TestTargetMethodTransformer extends TargetMethodTransformer {

    private static final MethodDescriptor SETUP_METHOD_DESCRIPTOR = MethodDescriptor.of("setup",
            ImmutableList.of(ClassDescriptor.of("net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent")),
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
                SETUP_METHOD_DESCRIPTOR
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
                            if ("FMLCommonSetupEvent".equalsIgnoreCase(ldcConstant)) {
                                super.visitLdcInsn(ldcConstant + " (Hey there, TestTargetMethodTransformer here)");
                                return;
                            }
                        }
                        super.visitLdcInsn(value);
                    }
                }
        );
    }
}
