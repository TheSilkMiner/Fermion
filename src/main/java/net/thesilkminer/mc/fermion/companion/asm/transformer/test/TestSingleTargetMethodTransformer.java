package net.thesilkminer.mc.fermion.companion.asm.transformer.test;

import com.google.common.collect.ImmutableList;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.descriptor.MethodDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.prefab.transformer.SingleTargetMethodTransformer;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

public final class TestSingleTargetMethodTransformer extends SingleTargetMethodTransformer {

    public TestSingleTargetMethodTransformer() {
        super(
                TransformerData.Builder.create()
                        .setOwningPluginId("fermion.asm")
                        .setName("test_single_target_method_transformer")
                        .setDescription("This tests the SingleTargetMethodTransformer prefab")
                        .setDisabledByDefault()
                        .build(),
                ClassDescriptor.of("net.thesilkminer.mc.fermion.Fermion"),
                MethodDescriptor.of("onPostInitialization",
                        ImmutableList.of(ClassDescriptor.of("net.minecraftforge.fml.common.event.FMLPostInitializationEvent")),
                        ClassDescriptor.of(void.class))
        );
    }

    @Nonnull
    @Override
    protected BiFunction<Integer, MethodVisitor, MethodVisitor> getMethodVisitorCreator() {
        return (v, mv) -> new MethodVisitor(v, mv) {
            @Override
            @SuppressWarnings("SpellCheckingInspection")
            public void visitCode() {
                super.visitCode();
                final Label l0 = new Label();
                super.visitLabel(l0);
                super.visitLineNumber(7 * 10 + 6, l0);
                super.visitFieldInsn(Opcodes.GETSTATIC, "net/thesilkminer/mc/fermion/Fermion", "LOGGER",
                        "Lorg/apache/logging/log4j/Logger;");
                super.visitVarInsn(Opcodes.ALOAD, 1);
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraftforge/fml/common/event/FMLPostInitializationEvent",
                        "getModState", "()Lnet/minecraftforge/fml/common/LoaderState$ModState;", false);
                super.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/apache/logging/log4j/Logger", "debug",
                        "(Ljava/lang/Object;)V", true);
            }

            @Override
            public void visitMaxs(final int maxStack, final int maxLocals) {
                super.visitMaxs(3, 2);
            }
        };
    }
}
