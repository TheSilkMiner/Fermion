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

package net.thesilkminer.mc.fermion.companion.asm.transformer;

import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;
import net.thesilkminer.mc.fermion.asm.api.PluginMetadata;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerRegistry;
import net.thesilkminer.mc.fermion.asm.prefab.AbstractTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public final class ModDiscovererTransformer extends AbstractTransformer {

    @SuppressWarnings("SpellCheckingInspection")
    private static final class ModIdentifierMethodVisitor extends MethodVisitor {

        //    TRYCATCHBLOCK L0 L1 L2 net/minecraftforge/fml/common/LoaderException
        //   L3
        //    LINENUMBER 87 L3
        // <<< OVERWRITE BEGIN
        //    INVOKESTATIC com/google/common/collect/Lists.newArrayList ()Ljava/util/ArrayList;
        // === OVERWRITE CODE
        //    INVOKEVIRTUAL net/minecraftforge/fml/common/discovery/ModDiscoverer.<fermion-inject:injectFermionContainers> ()Ljava/util/ArrayList;
        // >>> OVERWRITE END
        //   L4
        //    LINENUMBER 89 L4
        //    ALOAD 0
        //    GETFIELD net/minecraftforge/fml/common/discovery/ModDiscoverer.candidates : Ljava/util/List;
        //    INVOKEINTERFACE java/util/List.iterator ()Ljava/util/Iterator;
        //    ASTORE 2

        private ModIdentifierMethodVisitor(final int version, @Nonnull final MethodVisitor visitor) {
            super(version, visitor);
        }

        @Override
        public void visitCode() {
            super.visitCode();
        }

        @Override
        public void visitMethodInsn(final int opcode, @Nonnull final String owner, @Nonnull final String name,
                                    @Nonnull final String desc, final boolean itf) {

            if (opcode == Opcodes.INVOKESTATIC && "newArrayList".equals(name) && "com/google/common/collect/Lists".equals(owner)) {
                LOGGER.info("Found 'INVOKESTATIC com/google/common/collect/Lists.newArrayList ()Ljava/util/ArrayList;': replacing");
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        THIS_CLASS_NAME,
                        INJECT_CONTAINERS_METHOD_NAME,
                        "()Ljava/util/ArrayList;",
                        false);
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static final class InjectFermionContainersVisitor extends MethodVisitor {

        //  // access flags 0x1
        //  // signature ()Ljava/util/ArrayList<Lnet/minecraftforge/fml/common/ModContainer;>;
        //  // declaration: java.util.ArrayList<net.minecraftforge.fml.common.ModContainer> injectFermionContainers()
        //  public injectFermionContainers()Ljava/util/ArrayList;
        //  @Ljavax/annotation/Nonnull;()
        //   L0
        //    LINENUMBER 23 L0
        //    GETSTATIC net/minecraftforge/fml/common/discovery/ModDiscoverer.<fermion-inject:LOGGER> : Lorg/apache/logging/log4j/Logger;
        //    LDC "Attempting to inject ModContainers into ModDiscoverer"
        //    INVOKEINTERFACE org/apache/logging/log4j/Logger.info (Ljava/lang/String;)V
        //   L1
        //    LINENUMBER 24 L1
        //    INVOKESTATIC com/google/common/collect/Lists.newArrayList ()Ljava/util/ArrayList;
        //    ASTORE 1
        //   L2
        //    LINENUMBER 25 L2
        //    ALOAD 0
        //    INVOKESPECIAL net/minecraftforge/fml/common/discovery/ModDiscoverer.<fermion-inject:getData> ()Ljava/util/List;
        //    ASTORE 2
        //   L3
        //    LINENUMBER 26 L3
        //    ALOAD 2
        //    INVOKEINTERFACE java/util/List.iterator ()Ljava/util/Iterator;
        //    ASTORE 3
        //   L4
        //   FRAME APPEND [java/util/ArrayList java/util/List java/util/Iterator]
        //    ALOAD 3
        //    INVOKEINTERFACE java/util/Iterator.hasNext ()Z
        //    IFEQ L5
        //    ALOAD 3
        //    INVOKEINTERFACE java/util/Iterator.next ()Ljava/lang/Object;
        //    CHECKCAST org/apache/commons/lang3/tuple/Pair
        //    ASTORE 4
        //   L6
        //    LINENUMBER 27 L6
        //    NEW net/thesilkminer/mc/fermion/companion/LaunchPluginContainer
        //    DUP
        //    ALOAD 4
        //    INVOKEVIRTUAL org/apache/commons/lang3/tuple/Pair.getKey ()Ljava/lang/Object;
        //    CHECKCAST net/thesilkminer/mc/fermion/asm/api/PluginMetadata
        //    ALOAD 4
        //    INVOKEVIRTUAL org/apache/commons/lang3/tuple/Pair.getValue ()Ljava/lang/Object;
        //    CHECKCAST java/util/List
        //    INVOKESPECIAL net/thesilkminer/mc/fermion/companion/LaunchPluginContainer.<init> (Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata;Ljava/util/List;)V
        //    ASTORE 5
        //   L7
        //    LINENUMBER 28 L7
        //    ALOAD 1
        //    NEW net/minecraftforge/fml/common/InjectedModContainer
        //    DUP
        //    ALOAD 5
        //    ALOAD 5
        //    INVOKEINTERFACE net/minecraftforge/fml/common/ModContainer.getSource ()Ljava/io/File;
        //    INVOKESPECIAL net/minecraftforge/fml/common/InjectedModContainer.<init> (Lnet/minecraftforge/fml/common/ModContainer;Ljava/io/File;)V
        //    INVOKEVIRTUAL java/util/ArrayList.add (Ljava/lang/Object;)Z
        //    POP
        //   L8
        //    LINENUMBER 29 L8
        //    GOTO L4
        //   L5
        //    LINENUMBER 30 L5
        //   FRAME CHOP 1
        //    GETSTATIC net/minecraftforge/fml/common/discovery/ModDiscoverer.<fermion-inject:LOGGER> : Lorg/apache/logging/log4j/Logger;
        //    NEW java/lang/StringBuilder
        //    DUP
        //    INVOKESPECIAL java/lang/StringBuilder.<init> ()V
        //    LDC "Generated "
        //    INVOKEVIRTUAL java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
        //    ALOAD 1
        //    INVOKEVIRTUAL java/util/ArrayList.size ()I
        //    INVOKEVIRTUAL java/lang/StringBuilder.append (I)Ljava/lang/StringBuilder;
        //    LDC " containers: now injecting"
        //    INVOKEVIRTUAL java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
        //    INVOKEVIRTUAL java/lang/StringBuilder.toString ()Ljava/lang/String;
        //    INVOKEINTERFACE org/apache/logging/log4j/Logger.info (Ljava/lang/String;)V
        //   L9
        //    LINENUMBER 31 L9
        //    ALOAD 1
        //    ARETURN
        //   L10
        //    LOCALVARIABLE launchPluginContainer Lnet/minecraftforge/fml/common/ModContainer; L7 L8 5
        //    LOCALVARIABLE singleData Lorg/apache/commons/lang3/tuple/Pair; L6 L8 4
        //    // signature Lorg/apache/commons/lang3/tuple/Pair<Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata;Ljava/util/List<Ljava/lang/String;>;>;
        //    // declaration: org.apache.commons.lang3.tuple.Pair<net.thesilkminer.mc.fermion.asm.api.PluginMetadata, java.util.List<java.lang.String>>
        //    LOCALVARIABLE this Lnet/minecraftforge/fml/common/discovery/ModDiscoverer; L0 L10 0
        //    LOCALVARIABLE containers Ljava/util/ArrayList; L2 L10 1
        //    // signature Ljava/util/ArrayList<Lnet/minecraftforge/fml/common/ModContainer;>;
        //    // declaration: java.util.ArrayList<net.minecraftforge.fml.common.ModContainer>
        //    LOCALVARIABLE data Ljava/util/List; L3 L10 2
        //    // signature Ljava/util/List<Lorg/apache/commons/lang3/tuple/Pair<Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata;Ljava/util/List<Ljava/lang/String;>;>;>;
        //    // declaration: java.util.List<org.apache.commons.lang3.tuple.Pair<net.thesilkminer.mc.fermion.asm.api.PluginMetadata, java.util.List<java.lang.String>>>
        //    MAXSTACK = 5
        //    MAXLOCALS = 6

        private InjectFermionContainersVisitor(final int version, @Nonnull final MethodVisitor parent) {
            super(version, parent);
        }

        @Override
        public void visitCode() {
            LOGGER.info("Injecting '" + INJECT_CONTAINERS_METHOD_NAME + "' into class");

            // Create annotation
            final AnnotationVisitor nonNullVisitor = super.visitAnnotation("Ljavax/annotation/Nonnull;", true);
            nonNullVisitor.visitEnd();

            // Start creating the method code
            super.visitCode();

            final Label l0 = new Label();
            super.visitLabel(l0);
            super.visitLineNumber(100 + 4 * 10 + 1, l0);
            super.visitFieldInsn(Opcodes.GETSTATIC, THIS_CLASS_NAME, LOGGER_FIELD_NAME, "Lorg/apache/logging/log4j/Logger;");
            super.visitLdcInsn("Attempting to inject ModContainers into ModDiscoverer");
            super.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/apache/logging/log4j/Logger", "info", "(Ljava/lang/String;)V", true);

            final Label l1 = new Label();
            super.visitLabel(l1);
            super.visitLineNumber(100 + 4 * 10 + 2, l1);
            super.visitMethodInsn(Opcodes.INVOKESTATIC, "com/google/common/collect/Lists", "newArrayList", "()Ljava/util/ArrayList;", false);
            super.visitVarInsn(Opcodes.ASTORE, 1);

            final Label l2 = new Label();
            super.visitLabel(l2);
            super.visitLineNumber(100 + 4 * 10 + 3, l2);
            super.visitVarInsn(Opcodes.ALOAD, 0);
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, THIS_CLASS_NAME, GET_DATA_METHOD_NAME, "()Ljava/util/List;", false);
            super.visitVarInsn(Opcodes.ASTORE, 2);

            final Label l3 = new Label();
            super.visitLabel(l3);
            super.visitLineNumber(100 + 4 * 10 + 4, l3);
            super.visitVarInsn(Opcodes.ALOAD, 2);
            super.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true);
            super.visitVarInsn(Opcodes.ASTORE, 3);

            final Label l4 = new Label();
            final Label l5 = new Label();
            super.visitLabel(l4);
            super.visitFrame(Opcodes.F_APPEND, 3, new Object[] { "java/util/ArrayList", "java/util/List", "java/util/Iterator" }, 0, null);
            super.visitVarInsn(Opcodes.ALOAD, 3);
            super.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
            super.visitJumpInsn(Opcodes.IFEQ, l5);
            super.visitVarInsn(Opcodes.ALOAD, 3);
            super.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
            super.visitTypeInsn(Opcodes.CHECKCAST, "org/apache/commons/lang3/tuple/Pair");
            super.visitVarInsn(Opcodes.ASTORE, 4);

            final Label l6 = new Label();
            super.visitLabel(l6);
            super.visitLineNumber(100 + 4 * 10 + 5, l6);
            super.visitTypeInsn(Opcodes.NEW, "net/thesilkminer/mc/fermion/companion/LaunchPluginContainer");
            super.visitInsn(Opcodes.DUP);
            super.visitVarInsn(Opcodes.ALOAD, 4);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/apache/commons/lang3/tuple/Pair", "getKey", "()Ljava/lang/Object;", false);
            super.visitTypeInsn(Opcodes.CHECKCAST, "net/thesilkminer/mc/fermion/asm/api/PluginMetadata");
            super.visitVarInsn(Opcodes.ALOAD, 4);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/apache/commons/lang3/tuple/Pair", "getValue", "()Ljava/lang/Object;", false);
            super.visitTypeInsn(Opcodes.CHECKCAST, "java/util/List");
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, "net/thesilkminer/mc/fermion/companion/LaunchPluginContainer", "<init>", "(Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata;Ljava/util/List;)V", false);
            super.visitVarInsn(Opcodes.ASTORE, 5);

            final Label l7 = new Label();
            super.visitLabel(l7);
            super.visitLineNumber(100 + 4 * 10 + 6, l7);
            super.visitVarInsn(Opcodes.ALOAD, 1);
            super.visitTypeInsn(Opcodes.NEW, "net/minecraftforge/fml/common/InjectedModContainer");
            super.visitInsn(Opcodes.DUP);
            super.visitVarInsn(Opcodes.ALOAD, 5);
            super.visitVarInsn(Opcodes.ALOAD, 5);
            super.visitMethodInsn(Opcodes.INVOKEINTERFACE, "net/minecraftforge/fml/common/ModContainer", "getSource", "()Ljava/io/File;", true);
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, "net/minecraftforge/fml/common/InjectedModContainer", "<init>", "(Lnet/minecraftforge/fml/common/ModContainer;Ljava/io/File;)V", false);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false);
            super.visitInsn(Opcodes.POP);

            final Label l8 = new Label();
            super.visitLabel(l8);
            super.visitLineNumber(100 + 4 * 10 + 7, l8);
            super.visitJumpInsn(Opcodes.GOTO, l4);

            super.visitLabel(l5);
            super.visitLineNumber(100 + 4 * 10 + 8, l5);
            super.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            super.visitFieldInsn(Opcodes.GETSTATIC, THIS_CLASS_NAME, LOGGER_FIELD_NAME, "Lorg/apache/logging/log4j/Logger;");
            super.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            super.visitInsn(Opcodes.DUP);
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            super.visitLdcInsn("Generated ");
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            super.visitVarInsn(Opcodes.ALOAD, 1);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/ArrayList", "size", "()I", false);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false);
            super.visitLdcInsn(" containers: now injecting");
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            super.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/apache/logging/log4j/Logger", "info", "(Ljava/lang/String;)V", true);

            final Label l9 = new Label();
            super.visitLabel(l9);
            super.visitLineNumber(100 + 4 * 10 + 9, l9);
            super.visitVarInsn(Opcodes.ALOAD, 1);
            super.visitInsn(Opcodes.ARETURN);

            final Label l10 = new Label();
            super.visitLabel(l10);

            super.visitLocalVariable("instance", "Lnet/minecraftforge/fml/common/ModContainer;", null, l7, l8, 4);
            super.visitLocalVariable("data", "Lorg/apache/commons/lang3/tuple/Pair;", "Lorg/apache/commons/lang3/tuple/Pair<Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata;Ljava/util/List<Ljava/lang/String;>;>;", l6, l8, 3);
            super.visitLocalVariable("this", "L" + THIS_CLASS_NAME + ";", null, l0, l10, 0);
            super.visitLocalVariable("list", "Ljava/util/ArrayList;", "Ljava/util/ArrayList<Lnet/minecraftforge/fml/common/ModContainer;>;", l2, l10, 0);
            super.visitLocalVariable("pairs", "Ljava/util/List;", "Ljava/util/List<Lorg/apache/commons/lang3/tuple/Pair<Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata;Ljava/util/List<Ljava/lang/String;>;>;>;", l3, l10, 1);

            super.visitMaxs(5, 6);

            // And now we're done with the method code
            super.visitEnd();
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static final class GetDataMethodVisitor extends MethodVisitor {
        private int currentLineNumber = 2 * 100;

        private GetDataMethodVisitor(final int version, @Nonnull final MethodVisitor parent) {
            super(version, parent);
        }

        @Override
        public void visitCode() {
            LOGGER.info("Injecting '" + GET_DATA_METHOD_NAME + "' into class");

            // Visit the various annotations
            final AnnotationVisitor nonNull = super.visitAnnotation("Ljavax/annotation/Nonnull;", true);
            nonNull.visitEnd();

            // And now, the method's code
            super.visitCode();

            final Label lBegin = new Label();
            this.visitLabelAndLineNumber(lBegin);
            this.visitMethodInsn(Opcodes.INVOKESTATIC, "com/google/common/collect/ImmutableList", "builder", "()Lcom/google/common/collect/ImmutableList$Builder;", false);

            this.visitBuilder();

            final Label lReturn = new Label();
            this.visitLabelAndLineNumber(lReturn);
            super.visitInsn(Opcodes.ARETURN);

            final Label lEnd = new Label();
            super.visitLabel(lEnd);

            super.visitLocalVariable("this", "L" + THIS_CLASS_NAME + ";", null, lBegin, lEnd, 0);

            super.visitMaxs(6, 1);

            super.visitEnd();
        }

        private void visitLabelAndLineNumber(@Nonnull final Label label) {
            super.visitLabel(label);
            super.visitLineNumber(this.currentLineNumber, label);
            ++this.currentLineNumber;
        }

        private void visitBuilder() {
            LOGGER.info("Attempting to generate builder for LaunchPlugin data: " + launchPlugins);

            for (@Nonnull final LaunchPlugin launchPlugin : launchPlugins) {
                final Label lAddLine = new Label();
                this.visitLabelAndLineNumber(lAddLine);
                this.visitBuilderForLaunchPlugin(launchPlugin);
            }

            final Label lBuilderEnd = new Label();
            this.visitLabelAndLineNumber(lBuilderEnd);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/google/common/collect/ImmutableList$Builder", "build", "()Lcom/google/common/collect/ImmutableList;", false);

            LOGGER.info("Builder generated successfully");
        }

        private void visitBuilderForLaunchPlugin(@Nonnull final LaunchPlugin plugin) {
            LOGGER.debug("Generating builder add call for LaunchPlugin " + plugin);

            this.generateDataListFor(plugin.getMetadata());
            this.generatePackListFor(plugin.getRootPackages());

            super.visitMethodInsn(Opcodes.INVOKESTATIC, "org/apache/commons/lang3/tuple/ImmutablePair", "of", "(Ljava/lang/Object;Ljava/lang/Object;)Lorg/apache/commons/lang3/tuple/ImmutablePair;", false);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/google/common/collect/ImmutableList$Builder", "add", "(Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList$Builder;", false);

            LOGGER.debug("Add call generated successfully");
        }

        private void generateDataListFor(@Nonnull final PluginMetadata metadata) {
            LOGGER.debug("Generating entry for PluginMetadata " + metadata);

            super.visitLdcInsn(metadata.getId());
            super.visitMethodInsn(Opcodes.INVOKESTATIC, "net/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder",
                    "create", "(Ljava/lang/String;)Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder;", false);

            super.visitLdcInsn(metadata.getName());
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder",
                    "setName", "(Ljava/lang/String;)Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder;", false);

            final StringBuilder descBuilder = new StringBuilder();
            if (metadata.getDescription() != null) descBuilder.append(metadata.getDescription());
            this.appendTransformersForMetadata(metadata, descBuilder);

            LOGGER.debug("Successfully built advanced description '" + descBuilder + "'");

            super.visitLdcInsn(descBuilder.toString());
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder",
                    "setDescription", "(Ljava/lang/String;)Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder;", false);

            if (metadata.getCredits() != null) {
                super.visitLdcInsn(metadata.getCredits());
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder",
                        "setCredits", "(Ljava/lang/String;)Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder;", false);
            }

            metadata.getAuthors().forEach(author -> {
                super.visitLdcInsn(author.getName());
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder",
                        "addAuthor", "(Ljava/lang/String;)Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder;", false);
            });

            if (metadata.getUrl() != null) {
                super.visitLdcInsn(metadata.getUrl());
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder",
                        "setDisplayUrl", "(Ljava/lang/String;)Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder;", false);
            }

            super.visitLdcInsn(metadata.getVersion().toString());
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder",
                    "setVersion", "(Ljava/lang/String;)Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder;", false);

            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/thesilkminer/mc/fermion/asm/api/PluginMetadata$Builder",
                    "build", "()Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata;", false);

            LOGGER.debug("Entry generated successfully");
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

        private void generatePackListFor(@Nonnull final Set<String> rootPackages) {
            LOGGER.debug("Generating list for rootPackages " + rootPackages);

            super.visitMethodInsn(Opcodes.INVOKESTATIC, "com/google/common/collect/ImmutableList", "builder", "()Lcom/google/common/collect/ImmutableList$Builder;", false);

            rootPackages.forEach(it -> {
                super.visitLdcInsn(it);
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/google/common/collect/ImmutableList$Builder", "add", "(Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList$Builder;", false);
            });

            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/google/common/collect/ImmutableList$Builder", "build", "()Lcom/google/common/collect/ImmutableList;", false);

            LOGGER.debug("List generated successfully");
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static final class StaticBlockVisitor extends MethodVisitor {

        // // access flags 0x8
        // static <clinit>()V
        //  L0
        //   LINENUMBER 21 L0
        //   LDC "Fermion Companion/ModDiscoverer"
        //   INVOKESTATIC org/apache/logging/log4j/LogManager.getLogger (Ljava/lang/String;)Lorg/apache/logging/log4j/Logger;
        //   PUTSTATIC net/minecraftforge/fml/common/discovery/ModDiscoverer.<fermion-inject:LOGGER> : Lorg/apache/logging/log4j/Logger;
        //   RETURN
        //   MAXSTACK = 1
        //   MAXLOCALS = 0

        private StaticBlockVisitor(final int version, @Nonnull final MethodVisitor parent) {
            super(version, parent);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            final Label l0 = new Label();
            super.visitLabel(l0);
            super.visitLineNumber(100 + 6 * 10, l0);
            super.visitLdcInsn("Fermion Companion/ModDiscoverer");
            super.visitMethodInsn(Opcodes.INVOKESTATIC, "org/apache/logging/log4j/LogManager", "getLogger", "(Ljava/lang/String;)Lorg/apache/logging/log4j/Logger;", false);
            super.visitFieldInsn(Opcodes.PUTSTATIC, THIS_CLASS_NAME, LOGGER_FIELD_NAME, "Lorg/apache/logging/log4j/Logger;");
            super.visitInsn(Opcodes.RETURN);

            super.visitMaxs(1, 0);

            super.visitEnd();
        }
    }

    private static final Logger LOGGER = LogManager.getLogger("Fermion Companion/ModDiscovererTransformer");
    @SuppressWarnings("SpellCheckingInspection")
    private static final String THIS_CLASS_NAME = "net/minecraftforge/fml/common/discovery/ModDiscoverer";
    private static final String LOGGER_FIELD_NAME = "fermion$$injected$$LOGGER$$generated$$00_60_1122";
    private static final String INJECT_CONTAINERS_METHOD_NAME = "fermion$$injected$$injectFermionContainers$$generated$$00_69_1122";
    private static final String GET_DATA_METHOD_NAME = "fermion$$injected$$getData$$generated$$00_20_1122";

    public static List<LaunchPlugin> launchPlugins;
    public static List<String> transformers;
    public static TransformerRegistry registry;

    public ModDiscovererTransformer(@Nonnull final LaunchPlugin parent) {
        super(
                TransformerData.Builder.create()
                        .setOwningPlugin(parent)
                        .setName("mod_discoverer")
                        .setDescription("Transforms the ModDiscoverer class so that Fermion can inject their own Mod Container for event dispatch")
                        .build(),
                ClassDescriptor.of(THIS_CLASS_NAME)
        );
    }

    @Nonnull
    @Override
    public BiFunction<Integer, ClassVisitor, ClassVisitor> getClassVisitorCreator() {
        return (v, cw) -> new ClassVisitor(v, cw) {

            @Override
            @Nullable
            @SuppressWarnings("SpellCheckingInspection")
            public MethodVisitor visitMethod(final int access, @Nonnull final String name, @Nonnull final String desc,
                                             @Nullable final String signature, @Nullable final String[] exceptions) {
                final MethodVisitor parent = super.visitMethod(access, name, desc, signature, exceptions);

                if ("identifyMods".equals(name) && "()Ljava/util/List;".equals(desc)) {
                    LOGGER.info("Identified 'identifyMods()Ljava/util/List;' method: transforming now");
                    return new ModIdentifierMethodVisitor(v, parent);
                }

                return parent;
            }

            @Override
            public void visitEnd() {
                LOGGER.info("Reached end of class: injecting fields and methods");

                LOGGER.info("Injecting field '" + LOGGER_FIELD_NAME + "' into class");
                final FieldVisitor loggerField = super.visitField(
                        Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC,
                        LOGGER_FIELD_NAME,
                        "Lorg/apache/logging/log4j/Logger;",
                        null,
                        null
                );
                loggerField.visitEnd();

                final MethodVisitor injectMethod = new InjectFermionContainersVisitor(
                        v,
                        super.visitMethod(
                                Opcodes.ACC_PRIVATE,
                                INJECT_CONTAINERS_METHOD_NAME,
                                "()Ljava/util/ArrayList;",
                                "()Ljava/util/ArrayList<Lnet/minecraftforge/fml/common/ModContainer;>;",
                                null
                        )
                );
                injectMethod.visitCode();

                final MethodVisitor dataMethod = new GetDataMethodVisitor(
                        v,
                        super.visitMethod(
                                Opcodes.ACC_PRIVATE,
                                GET_DATA_METHOD_NAME,
                                "()Ljava/util/List;",
                                "()Ljava/util/List<Lorg/apache/commons/lang3/tuple/Pair<Lnet/thesilkminer/mc/fermion/asm/api/PluginMetadata;Ljava/util/List<Ljava/lang/String;>;>;>;",
                                null
                        )
                );
                dataMethod.visitCode();

                final MethodVisitor staticBlock = new StaticBlockVisitor(
                        v,
                        super.visitMethod(
                                Opcodes.ACC_STATIC,
                                "<clinit>",
                                "()V",
                                null,
                                null
                        )
                );
                staticBlock.visitCode();

                super.visitEnd();
            }
        };
    }
}
