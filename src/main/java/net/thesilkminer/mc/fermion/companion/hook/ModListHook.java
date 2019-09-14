package net.thesilkminer.mc.fermion.companion.hook;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.thesilkminer.mc.fermion.asm.api.PluginMetadata;
import net.thesilkminer.mc.fermion.companion.LaunchPluginFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ModListHook {
    private static final Logger LOGGER = LogManager.getLogger("fermion.asm");
    private static final Marker MARKER = MarkerManager.getMarker("ModList Hooks");

    private static final Map<PluginMetadata, Object> LAUNCH_PLUGIN_CACHE = Maps.newHashMap();
    private static int size = -1;

    private ModListHook() {}

    public static void injectFermionLaunchPlugins(@Nonnull final List<ModFileInfo> modFiles, @Nonnull final List<ModInfo> sortedList,
                                                  @Nonnull final Map<String, ModFileInfo> fileById, @Nonnull final List<PluginMetadata> dataList) {

        LOGGER.info(MARKER, "Injecting Fermion Launch Plugins into mod list");
        LOGGER.debug(MARKER, "modFiles at start: " + modFiles);
        LOGGER.debug(MARKER, "sortedList at start: " + sortedList);
        LOGGER.debug(MARKER, "fileById at start: " + fileById);
        LOGGER.debug(MARKER, "dataList to inject: " + dataList);

        final List<ModFileInfo> modFilesCopy = Lists.newArrayList(modFiles);
        final List<ModInfo> sortedListCopy = Lists.newArrayList(sortedList);
        final Map<String, ModFileInfo> fileByIdCopy = Maps.newLinkedHashMap(fileById);

        modFiles.clear();
        sortedList.clear();
        fileById.clear();

        for (@Nonnull final PluginMetadata metadata : dataList) {

            LOGGER.debug(MARKER, "Adding entry for plugin meta " + metadata);

            final ModFileInfo fileInfo = (ModFileInfo) createFakeFileInfo(metadata);
            final ModInfo modInfo = (ModInfo) createFakeModInfo(fileInfo, metadata);
            patchModInfoModId(modInfo, metadata);
            ((LaunchPluginFile) LAUNCH_PLUGIN_CACHE.get(metadata)).setInfo(modInfo);
            ((LaunchPluginFile) LAUNCH_PLUGIN_CACHE.get(metadata)).setModFileInfo(fileInfo);
            final String id = metadata.getId();

            modFiles.add(fileInfo);
            sortedList.add(modInfo);
            fileById.put(id, fileInfo);
        }

        modFiles.addAll(modFilesCopy);
        sortedList.addAll(sortedListCopy);
        fileById.putAll(fileByIdCopy);

        LOGGER.debug(MARKER, "modFiles at end: " + modFiles);
        LOGGER.debug(MARKER, "sortedList at end: " + sortedList);
        LOGGER.debug(MARKER, "fileById at end: " + fileById);
    }

    @Nonnull
    private static Object createFakeModInfo(@Nonnull final Object modFileInfo, @Nonnull final PluginMetadata data) {
        return new ModInfo((ModFileInfo) modFileInfo, (UnmodifiableConfig) getFakeTomlForPlugin(data));
    }

    @Nonnull
    private static Object createFakeFileInfo(@Nonnull final PluginMetadata data) {
        try {
            final Class<?> modFileInfoClass = Class.forName("net.minecraftforge.fml.loading.moddiscovery.ModFileInfo");
            final Class<?> modFileClass = Class.forName("net.minecraftforge.fml.loading.moddiscovery.ModFile");
            final Class<?> unmodifiableConfigClass = Class.forName("com.electronwill.nightconfig.core.UnmodifiableConfig");
            final Constructor<?> constructor = modFileInfoClass.getDeclaredConstructor(modFileClass, unmodifiableConfigClass);
            constructor.setAccessible(true);
            return constructor.newInstance((ModFile) getFakeFile(data), (UnmodifiableConfig) createFakeTomlConfig(data));
        } catch (@Nonnull final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    private static Object createFakeTomlConfig(@Nonnull final PluginMetadata data) {
        final Config inMemoryFakeConfig = Config.inMemoryConcurrent();
        inMemoryFakeConfig.add("modLoader", "javafml");
        inMemoryFakeConfig.add("loaderVersion", "[28,)");
        inMemoryFakeConfig.add("mods", Lists.newArrayList((UnmodifiableConfig) getFakeTomlForPlugin(data)));
        return inMemoryFakeConfig;
    }

    @Nonnull
    private static Object getFakeTomlForPlugin(@Nonnull final PluginMetadata data) {
        final Config inMemoryFakeConfig = Config.inMemoryConcurrent();
        inMemoryFakeConfig.add("modId", "gen__" + data.getId().replace('.', '_'));
        inMemoryFakeConfig.add("version", data.getVersion().toString());
        inMemoryFakeConfig.add("displayName", data.getName());
        inMemoryFakeConfig.add("displayUrl", data.getUrl());
        inMemoryFakeConfig.add("logoFile", data.getLogo());
        inMemoryFakeConfig.add("credits", data.getCredits());
        inMemoryFakeConfig.add("authors", data.getAuthors().stream().map(PluginMetadata.Author::getName).collect(Collectors.joining(",")));
        inMemoryFakeConfig.add("description", data.getDescription());
        return inMemoryFakeConfig;
    }

    @Nonnull
    private static Object getFakeFile(@Nonnull final PluginMetadata data) {
        return LAUNCH_PLUGIN_CACHE.computeIfAbsent(data, k -> new LaunchPluginFile());
    }

    private static void patchModInfoModId(@Nonnull final Object object, @Nonnull final PluginMetadata metadata) {
        try {
            final Class<?> modInfoClass = Class.forName("net.minecraftforge.fml.loading.moddiscovery.ModInfo");
            final Field modId = modInfoClass.getDeclaredField("modId");
            modId.setAccessible(true);
            modId.set(object, metadata.getId());
        } catch (@Nonnull final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static int injectFermionContainersForSize(@Nonnull final List<ModContainer> containers, @Nonnull final List<PluginMetadata> metadataList) {
        if (size != -1) return size;

        LOGGER.info(MARKER, "Injecting launch plugins to calculate size");
        LOGGER.debug(MARKER, "containers at start: " + containers);
        LOGGER.debug(MARKER, "metadataList to inject: " + metadataList);

        size = containers.size() + metadataList.size();

        LOGGER.info(MARKER, "Calculated size: " + size);
        return size;
    }
}
