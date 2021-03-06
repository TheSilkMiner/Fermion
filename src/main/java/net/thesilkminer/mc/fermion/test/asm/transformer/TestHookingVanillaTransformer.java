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
                        .setDescription("This adds a hook in EffectInstance that gets called on loading to test whether transforming Vanilla works")
                        .setDisabledByDefault()
                        .build(),
                ClassDescriptor.of("net.minecraft.potion.EffectInstance"),
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
                super.visitFieldInsn(Opcodes.GETSTATIC, "net/minecraft/potion/EffectInstance", MappingUtilities.INSTANCE.mapField("field_180155_a"), "Lorg/apache/logging/log4j/Logger;");
                super.visitMethodInsn(Opcodes.INVOKESTATIC, "net/thesilkminer/mc/fermion/test/hook/EffectInstanceHook", "logTest", "(Lorg/apache/logging/log4j/Logger;)V", false);
                final Label l1 = new Label();
                super.visitLabel(l1);
                super.visitLineNumber(10 + 4, l1);
                super.visitInsn(opcode);
            }
        };
    }
}
