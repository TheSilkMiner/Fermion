/*
 * Copyright (C) 2020  TheSilkMiner
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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import net.thesilkminer.mc.fermion.asm.api.IncompatibleEnvironmentException;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;
import net.thesilkminer.mc.fermion.asm.api.PluginMetadata;
import net.thesilkminer.mc.fermion.asm.api.configuration.TransformerConfiguration;
import net.thesilkminer.mc.fermion.asm.api.transformer.Transformer;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerRegistry;
import net.thesilkminer.mc.fermion.asm.common.shade.net.minecraftforge.fml.loading.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public final class LaunchBlackboard implements TransformerRegistry {

    private static final Log LOGGER = Log.of("Launch Blackboard");

    private final Map<String, Pair<PluginMetadata, LaunchPlugin>> pluginsMap;
    private final Map<String, Transformer> transformers;
    private final Map<String, JsonObject> configEntries;

    private Path dumpDir;

    public LaunchBlackboard() {
        this.pluginsMap = Maps.newLinkedHashMap();
        this.transformers = Maps.newHashMap();
        this.configEntries = Maps.newHashMap();
    }

    public void accept(@Nonnull final Iterable<LaunchPlugin> plugins, @Nonnull final Map<String, Object> injectedData) throws IncompatibleEnvironmentException {
        LOGGER.d("Accepting found Fermion Launch Plugins");
        for (@Nonnull final LaunchPlugin plugin : plugins) {
            this.accept(plugin);
        }

        LOGGER.d("Validating environment for plugins");
        final FermionEnvironment environment = new FermionEnvironment(this.pluginsMap, injectedData);
        for (@Nonnull final Pair<PluginMetadata, LaunchPlugin> plugin : this.pluginsMap.values()) {
            LOGGER.t("Validating environment for plugin '" + plugin.getKey().getId() + "'");
            plugin.getValue().validateEnvironment(environment);
        }

        LOGGER.d("Launching plugin transformers registration");
        for (@Nonnull final Pair<PluginMetadata, LaunchPlugin> plugin : this.pluginsMap.values()) {
            LOGGER.i("Registering transformers for plugin '" + plugin.getKey().getId() + "'");
            plugin.getValue().getTransformerRegister().accept(this);
        }

        LOGGER.d("Invoking post-transformers registration event handler");
        for (@Nonnull final Pair<PluginMetadata, LaunchPlugin> plugin : this.pluginsMap.values()) {
            plugin.getValue().onPostTransformersRegistration(environment, this);
        }
    }

    private void accept(@Nonnull final LaunchPlugin plugin) throws IncompatibleEnvironmentException {
        LOGGER.t("Attempting to load data from LaunchPlugin implementor " + plugin);
        final PluginMetadata pluginMetadata = plugin.getMetadata();
        final String pluginId = pluginMetadata.getId();
        if (this.pluginsMap.containsKey(pluginId)) {
            final Pair<PluginMetadata, LaunchPlugin> pair = this.pluginsMap.get(pluginId);
            throw new IncompatibleEnvironmentException("There is already a plugin registered with the same id '" + pluginId + "'.\n" +
                    "Plugin instance: " + pair.getValue() + "\n" +
                    "Plugin metadata: " + pair.getKey());
        }
        LOGGER.i("Found Fermion LaunchPlugin with id '" + pluginId + "': performing registration");
        this.pluginsMap.put(pluginId, ImmutablePair.of(pluginMetadata, plugin));
        LOGGER.t("Plugin '" + pluginId + "' registered successfully");
    }

    public void acceptDumpDir(@Nonnull final Path root) {
        this.dumpDir = root;
    }

    public void loadConfig(@Nonnull final Path root) {
        LOGGER.i("Received config loading request: loading them now");

        LOGGER.d("Triggering pre-config loading handlers");
        for (@Nonnull final Pair<PluginMetadata, LaunchPlugin> plugin : this.pluginsMap.values()) {
            plugin.getValue().onPreConfigLoading();
        }

        this.pluginsMap.forEach((k, v) -> this.loadPluginConfig(root, k));
    }

    private void loadPluginConfig(@Nonnull final Path root, @Nonnull final String id) {
        LOGGER.i("Loading configuration for plugin '" + id + "'");
        final Path pluginDir = root.resolve(id).toAbsolutePath().normalize();
        FileUtils.getOrCreateDirectory(pluginDir, id);
        final Path transformersConfigFile = pluginDir.resolve("./transformers.json").toAbsolutePath().normalize();
        LOGGER.d("Attempting to read and/or create file " + transformersConfigFile);
        if (Files.notExists(transformersConfigFile)) {
            try (final BufferedWriter writer = Files.newBufferedWriter(transformersConfigFile, StandardCharsets.UTF_8)) {
                writer.write("{}");
                writer.flush();
            } catch (@Nonnull final IOException e) {
                if (e instanceof FileAlreadyExistsException) {
                    LOGGER.w("Weird. Config file for plugin '" + id + "' was deemed non-existent, but now it is there. Whatever...");
                } else {
                    throw new RuntimeException("Unable to create configuration file for plugin '" + id + "'", e);
                }
            }
        }

        final Gson gson = new GsonBuilder()
                .serializeNulls()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();

        /*mutable*/ JsonObject transformersConfigArray;

        try (final Reader reader = Files.newBufferedReader(transformersConfigFile)) {

            transformersConfigArray = gson
                    .getAdapter(TypeToken.get(JsonObject.class))
                    .read(new JsonReader(reader));

            final JsonObject finalTransformersConfigArray = transformersConfigArray;

            this.getTransformers()
                    .entrySet()
                    .stream()
                    .filter(it -> it.getKey().startsWith(id + ":"))
                    .forEach(it -> this.loadTransformerConfig(finalTransformersConfigArray, it.getKey(), it.getValue()));

            transformersConfigArray = finalTransformersConfigArray;
        } catch (@Nonnull final IOException e) {
            throw new RuntimeException("An error has occurred while attempting to read the configuration file for plugin '" + id + "'", e);
        }


        try (final BufferedWriter writer = Files.newBufferedWriter(transformersConfigFile)) {
            gson.toJson(transformersConfigArray, writer);
        } catch (@Nonnull final IOException e) {
            throw new RuntimeException("An error has occurred while attempting to write the configuration file for plugin '" + id + "'", e);
        }
    }

    private void loadTransformerConfig(@Nonnull final JsonObject jsonConfig, @Nonnull final String registryName, @Nonnull final Transformer transformer) {
        final TransformerData data = transformer.getData();
        final String name = data.getName();

        if (!jsonConfig.has(name)) this.createDefaultConfigEntry(jsonConfig, transformer, data);

        final JsonObject configuration = jsonConfig.get(name).getAsJsonObject();
        final JsonObject specialConfiguration = configuration.get("configuration").getAsJsonObject();

        final TransformerConfiguration transformerConfiguration = transformer.provideConfiguration().get();
        final JsonObject defaultedSpecial = transformerConfiguration.getDefaultProvider().apply(specialConfiguration);

        configuration.add("configuration", defaultedSpecial);

        transformer.applyConfiguration(defaultedSpecial);

        this.configEntries.put(registryName, configuration);

        LOGGER.i("Successfully loaded configuration file for transformer '" + registryName + "'");
    }

    private void createDefaultConfigEntry(@Nonnull final JsonObject main, @Nonnull final Transformer transformer, @Nonnull final TransformerData data) {
        final JsonObject config = new JsonObject();
        config.add("description", new JsonPrimitive(data.getDescription()));
        config.add("enabled", new JsonPrimitive(data.isEnabledByDefault()));

        final TransformerConfiguration transformerConfiguration = transformer.provideConfiguration().get();
        final JsonObject defaultConfig = Optional.ofNullable(transformerConfiguration.getSerializer().get()).orElseGet(JsonObject::new);

        config.add("configuration", defaultConfig);

        main.add(data.getName(), config);
    }

    @Nonnull
    public Map<String, Transformer> getTransformers() {
        return ImmutableMap.copyOf(this.transformers);
    }

    @Nullable
    public Path getDumpDir() {
        return this.dumpDir;
    }

    @Override
    public void registerTransformer(@Nonnull final Transformer transformer) {
        Preconditions.checkNotNull(transformer);
        final TransformerData data = transformer.getData();
        final String pluginId = data.getOwningPluginId();
        final String name = data.getName();
        final String registryName = pluginId + ":" + name;
        if (this.transformers.get(registryName) != null) {
            throw new IllegalArgumentException("Unable to register transformer " + transformer + " with the given name, because it is already registered.\n" +
                    "Name: " + registryName + "\n" +
                    "Transformer already in registry: " + this.transformers.get(registryName));
        }
        LOGGER.i("Registered transformer '" + registryName + "' with class '" + transformer.getClass() + "'");
        this.transformers.put(registryName, transformer);
    }

    @Override
    public boolean isTransformerEnabled(@Nonnull final String registryName) {
        final JsonObject configObject = Preconditions.checkNotNull(this.configEntries.get(registryName));
        return configObject.get("enabled").getAsJsonPrimitive().getAsBoolean();
    }
}
