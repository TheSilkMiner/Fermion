package net.thesilkminer.mc.fermion.asm.common;

import com.google.common.collect.ImmutableList;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import net.minecraftforge.fml.loading.FileUtils;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;
import net.thesilkminer.mc.fermion.asm.common.utility.LaunchBlackboard;
import net.thesilkminer.mc.fermion.asm.common.utility.Log;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

public final class FermionTransformationService implements ITransformationService {

    private static final Log LOGGER = Log.of("Transformation Service");

    private final LaunchBlackboard blackboard;

    public FermionTransformationService() {
        this.blackboard = new LaunchBlackboard();
    }

    @Nonnull
    @Override
    public final String name() {
        return "fermion.asm.service";
    }

    @Override
    public final void initialize(@Nonnull final IEnvironment environment) {
        LOGGER.d("Initializing");
        LOGGER.i("Loading configuration files for Fermion environment");
        LOGGER.d("Attempting to find configuration directory");
        final Path gameDirectory = environment.getProperty(IEnvironment.Keys.GAMEDIR.get())
                .orElseThrow(() -> new IllegalStateException("No game directory was found. This is a serious error"));
        final Path configDirectory = gameDirectory.resolve("config/FermionEnv").toAbsolutePath().normalize();
        FileUtils.getOrCreateDirectory(configDirectory, "FermionEnv");
        LOGGER.d("Loading configuration files mod per mod");
        this.blackboard.loadConfig(configDirectory);
        LOGGER.i("Configuration loaded");
    }

    @Override
    public final void beginScanning(@Nonnull final IEnvironment environment) {
        LOGGER.d("Begin scanning");
    }

    @Override
    public final void onLoad(@Nonnull final IEnvironment env, @Nonnull final Set<String> otherServices) throws IncompatibleEnvironmentException {
        LOGGER.i("Fermion Transformer Service is being loaded");
        LOGGER.i("Attempting to discover Fermion core-mods");
        final ServiceLoader<LaunchPlugin> launchPluginLoader = ServiceLoader.load(LaunchPlugin.class);
        this.blackboard.accept(launchPluginLoader, env);
        LOGGER.i("Fermion core-mods discovery completed");
    }

    @Nonnull
    @Override
    @SuppressWarnings("rawtypes") // Fuck you: List<ITransformer<?>> was hard, wasn't it
    public final List<ITransformer> transformers() {
        LOGGER.i("Registered Fermion transformer");
        return ImmutableList.of(new FermionTransformer(this.blackboard));
    }
}
