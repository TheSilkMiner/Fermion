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
