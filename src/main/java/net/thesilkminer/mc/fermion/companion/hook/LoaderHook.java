package net.thesilkminer.mc.fermion.companion.hook;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.fml.common.InjectedModContainer;
import net.minecraftforge.fml.common.ModContainer;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;
import net.thesilkminer.mc.fermion.companion.LaunchPluginContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public final class LoaderHook {

    private static final Logger LOGGER = LogManager.getLogger("LoaderHook");

    public static void injectFermionContainers(@Nonnull final List<ModContainer> mods) {
        LOGGER.info("Injecting Fermion Launch Plugin containers into the mod list");
        getLaunchPluginsMap().forEach(plugin -> {
            LOGGER.debug("Adding entry for plugin '" + plugin + "'");
            final ModContainer container = new LaunchPluginContainer(plugin.getMetadata(), cast(plugin.getRootPackages()));
            mods.add(new InjectedModContainer(container, container.getSource()));
        });
    }

    @Nonnull
    private static List<LaunchPlugin> getLaunchPluginsMap() {
        LOGGER.error("To be filled with ASM");
        return ImmutableList.of();
    }

    @Nonnull
    private static List<String> cast(@Nonnull final Set<String> packages) {
        return ImmutableList.copyOf(packages);
    }
}
