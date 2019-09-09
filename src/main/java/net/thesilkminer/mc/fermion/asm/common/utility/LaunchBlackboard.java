package net.thesilkminer.mc.fermion.asm.common.utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;
import net.thesilkminer.mc.fermion.asm.api.PluginMetadata;
import net.thesilkminer.mc.fermion.asm.api.transformer.Transformer;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerRegistry;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Map;

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

        System.out.println(root);
        // TODO
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
