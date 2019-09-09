package net.thesilkminer.mc.fermion.asm.common.utility;

import com.google.common.collect.Maps;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;
import net.thesilkminer.mc.fermion.asm.api.PluginMetadata;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Map;

public final class LaunchBlackboard {

    private static final Log LOGGER = Log.of("Launch Blackboard");

    private final Map<String, Pair<PluginMetadata, LaunchPlugin>> pluginsMap;

    public LaunchBlackboard() {
        this.pluginsMap = Maps.newLinkedHashMap();
    }

    public void accept(@Nonnull final Iterable<LaunchPlugin> plugins, @Nonnull final IEnvironment fmlEnvironment) throws IncompatibleEnvironmentException {
        LOGGER.d("Accepting found Fermion Launch Plugins");
        for (@Nonnull final LaunchPlugin plugin : plugins) {
            this.accept(plugin);
        }
        final FermionEnvironment environment = new FermionEnvironment(this.pluginsMap, fmlEnvironment);
        // TODO Step 1: Environmental check for all plugins
        // TODO Step 2: Gather transformers data and inject them into a map
    }

    private void accept(@Nonnull final LaunchPlugin plugin) throws IncompatibleEnvironmentException {
        LOGGER.d("Attempting to load data from LaunchPlugin implementor " + plugin);
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
    }

    public void loadConfig(@Nonnull final Path root) {
        LOGGER.i("Loading up configs");
        System.out.println(root);
        // TODO
    }
}
