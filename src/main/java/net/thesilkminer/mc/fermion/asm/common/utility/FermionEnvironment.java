/*
 * Copyright (C) 2019  TheSilkMiner
 *
 * This file is part of Fermion.
 *
 * Fermion is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Fermion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Fermion.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact information:
 * E-mail: thesilkminer <at> outlook <dot> com
 */

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
