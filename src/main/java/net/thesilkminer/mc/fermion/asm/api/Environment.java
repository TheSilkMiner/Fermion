package net.thesilkminer.mc.fermion.asm.api;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

public interface Environment {

    @Nonnull Collection<String> getLoadedLaunchPlugins();
    @Nonnull <T> Optional<T> getProperty(@Nonnull final String key);
    @Nonnull <T> T computeIfNotPresent(@Nonnull final String key, @Nonnull final Supplier<T> supplier);
    @Nonnull Optional<LaunchPlugin> hasLaunchPlugin(@Nonnull final String id);
}
