package net.thesilkminer.mc.fermion.asm.common.utility;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.thesilkminer.mc.fermion.asm.api.Environment;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;
import net.thesilkminer.mc.fermion.asm.api.PluginMetadata;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public final class FermionEnvironment implements Environment {

    private static final Log LOGGER = Log.of("Environment");

    private final Map<String, Pair<PluginMetadata, LaunchPlugin>> pluginsMap;
    private final Map<String, Object> properties;

    FermionEnvironment(@Nonnull final Map<String, Pair<PluginMetadata, LaunchPlugin>> loadedPlugins,
                       @Nonnull final Map<String, Object> fmlEnvironment) {
        this.pluginsMap = ImmutableMap.copyOf(loadedPlugins);
        this.properties = Maps.newHashMap();
        cast(fmlEnvironment, this.properties);
    }

    private static void cast(@Nonnull final Map<String, Object> fmlEnvironment, @Nonnull final Map<String, Object> output) {
        output.putAll(fmlEnvironment);
        LOGGER.i("Injected FML data " + fmlEnvironment + " into Fermion Environment");
    }

    @Nonnull
    @Override
    public Collection<String> getLoadedLaunchPlugins() {
        return ImmutableSet.copyOf(this.pluginsMap.keySet());
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getProperty(@Nonnull final String key) {
        return Optional.ofNullable((T) this.properties.get(key));
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <T> T computeIfNotPresent(@Nonnull final String key, @Nonnull final Supplier<T> supplier) {
        return (T) this.properties.computeIfAbsent(key, it -> supplier.get());
    }

    @Nonnull
    @Override
    public Optional<LaunchPlugin> hasLaunchPlugin(@Nonnull final String id) {
        final Pair<PluginMetadata, LaunchPlugin> pair = this.pluginsMap.get(id);
        if (Objects.isNull(pair)) return Optional.empty();
        return Optional.of(pair.getValue());
    }

    @Nonnull
    Map<String, Object> getProperties() {
        return this.properties;
    }
}
