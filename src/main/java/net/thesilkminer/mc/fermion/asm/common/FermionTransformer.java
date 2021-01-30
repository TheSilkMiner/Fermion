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

package net.thesilkminer.mc.fermion.asm.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.launchwrapper.IClassTransformer;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.Transformer;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerRegistry;
import net.thesilkminer.mc.fermion.asm.common.shade.net.minecraftforge.fml.loading.FileUtils;
import net.thesilkminer.mc.fermion.asm.common.utility.EffectivelyFinalByteArray;
import net.thesilkminer.mc.fermion.asm.common.utility.LaunchBlackboard;
import net.thesilkminer.mc.fermion.asm.common.utility.Log;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nonnull;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class FermionTransformer implements IClassTransformer {

    private static final class TransformerData {
        private final TransformerRegistry registry;
        private final Map<String, Boolean> environmentConfiguration;
        private final Map<ClassDescriptor, List<Transformer>> classToTransformer;
        private final Map<Transformer, String> transformerToName;
        private final Path dumpRoot;
        private final boolean emergencyMode;

        private TransformerData(@Nonnull final LaunchBlackboard blackboard, @Nonnull final Map<String, Boolean> envConfig) {
            this.registry = blackboard;
            this.environmentConfiguration = envConfig;
            this.classToTransformer = Maps.newHashMap();
            this.transformerToName = Maps.newHashMap();
            this.dumpRoot = blackboard.getDumpDir();
            this.emergencyMode = this.environmentConfiguration.get("emergency_mode");

            final Map<String, Transformer> transformers = blackboard.getTransformers();
            transformers.forEach((k, v) -> this.transformerToName.put(v, k));

            transformers.values().forEach(it -> it.getClassesToTransform().forEach(c -> {
                final List<Transformer> transformerList = this.classToTransformer.computeIfAbsent(c, k -> Lists.newArrayList());
                transformerList.add(it);
            }));

            if (this.emergencyMode) {
                LOGGER.w("*********************************************************");
                LOGGER.w("*    FERMION IS CURRENTLY RUNNING IN EMERGENCY MODE!    *");
                LOGGER.w("* IN THIS STATE NO TRANSFORMERS WILL BE LOADED AT ALL!  *");
                LOGGER.w("*           DO NOT EXPECT YOUR CHANGES TO WORK          *");
                LOGGER.w("*********************************************************");
            }
        }
    }

    private static final Log LOGGER = Log.of("Transformer");

    private static TransformerData data;

    public FermionTransformer() {
        LOGGER.i("Transformer initialized! Waiting for an accept call");
    }

    static void accept(@Nonnull final LaunchBlackboard blackboard, @Nonnull final Map<String, Boolean> configuration) {
        LOGGER.i("Attempting to accept data from setup");
        data = new TransformerData(blackboard, configuration);
    }

    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        if (data == null) {
            LOGGER.e("Unable to transform class '" + transformedName + "': no Transformers were accepted");
            return basicClass;
        }

        if (data.emergencyMode) return basicClass;

        // We create the class descriptor with the transformed class name, because I hope nobody is actually attempting
        // to transform a class using MOJ names. And if they are... their problem
        final ClassDescriptor classDescriptor = ClassDescriptor.of(transformedName);

        final EffectivelyFinalByteArray finalClassBytes;
        try {
            finalClassBytes = EffectivelyFinalByteArray.of(basicClass);
        } catch (@Nonnull final NullPointerException e) {
            return basicClass;
        }
        final List<Transformer> transformers = Lists.newArrayList();

        transformers.addAll(Optional.ofNullable(data.classToTransformer.get(classDescriptor)).orElseGet(Lists::newArrayList));

        if (transformers.size() == 0) return basicClass;

        LOGGER.d("Got 'em: " + name + " --> " + transformedName);
        LOGGER.i("************************************************************************");
        LOGGER.i("Attempting to transform class '" + classDescriptor.getClassName() + "'");
        LOGGER.d("Injecting universal transformer 'fermion.asm.service:universal'");
        transformers.add(new FermionUniversalTransformer());

        transformers.sort((a, b) -> {
            if (a instanceof FermionUniversalTransformer) return 1;
            else if (b instanceof FermionUniversalTransformer) return -1;
            else return 0;
        });

        LOGGER.i("Found " + transformers.size() + " transformers available: running them one by one");
        LOGGER.d("    " + transformers);

        transformers.forEach(it -> {
            final boolean isUniversal = it instanceof FermionUniversalTransformer;

            final String registryName = isUniversal? FermionUniversalTransformer.TRANSFORMER_NAME : data.transformerToName.get(it);

            LOGGER.i("    Attempting to call transformer '" + registryName + "'");

            final boolean isEnabled = (isUniversal && finalClassBytes.wasTransformed()) || (!isUniversal && data.registry.isTransformerEnabled(registryName));
            if (!isEnabled) {
                if (isUniversal) {
                    LOGGER.w("        UNABLE TO CALL TRANSFORMER: Class wasn't patched previously");
                } else {
                    LOGGER.w("        UNABLE TO CALL TRANSFORMER: It was disabled in the configuration file");
                }
                return;
            }

            final ClassReader reader = new ClassReader(finalClassBytes.get());
            final ClassWriter writer = new ClassWriter(reader, Opcodes.ASM5);

            final ClassVisitor providedVisitor = it.getClassVisitorCreator().apply(Opcodes.ASM5, writer);

            reader.accept(providedVisitor, 0);

            finalClassBytes.transformInto(writer.toByteArray());

            LOGGER.i("    Transformer '" + registryName + "' called successfully");
        });

        LOGGER.i("Transformation run completed successfully for class '" + classDescriptor.getClassName() + "'");
        LOGGER.i("************************************************************************");

        final byte[] completelyTransformedClass = finalClassBytes.get();

        if (data.environmentConfiguration.get("dump")) this.dumpClassToDisk(transformedName, completelyTransformedClass);

        return completelyTransformedClass;
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private void dumpClassToDisk(@Nonnull final String name, @Nonnull final byte[] classData) {
        if (data.dumpRoot == null) throw new IllegalStateException("this.dumpRoot == null");
        final String dumpName = name.replace('.', '/');
        LOGGER.d("Dumping class data for " + dumpName);

        final Path dumpLocation = data.dumpRoot.resolve("./" + dumpName + ".class").toAbsolutePath().normalize();
        final Path parentDirectory = dumpLocation.resolve("./..").toAbsolutePath().normalize();
        try {
            FileUtils.getOrCreateDirectory(parentDirectory, parentDirectory.getFileName().toString());
            if (Files.notExists(dumpLocation)) {
                Files.createFile(dumpLocation);
            }
        } catch (final IOException e) {
            LOGGER.e("Unable to create file to dump class " + dumpName + " on disk.", e);
        }

        try (final BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(dumpLocation.toFile()))) {
            writer.write(classData);
            LOGGER.d("Dumping completed");
        } catch (final IOException e) {
            LOGGER.e("Unable to dump class " + dumpName + " to disk!", e);
        }
    }
}
