package net.thesilkminer.mc.fermion.asm.api.transformer;

import javax.annotation.Nonnull;

public interface TransformerRegistry {
    void registerTransformer(@Nonnull final Transformer transformer);
    boolean isTransformerEnabled(@Nonnull final String registryName);
}
