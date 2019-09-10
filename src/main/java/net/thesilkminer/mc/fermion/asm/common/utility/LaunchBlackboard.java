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
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import net.minecraftforge.fml.loading.FileUtils;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;
import net.thesilkminer.mc.fermion.asm.api.PluginMetadata;
import net.thesilkminer.mc.fermion.asm.api.configuration.TransformerConfiguration;
import net.thesilkminer.mc.fermion.asm.api.transformer.Transformer;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerRegistry;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
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

    public LaunchBlackboard() {
        this.pluginsMap = Maps.newLinkedHashMap();
        this.transformers = Maps.newHashMap();
        this.configEntries = Maps.newHashMap();
    }

    public void accept(@Nonnull final Iterable<LaunchPlugin> plugins, @Nonnull final IEnvironment fmlEnvironment) throws IncompatibleEnvironmentException {
        LOGGER.d("Accepting found Fermion Launch Plugins");
        for (@Nonnull final LaunchPlugin plugin : plugins) {
            this.accept(plugin);
        }

        LOGGER.d("Validating environment for plugins");
        final FermionEnvironment environment = new FermionEnvironment(this.pluginsMap, fmlEnvironment);
        try {
            for (@Nonnull final Pair<PluginMetadata, LaunchPlugin> plugin : this.pluginsMap.values()) {
                LOGGER.t("Validating environment for plugin '" + plugin.getKey().getId() + "'");
                plugin.getValue().validateEnvironment(environment);
            }
        } catch (@Nonnull final net.thesilkminer.mc.fermion.asm.api.IncompatibleEnvironmentException exception) {
            final IncompatibleEnvironmentException t = new IncompatibleEnvironmentException("A Fermion LaunchPlugin is not compatible with this environment");
            t.initCause(exception);
            throw t;
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

        try (final Reader reader = Files.newBufferedReader(transformersConfigFile)) {

            final JsonObject transformersConfigArray = gson
                    .getAdapter(TypeToken.get(JsonObject.class))
                    .read(new JsonReader(reader));

            this.getTransformers()
                    .entrySet()
                    .stream()
                    .filter(it -> it.getKey().startsWith(id + ":"))
                    .forEach(it -> this.loadTransformerConfig(transformersConfigArray, it.getKey(), it.getValue()));

        } catch (@Nonnull final IOException e) {
            throw new RuntimeException("An error has occurred while attempting to read the configuration file for plugin '" + id + "'", e);
        }
    }

    private void loadTransformerConfig(@Nonnull final JsonObject jsonConfig, @Nonnull final String registryName, @Nonnull final Transformer transformer) {
        final TransformerData data = transformer.getData();
        final String name = data.getName();

        if (!jsonConfig.has(name)) this.createDefaultConfigEntry(jsonConfig, transformer, data);

        final JsonObject configuration = jsonConfig.get(name).getAsJsonObject();
        final JsonObject specialConfiguration = jsonConfig.get("configuration").getAsJsonObject();

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
    public boolean isTransformerEnabled(@Nonnull final String id, @Nonnull final String transformerName) {
        final String registryName = id + ":" + transformerName;
        final JsonObject configObject = Preconditions.checkNotNull(this.configEntries.get(registryName));
        return configObject.get("enabled").getAsJsonPrimitive().getAsBoolean();
    }
}
