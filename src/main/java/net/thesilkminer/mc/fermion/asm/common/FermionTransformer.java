package net.thesilkminer.mc.fermion.asm.common;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import net.minecraftforge.fml.loading.FileUtils;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.Transformer;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerRegistry;
import net.thesilkminer.mc.fermion.asm.common.utility.EffectivelyFinalByteArray;
import net.thesilkminer.mc.fermion.asm.common.utility.LaunchBlackboard;
import net.thesilkminer.mc.fermion.asm.common.utility.Log;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nonnull;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class FermionTransformer implements ITransformer<ClassNode> {

    private static final Log LOGGER = Log.of("Transformer");

    private final TransformerRegistry registry;
    private final Map<String, Boolean> environmentConfiguration;
    private final Map<ClassDescriptor, List<Transformer>> classToTransformer;
    private final Map<Transformer, String> transformerToName;
    private final Path dumpRoot;

    FermionTransformer(@Nonnull final LaunchBlackboard blackboard, @Nonnull final Map<String, Boolean> environmentConfig) {
        this.registry = blackboard;
        this.environmentConfiguration = environmentConfig;
        this.classToTransformer = Maps.newHashMap();
        this.transformerToName = Maps.newHashMap();
        this.dumpRoot = blackboard.getDumpDir();

        final Map<String, Transformer> transformers =  blackboard.getTransformers();
        transformers.forEach((k, v) -> this.transformerToName.put(v, k));

        transformers.values().forEach(it -> it.getClassesToTransform().forEach(c -> {
            final List<Transformer> transformerList = this.classToTransformer.computeIfAbsent(c, k -> Lists.newArrayList());
            transformerList.add(it);
        }));
    }

    @Nonnull
    @Override
    public ClassNode transform(@Nonnull final ClassNode input, @Nonnull final ITransformerVotingContext context) {
        LOGGER.d("Got 'em: " + input.name);

        final ClassDescriptor classDescriptor = ClassDescriptor.of(input.name);

        LOGGER.i("************************************************************************");
        LOGGER.i("Attempting to transform class '" + classDescriptor.getClassName() + "'");

        final EffectivelyFinalByteArray finalClassBytes = EffectivelyFinalByteArray.of(this.toByteArray(input));
        final List<Transformer> transformers = this.classToTransformer.get(classDescriptor);

        if (transformers.size() != 0) {
            LOGGER.d("Injecting universal transformer 'fermion.asm.service:universal'");
            transformers.add(new FermionUniversalTransformer());
        }

        transformers.sort((a, b) -> {
            if (a instanceof FermionUniversalTransformer) return 1;
            else if (b instanceof FermionUniversalTransformer) return -1;
            else return 0;
        });

        LOGGER.i("Found " + transformers.size() + " transformers available: running them one by one");
        LOGGER.d("    " + transformers);

        transformers.forEach(it -> {
            final boolean isUniversal = it instanceof FermionUniversalTransformer;

            final String registryName = isUniversal? FermionUniversalTransformer.TRANSFORMER_NAME : this.transformerToName.get(it);

            LOGGER.i("    Attempting to call transformer '" + registryName + "'");

            final boolean isEnabled = (isUniversal && finalClassBytes.wasTransformed()) || (!isUniversal && this.registry.isTransformerEnabled(registryName));
            if (!isEnabled) {
                if (isUniversal) {
                    LOGGER.w("        UNABLE TO CALL TRANSFORMER: Class wasn't patched previously");
                } else {
                    LOGGER.w("        UNABLE TO CALL TRANSFORMER: It was disabled in the configuration file");
                }
                return;
            }

            final ClassReader reader = new ClassReader(finalClassBytes.get());
            final ClassWriter writer = new ClassWriter(reader, Opcodes.ASM6);

            final ClassVisitor providedVisitor = it.getClassVisitorCreator().apply(Opcodes.ASM6, writer);

            reader.accept(providedVisitor, 0);

            finalClassBytes.transformInto(writer.toByteArray());

            LOGGER.i("    Transformer '" + registryName + "' called successfully");
        });

        LOGGER.i("Transformation run completed successfully for class '" + classDescriptor.getClassName() + "'");
        LOGGER.i("************************************************************************");

        final byte[] completelyTransformedClass = finalClassBytes.get();

        if (this.environmentConfiguration.get("dump")) this.dumpClassToDisk(input.name, completelyTransformedClass);

        return this.fromByteArray(completelyTransformedClass);
    }

    private byte[] toByteArray(@Nonnull final ClassNode node) {
        // We cannot compute frames because some classes may not be loaded
        // They'll get handled anyway after all the transformations are complete anyway
        // thanks to ModLauncher, so... whatever
        final ClassWriter writer = new ClassWriter(Opcodes.ASM5);
        node.accept(writer);
        return writer.toByteArray();
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private ClassNode fromByteArray(@Nonnull final byte[] array) {
        final ClassNode node = new ClassNode(Opcodes.ASM6);
        final ClassReader reader = new ClassReader(array);
        reader.accept(node, 0);
        return node;
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private void dumpClassToDisk(@Nonnull final String name, @Nonnull final byte[] classData) {
        if (this.dumpRoot == null) throw new IllegalStateException("this.dumpRoot == null");
        LOGGER.d("Dumping class data for " + name);

        final Path dumpLocation = this.dumpRoot.resolve("./" + name + ".class").toAbsolutePath().normalize();
        final Path parentDirectory = dumpLocation.resolve("./..").toAbsolutePath().normalize();
        try {
            FileUtils.getOrCreateDirectory(parentDirectory, parentDirectory.getFileName().toString());
            if (Files.notExists(dumpLocation)) {
                Files.createFile(dumpLocation);
            }
        } catch (final IOException e) {
            LOGGER.e("Unable to create file to dump class " + name + " on disk.", e);
        }

        try (final BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(dumpLocation.toFile()))) {
            writer.write(classData);
            LOGGER.d("Dumping completed");
        } catch (final IOException e) {
            LOGGER.e("Unable to dump class " + name + " to disk!", e);
        }
    }

    @Nonnull
    @Override
    public TransformerVoteResult castVote(@Nonnull final ITransformerVotingContext context) {
        // TODO Voting?
        return TransformerVoteResult.YES;
    }

    @Nonnull
    @Override
    public Set<Target> targets() {
        if (this.environmentConfiguration.get("emergency_mode")) {
            LOGGER.w("*********************************************************");
            LOGGER.w("*    FERMION IS CURRENTLY RUNNING IN EMERGENCY MODE!    *");
            LOGGER.w("* IN THIS STATE NO TRANSFORMERS WILL BE LOADED AT ALL!  *");
            LOGGER.w("*           DO NOT EXPECT YOUR CHANGES TO WORK          *");
            LOGGER.w("*********************************************************");
            return ImmutableSet.of();
        }

        return ImmutableSet.copyOf(
                this.classToTransformer.keySet()
                        .stream()
                        .map(ClassDescriptor::getClassName)
                        .map(Target::targetClass)
                        .collect(Collectors.toSet())
        );
    }
}
