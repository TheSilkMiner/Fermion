package net.thesilkminer.mc.fermion.companion.asm.transformer;

import com.google.common.collect.Lists;
import net.thesilkminer.mc.fermion.asm.api.PluginMetadata;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerRegistry;
import net.thesilkminer.mc.fermion.asm.prefab.AbstractTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public final class ModListTransformer extends AbstractTransformer {

    private static final class ConstructorVisitor extends MethodVisitor {
        private boolean hasVisitedThreadCountGetter;

        private ConstructorVisitor(final int version, @Nonnull final MethodVisitor parent) {
            super(version, parent);
        }

        @Override
        @SuppressWarnings("SpellCheckingInspection")
        public void visitMethodInsn(final int opcode, @Nonnull final String owner, @Nonnull final String name,
                                    @Nonnull final String descriptor, final boolean isInterface) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

            if (opcode == Opcodes.INVOKESTATIC && "net/minecraftforge/fml/loading/FMLConfig".equals(owner)
                    && "loadingThreadCount".equals(name) && "()I".equals(descriptor) && !isInterface) {

                LOGGER.info(MARKER, "Found INVOKESTATIC for FMLConfig.loadingThreadCount: marked it as a found");
                this.hasVisitedThreadCountGetter = true;
            }
        }

        @Override
        @SuppressWarnings("SpellCheckingInspection")
        public void visitFieldInsn(final int opcode, @Nonnull final String owner, @Nonnull final String name, @Nonnull final String descriptor) {
            if (this.hasVisitedThreadCountGetter && opcode == Opcodes.GETSTATIC
                    && "net/minecraftforge/fml/ModList".equals(owner) && "LOGGER".equals(name)
                    && "Lorg/apache/logging/log4j/Logger;".equals(descriptor)) {

                LOGGER.info(MARKER, "Found GETSTATIC for LOGGER after FMLConfig.loadingThreadCount: performing hook injection");
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraftforge/fml/ModList", "modFiles", "Ljava/util/List;");
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraftforge/fml/ModList", "sortedList", "Ljava/util/List;");
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraftforge/fml/ModList", "fileById", "Ljava/util/Map;");
                super.visitMethodInsn(Opcodes.INVOKESTATIC, "net/minecraftforge/fml/ModList", GENERATED_GET_DATA_LIST_METHOD_NAME,
                        "()Ljava/util/List;", false);
                super.visitMethodInsn(Opcodes.INVOKESTATIC, "net/thesilkminer/mc/fermion/companion/hook/ModListHook",
                        "injectFermionLaunchPlugins", "(Ljava/util/List;Ljava/util/List;Ljava/util/Map;Ljava/util/List;)V", false);
                final Label l0 = new Label();
                super.visitLabel(l0);
                super.visitLineNumber(7 * 10 + 1, l0);
                LOGGER.info(MARKER, "Hook injection completed: disabling future ones");
                this.hasVisitedThreadCountGetter = false;
            }

            super.visitFieldInsn(opcode, owner, name, descriptor);
        }
    }

    private static final class SizeVisitor extends MethodVisitor {
        private SizeVisitor(final int version, @Nonnull final MethodVisitor parent) {
            super(version, parent);
        }

        @Override
        @SuppressWarnings("SpellCheckingInspection")
        public void visitCode() {
            super.visitCode();

            LOGGER.info(MARKER, "Patching size to return our own value");
            final Label l0 = new Label();
            super.visitLabel(l0);
            super.visitLineNumber(100 + 8 * 10 + 1, l0);
            super.visitVarInsn(Opcodes.ALOAD, 0);
            super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraftforge/fml/ModList", "mods", "Ljava/util/List;");
            super.visitMethodInsn(Opcodes.INVOKESTATIC, "net/minecraftforge/fml/ModList", GENERATED_GET_DATA_LIST_METHOD_NAME,
                    "()Ljava/util/List;", false);
            super.visitMethodInsn(Opcodes.INVOKESTATIC, "net/thesilkminer/mc/fermion/companion/hook/ModListHook",
                    "injectFermionContainersForSize", "(Ljava/util/List;Ljava/util/List;)I", false);
            super.visitInsn(Opcodes.IRETURN);
            final Label l1 = new Label();
            super.visitLabel(l1);
            super.visitLocalVariable("this", "Lnet/minecraftforge/fml/ModList;", null, l0, l1, 0);
            super.visitMaxs(1, 1);
            super.visitEnd();
            LOGGER.info(MARKER, "Method overwritten");
        }

        @Override public void visitLabel(@Nonnull final Label label) {}
        @Override public void visitLineNumber(final int line, @Nonnull final Label start) {}
        @Override public void visitVarInsn(final int opcode, final int var) {}
        @Override public void visitFieldInsn(final int opcode, @Nonnull final String owner, @Nonnull final String name, @Nonnull final String descriptor) {}
        @Override public void visitMethodInsn(final int opcode, @Nonnull final String owner, @Nonnull final String name, @Nonnull final String descriptor, final boolean isInterface) {}
        @Override public void visitInsn(final int opcode) {}
        @Override public void visitLocalVariable(@Nonnull final String name, @Nonnull final String descriptor, @Nullable final String signature, @Nonnull final Label start, @Nonnull final Label end, final int index) {}
        @Override public void visitMaxs(final int maxStack, final int maxLocals) {}
        @Override public void visitEnd() {}
    }

    private static final Logger LOGGER = LogManager.getLogger("fermion.asm");
    private static final Marker MARKER = MarkerManager.getMarker("ModListTransformer");
    private static final String GENERATED_GET_DATA_LIST_METHOD_NAME = "fermion$$injected$$getDataList$$generated$$00_1_01_144";

    public static List<PluginMetadata> pluginMetadataList = Lists.newArrayList();
    public static List<String> transformers = Lists.newArrayList();
    public static TransformerRegistry registry = null;

    public ModListTransformer() {
        super(
                TransformerData.Builder.create()
                        .setOwningPluginId("fermion.asm")
                        .setName("mod_list_transformer")
                        .setDescription("Transforms the ModList class to allow injection into the loading process of our own LaunchPlugins")
                        .build(),
                ClassDescriptor.of("net.minecraftforge.fml.ModList")
        );
    }

    @Nonnull
    @Override
    public BiFunction<Integer, ClassVisitor, ClassVisitor> getClassVisitorCreator() {
        return (v, cw) -> new ClassVisitor(v, cw) {

            @Nullable
            @Override
            @SuppressWarnings("SpellCheckingInspection")
            public MethodVisitor visitMethod(final int access, @Nonnull final String name, @Nonnull final String descriptor,
                                             @Nullable final String signature, @Nullable final String[] exceptions) {
                final MethodVisitor parent = super.visitMethod(access, name, descriptor, signature, exceptions);

                if (access == Opcodes.ACC_PRIVATE && "<init>".equals(name) && "(Ljava/util/List;Ljava/util/List;)V".equals(descriptor)) {
                    LOGGER.info(MARKER, "Found constructor: begin patching");
                    return new ConstructorVisitor(v, parent);
                }

                if (access == Opcodes.ACC_PUBLIC && "size".equals(name) && "()I".equals(descriptor)) {
                    LOGGER.info(MARKER, "Found size: begin patching");
                    return new SizeVisitor(v, parent);
                }

                return parent;
            }

            @Override
            @SuppressWarnings("SpellCheckingInspection")
            public void visitEnd() {
                LOGGER.info(MARKER, "Reached end of class: injecting data getter method");

                final MethodVisitor mv = super.visitMethod(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, GENERATED_GET_DATA_LIST_METHOD_NAME,
                        "()Ljava/util/List;", "()Ljava/util/List<Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata;>;", null);

                mv.visitCode();
                final Label l0 = new Label();
                mv.visitLabel(l0);
                mv.visitLineNumber(1000, l0);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/google/common/collect/Lists", "newArrayList",
                        "()Ljava/util/ArrayList;", false);
                mv.visitVarInsn(Opcodes.ASTORE, 0);
                final Label l1 = new Label();
                mv.visitLabel(l1);
                mv.visitLineNumber(1000 + 1, l1);

                this.generateDataList(mv);

                final Label l2 = new Label();
                mv.visitLabel(l2);
                mv.visitLineNumber(1000 + 2, l2);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitInsn(Opcodes.ARETURN);
                final Label l3 = new Label();
                mv.visitLabel(l3);
                mv.visitLineNumber(1000 + 3, l3);
                mv.visitLocalVariable("dataList", "Ljava/util/List;",
                        "Ljava/util/List<Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata;>;", l0, l3, 0);
                mv.visitMaxs(3, 1);
                mv.visitEnd();

                super.visitEnd();
            }

            private void generateDataList(@Nonnull final MethodVisitor visitor) {
                LOGGER.debug(MARKER, "Attempting to generate list for PluginMetadata: " + pluginMetadataList);
                pluginMetadataList.forEach(it -> this.generateDataListFor(visitor, it));
            }

            @SuppressWarnings("SpellCheckingInspection")
            private void generateDataListFor(@Nonnull final MethodVisitor mv, @Nonnull final PluginMetadata metadata) {
                LOGGER.debug(MARKER, "Generating entry for PluginMetadata " + metadata);

                mv.visitVarInsn(Opcodes.ALOAD, 0);

                mv.visitLdcInsn(metadata.getId());
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "net/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder",
                        "create", "(Ljava/lang/String;)Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder;", false);

                mv.visitLdcInsn(metadata.getName());
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder",
                        "setName", "(Ljava/lang/String;)Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder;", false);

                final StringBuilder descBuilder = new StringBuilder();
                if (metadata.getDescription() != null) descBuilder.append(metadata.getDescription());
                this.appendTransformersForMetadata(metadata, descBuilder);

                LOGGER.debug(MARKER, "Successfully built advanced description '" + descBuilder + "'");

                mv.visitLdcInsn(descBuilder.toString());
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder",
                            "setDescription", "(Ljava/lang/String;)Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder;", false);

                if (metadata.getCredits() != null) {
                    mv.visitLdcInsn(metadata.getCredits());
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder",
                            "setCredits", "(Ljava/lang/String;)Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder;", false);
                }

                metadata.getAuthors().forEach(author -> {
                    mv.visitLdcInsn(author.getName());
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder",
                            "addAuthor", "(Ljava/lang/String;)Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder;", false);
                });

                if (metadata.getUrl() != null) {
                    mv.visitLdcInsn(metadata.getUrl());
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder",
                            "setDisplayUrl", "(Ljava/lang/String;)Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder;", false);
                }

                mv.visitLdcInsn(metadata.getVersion().toString());
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder",
                        "setVersion", "(Ljava/lang/String;)Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder;", false);

                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder",
                        "build", "()Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata;", false);

                mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
                mv.visitInsn(Opcodes.POP);

                LOGGER.debug(MARKER, "Entry generated successfully");
            }

            private void appendTransformersForMetadata(@Nonnull final PluginMetadata metadata, @Nonnull final StringBuilder builder) {
                final List<String> pluginTransformers = transformers.stream()
                        .filter(it -> it.startsWith(metadata.getId() + ":"))
                        .collect(Collectors.toList());
                final List<String> enabled = pluginTransformers.stream()
                        .filter(it -> registry.isTransformerEnabled(it))
                        .collect(Collectors.toList());
                final List<String> disabled = pluginTransformers.stream()
                        .filter(it -> !enabled.contains(it))
                        .collect(Collectors.toList());

                if (!enabled.isEmpty()) {
                    builder.append("\n\n\nEnabled transformers:\n");
                    enabled.forEach(it -> {
                        builder.append("- ");
                        builder.append(it);
                        builder.append('\n');
                    });
                }

                if (!disabled.isEmpty()) {
                    builder.append("\n\n\nDisabled transformers:\n");
                    disabled.forEach(it -> {
                        builder.append("- ");
                        builder.append(it);
                        builder.append('\n');
                    });
                }
            }
        };
    }
}
