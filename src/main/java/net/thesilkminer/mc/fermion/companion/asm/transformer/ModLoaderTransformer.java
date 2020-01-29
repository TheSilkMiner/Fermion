package net.thesilkminer.mc.fermion.companion.asm.transformer;

import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.prefab.AbstractTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.BiFunction;

public final class ModLoaderTransformer extends AbstractTransformer {

    private static final class GatherAndInitializeModsVisitor extends MethodVisitor {

        private static final Marker MARKER = MarkerManager.getMarker("ModLoaderTransformer$GatherAndInitializeModsVisitor");

        private boolean hasVisitField;
        private boolean hasInvokeDynamic;
        private boolean hasInvokeVirtual;
        private boolean hasInjected;

        private GatherAndInitializeModsVisitor(final int version, @Nonnull final MethodVisitor parent) {
            super(version, parent);
        }

        @Override
        public void visitCode() {
            this.hasVisitField = false;
            this.hasInvokeDynamic = false;
            this.hasInvokeVirtual = false;
            this.hasInjected = false;
            super.visitCode();
        }

        @Override
        @SuppressWarnings("SpellCheckingInspection")
        public void visitFieldInsn(final int opcode, @Nonnull final String owner, @Nonnull final String name, @Nonnull final String descriptor) {
            super.visitFieldInsn(opcode, owner, name, descriptor);

            if (this.hasInjected) return;

            if (opcode == Opcodes.GETFIELD && "net/minecraftforge/fml/ModLoader".equals(owner)
                    && "statusConsumer".equals(name) && "Ljava/util/Optional;".equals(descriptor)) {
                LOGGER.info(MARKER, "Found GETFIELD instruction for statusConsumer");
                this.hasVisitField = true;
            }
        }

        @Override
        @SuppressWarnings("SpellCheckingInspection")
        public void visitInvokeDynamicInsn(@Nonnull final String name, @Nonnull final String descriptor,
                                           @Nonnull final Handle bootstrapMethodHandle, @Nonnull final Object... bootstrapMethodArguments) {
            super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);

            if (this.hasInjected) return;
            if (!this.hasVisitField) return;

            if ("accept".equals(name) && "()Ljava/util/function/Consumer;".equals(descriptor)) {
                LOGGER.info(MARKER, "Found INVOKEDYNAMIC on Consumer.accept");
                this.hasInvokeDynamic = true;
            }
        }

        @Override
        @SuppressWarnings("SpellCheckingInspection")
        public void visitMethodInsn(final int opcode, @Nonnull final String owner, @Nonnull final String name,
                                    @Nonnull final String descriptor, final boolean isInterface) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

            if (this.hasInjected) return;
            if (!this.hasVisitField) return;
            if (!this.hasInvokeDynamic) return;

            if (opcode == Opcodes.INVOKEVIRTUAL && "java/util/Optional".equals(owner)
                    && "ifPresent".equals(name) && "(Ljava/util/function/Consumer;)V".equals(descriptor)) {
                LOGGER.info(MARKER, "Found INVOKEVIRTUAL for Optional.ifPresent");
                this.hasInvokeVirtual = true;
            }
        }

        @Override
        @SuppressWarnings("SpellCheckingInspection")
        public void visitLabel(@Nonnull final Label label) {
            if (!this.hasInjected && this.hasVisitField && this.hasInvokeDynamic && this.hasInvokeVirtual) {
                LOGGER.info(MARKER, "Found all targets: injecting before new label");

                final Label l0 = new Label();
                super.visitLabel(l0);
                super.visitLineNumber(100 + 6 * 10, l0);
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraftforge/fml/ModLoader", "statusConsumer", "Ljava/util/Optional;");

                // Oh boy, time to ASM a lambda call...
                super.visitInvokeDynamicInsn(
                        "accept",
                        "()Ljava/util/function/Consumer;",
                        new Handle(Opcodes.H_INVOKESTATIC,
                                "java/lang/invoke/LambdaMetafactory",
                                "metafactory",
                                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;" +
                                        "Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)" +
                                        "Ljava/lang/invoke/CallSite;",
                                false),
                        Type.getType("(Ljava/lang/Object;)V"),
                        new Handle(Opcodes.H_INVOKESTATIC,
                                "net/minecraftforge/fml/ModLoader",
                                GENERATED_LAMBDA_METHOD_NAME,
                                "(Ljava/util/function/Consumer;)V",
                                false),
                        Type.getType("(Ljava/util/function/Consumer;)V")
                );
                // NEVER AGAIN!

                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Optional",
                        "ifPresent", "(Ljava/util/function/Consumer;)V", false);

                LOGGER.info(MARKER, "Code injection completed");

                this.hasInjected = true;
            }

            super.visitLabel(label);
        }
    }

    private static final Logger LOGGER = LogManager.getLogger("fermion.asm");
    private static final Marker MARKER = MarkerManager.getMarker("ModLoaderTransformer");

    private static final String GENERATED_LAMBDA_METHOD_NAME = "fermion$$lambda$$gatherAndInitializeMods$$generated$00_1_48_144";

    public ModLoaderTransformer() {
        super(
                TransformerData.Builder.create()
                        .setOwningPluginId("fermion.asm")
                        .setName("mod_loader_transformer")
                        .setDescription("Transforms the Mod Loader class to add some more messages to the loading screen")
                        .build(),
                ClassDescriptor.of("net.minecraftforge.fml.ModLoader")
        );
    }

    @Nonnull
    @Override
    public BiFunction<Integer, ClassVisitor, ClassVisitor> getClassVisitorCreator() {
        return (v, cw) -> new ClassVisitor(v, cw) {
            @Override
            public MethodVisitor visitMethod(int access, @Nonnull final String name, @Nonnull final String descriptor,
                                             @Nullable final String signature, @Nullable final String[] exceptions) {

                final MethodVisitor parent = super.visitMethod(access, name, descriptor, signature, exceptions);

                if (access == Opcodes.ACC_PUBLIC
                        && "gatherAndInitializeMods".equals(name)
                        && "(Ljava/lang/Runnable;)V".equals(descriptor)
                        && Objects.isNull(signature)
                        && Objects.isNull(exceptions)) {

                    LOGGER.info(MARKER, "Found target method 'gatherAndInitializeMods': constructing new patcher");
                    return new GatherAndInitializeModsVisitor(v, parent);
                }

                return parent;
            }

            @Override
            @SuppressWarnings("SpellCheckingInspection")
            public void visitEnd() {
                LOGGER.info(MARKER, "Reached end of class: generating lambda method");
                final MethodVisitor mv = super.visitMethod(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_SYNTHETIC,
                        GENERATED_LAMBDA_METHOD_NAME, "(Ljava/util/function/Consumer;)V", null, null);
                mv.visitCode();
                final Label l0 = new Label();
                mv.visitLabel(l0);
                mv.visitLineNumber(100 + 6 * 10, l0);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitLdcInsn("Injecting Fermion Launch Plugins into list");
                mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/function/Consumer", "accept",
                        "(Ljava/lang/Object;)V", true);
                mv.visitInsn(Opcodes.RETURN);
                final Label l1 = new Label();
                mv.visitLabel(l1);
                mv.visitLineNumber(100 + 6 * 10, l1);
                mv.visitLocalVariable("c", "Ljava/util/function/Consumer;", null, l0, l1, 0);
                mv.visitMaxs(2, 1);
                mv.visitEnd();
                LOGGER.info(MARKER, "Lambda method generated successfully");

                super.visitEnd();
            }
        };
    }
}
