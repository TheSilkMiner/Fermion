package net.thesilkminer.mc.fermion.asm.api.transformer;

import javax.annotation.Nonnull;

/**
 * Acts as a registry where the transformers coming from various launch plugins
 * are registered and stored.
 *
 * @apiNote
 *      This interface has not been designed for implementation by
 *      <strong>clients</strong>. This means that you should not try to
 *      implement this interface in your own Launch Plugin. You should only
 *      rely on instances that are given to you via parameters or getters.
 *
 * @since 1.0.0
 */
public interface TransformerRegistry {

    /**
     * Registers the given transformer into the registry.
     *
     * <p>The transformer must be unique, so it must not have the same name as
     * another transformer that has been previously registered by the same
     * Launch Plugin. Conflicting names that come from different launch plugins
     * are allowed.</p>
     *
     * @param transformer
     *      The transformer instance that should be registered. It cannot be
     *      null.
     * @throws IllegalArgumentException
     *      If the given transformer was already registered.
     *
     * @since 1.0.0
     */
    void registerTransformer(@Nonnull final Transformer transformer);

    /**
     * Checks whether the transformer that is uniquely identified by the passed
     * in registry name is enabled or not.
     *
     * <p>Note that this value cannot be considered permanent up until the
     * actual transforming process has begun. The actual value may in fact
     * change according to external conditions (that are implementation
     * dependent) until a value gets finalized.</p>
     *
     * <p>Also note that the {@code registryName} passed in does
     * <strong>not</strong> match the name of the transformer.</p>
     *
     * @param registryName
     *      The registry name that uniquely identifies the transformer.
     *      It cannot be null.
     * @return
     *      Whether the transformer identified by its registry name is enabled
     *      or not.
     * @throws NullPointerException
     *      If no such transformer exists.
     *
     * @since 1.0.0
     */
    boolean isTransformerEnabled(@Nonnull final String registryName);
}
