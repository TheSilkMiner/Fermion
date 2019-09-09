package net.thesilkminer.mc.fermion.asm.api;

import javax.annotation.Nonnull;

public interface LaunchPlugin {

    @Nonnull PluginMetadata getMetadata();
    void validateEnvironment(@Nonnull final Environment environment) throws IncompatibleEnvironmentException;
}
