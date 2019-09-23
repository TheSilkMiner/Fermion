package net.thesilkminer.mc.fermion.asm.api;

import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerRegistry;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Identifies a launch plugin that will be loaded through the implementation
 * and that provides a set of transformers.
 *
 * <p>Note that launch plugins are discovered and loaded automatically by the
 * environment.</p>
 *
 * @since 1.0.0
 */
public interface LaunchPlugin {

    /**
     * Gets all the data associated to this launch plugin, such as its ID or
     * its name.
     *
     * <p>The data must be complete in all its parts. It may or may not be a
     * new instance every time this method is called, provided its data does
     * not change between calls.</p>
     *
     * <p>Refer to {@link PluginMetadata} for more information on which data it
     * is required to provide.</p>
     *
     * @return
     *      A {@link PluginMetadata} object, which must not be null, containing
     *      the entirety of the plugin data that may be needed.
     *
     * @since 1.0.0
     */
    @Nonnull PluginMetadata getMetadata();

    /**
     * Gets the root packages that define where the Launch Plugin files are
     * located inside their JAR.
     *
     * <p>This also means that every part that is secondary to the Launch
     * Plugin but needed for the working of the Mod must be identified here.
     * E.g., if a mod is in package {@code com.example.mod} and the Launch
     * Plugin is in package {@code com.example.mod.asm}, then the first one
     * should be specified.</p>
     *
     * <p>It is illegal to specify either {@code net.minecraft} or
     * {@code net.minecraftforge}. The string must not terminate with a dot.
     * The set cannot contain subpackages of each other (e.g., you cannot
     * specify both {@code com.example} and {@code com.example.sub}): only
     * the parent one should be specified.</p>
     *
     * @return
     *      A {@link Set} containing the root packages that define where the
     *      Launch Plugin files are located inside their JAR.
     *
     * @since 1.0.0
     */
    @Nonnull Set<String> getRootPackages();

    /**
     * Validates whether the environment is valid for the loading of this
     * transformer.
     *
     * @param environment
     *      An {@link Environment} instance that contains all information about
     *      the current environment. It cannot be null.
     * @throws IncompatibleEnvironmentException
     *      If the current environment is incompatible with the loading of the
     *      launch plugin.
     *
     * @since 1.0.0
     */
    void validateEnvironment(@Nonnull final Environment environment) throws IncompatibleEnvironmentException;

    /**
     * Gets a {@link Consumer} that registers the various transformers to the
     * supplied {@link TransformerRegistry}.
     *
     * <p>It is legal for a plugin, though a bit useless, to provide an empty
     * consumer, thus not registering any transformers.</p>
     *
     * <p>The passed in transformer registry is guaranteed to be non-null.</p>
     *
     * @return
     *      A consumer that registers the various transformers. It cannot be
     *      null.
     *
     * @since 1.0.0
     */
    @Nonnull Consumer<TransformerRegistry> getTransformerRegister();

    /**
     * Allows a launch plugin to react to changes in the environment or in the
     * transformer registry after the transformers have been registered.
     *
     * <p>It is illegal for a launch plugin to register any kind of transformer
     * in this event handler. Transformers can only be registered when the
     * implementation deems necessary, through the consumer provided by
     * {@link #getTransformerRegister()}.</p>
     *
     * <p>This method is guaranteed to be called after
     * {@link #getTransformerRegister()} and after the consumer returned by
     * that method has run.</p>
     *
     * @param environment
     *      The environment the launch plugin is currently in. Guaranteed
     *      to be non-null.
     * @param registry
     *      The registry instance where the transformers got registered
     *      previously. It is guaranteed not to be null.
     *
     * @since 1.0.0
     */
    default void onPostTransformersRegistration(@Nonnull final Environment environment, @Nonnull final TransformerRegistry registry) {}

    /**
     * Allows a launch plugin to perform operations before the configuration
     * files are loaded.
     *
     * @since 1.0.0
     */
    default void onPreConfigLoading() {}
}
