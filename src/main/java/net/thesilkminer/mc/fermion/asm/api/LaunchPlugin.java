package net.thesilkminer.mc.fermion.asm.api;

import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerRegistry;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public interface LaunchPlugin {

    @Nonnull PluginMetadata getMetadata();
    void validateEnvironment(@Nonnull final Environment environment) throws IncompatibleEnvironmentException;
    @Nonnull Consumer<TransformerRegistry> getTransformerRegister();

    default void onPostTransformersRegistration(@Nonnull final Environment environment, @Nonnull final TransformerRegistry registry) {}
    default void onPreConfigLoading() {}
}
