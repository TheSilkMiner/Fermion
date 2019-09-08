package net.thesilkminer.mc.fermion.asm.common;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import net.thesilkminer.mc.fermion.asm.common.utility.Log;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class FermionTransformationService implements ITransformationService {

    private static final Log LOGGER = Log.of("Transformation Service");

    @Nonnull
    @Override
    public final String name() {
        return "fermion.asm.service";
    }

    @Override
    public final void initialize(@Nonnull final IEnvironment environment) {
        LOGGER.d("Initializing");
    }

    @Override
    public final void beginScanning(@Nonnull final IEnvironment environment) {
        LOGGER.d("Begin scanning");
    }

    @Override
    public final void onLoad(@Nonnull final IEnvironment env, @Nonnull final Set<String> otherServices) throws IncompatibleEnvironmentException {
        LOGGER.i("Fermion Transformer Service is being loaded");
        LOGGER.i("Attempting to discover Fermion core-mods");
        // Query transformers to load here
        LOGGER.i("Fermion core-mods discovery completed");
    }

    @Nonnull
    @Override
    @SuppressWarnings("rawtypes") // Fuck you: List<ITransformer<?>> was hard, wasn't it
    public final List<ITransformer> transformers() {
        LOGGER.d("Transformers");
        // TODO
        return new ArrayList<>();
    }
}
