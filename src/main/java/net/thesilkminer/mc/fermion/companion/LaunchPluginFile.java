package net.thesilkminer.mc.fermion.companion;

import com.google.common.collect.Lists;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.CoreModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.jar.Manifest;

public final class LaunchPluginFile extends ModFile {

    private static final class DummyModLocator implements IModLocator {

        private static final DummyModLocator INSTANCE = new DummyModLocator();

        private DummyModLocator() {}

        @Nonnull
        @Override
        public List<IModFile> scanMods() {
            return Lists.newArrayList();
        }

        @Nonnull
        @Override
        public String name() {
            return "fermion.dummy";
        }

        @Nonnull
        @Override
        public Path findPath(@Nonnull final IModFile modFile, @Nonnull final String... path) {
            if (path.length == 1 && Objects.equals(path[0], "pack.mcmeta")) {
                return FMLLoader.getLoadingModList().getModFileById("fermion").getFile().findResource("dummy.mcmeta");
            } else {
                return Paths.get(path[0], path);
            }
        }

        @Override
        public void scanFile(@Nonnull final IModFile modFile, @Nonnull final Consumer<Path> pathConsumer) {}

        @Nonnull
        @Override
        public Optional<Manifest> findManifest(@Nonnull final Path file) {
            return Optional.empty();
        }

        @Override
        public void initArguments(@Nonnull final Map<String, ?> arguments) {}

        @Override
        public boolean isValid(@Nonnull final IModFile modFile) {
            return true;
        }
    }

    private IModInfo info;
    private IModFileInfo modFileInfo;

    public LaunchPluginFile() {
        super(null, DummyModLocator.INSTANCE);
    }

    public void setInfo(@Nonnull final IModInfo info) {
        this.info = info;
    }

    @Nonnull
    public IModInfo getInfo() {
        return this.info;
    }

    public void setModFileInfo(@Nonnull final IModFileInfo info) {
        this.modFileInfo = info;
    }

    @Override
    public void setFileProperties(Map<String, Object> fileProperties) {
        super.setFileProperties(fileProperties);
    }

    @Override
    public IModLanguageProvider getLoader() {
        return super.getLoader();
    }

    @Override
    public Path findResource(String className) {
        return super.findResource(className);
    }

    @Override
    public void identifyLanguage() {
        super.identifyLanguage();
    }

    @Override
    public Supplier<Map<String, Object>> getSubstitutionMap() {
        return super.getSubstitutionMap();
    }

    @Override
    public Type getType() {
        return super.getType();
    }

    @Override
    public Path getFilePath() {
        return super.getFilePath();
    }

    @Override
    public List<IModInfo> getModInfos() {
        return Lists.newArrayList(this.info);
    }

    @Override
    public Optional<Path> getAccessTransformer() {
        return Optional.empty();
    }

    @Override
    public boolean identifyMods() {
        return true;
    }

    @Override
    public List<CoreModFile> getCoreMods() {
        return Lists.newArrayList();
    }

    @Override
    public ModFileScanData compileContent() {
        return super.compileContent();
    }

    @Override
    public void scanFile(Consumer<Path> pathConsumer) {
        super.scanFile(pathConsumer);
    }

    @Override
    public void setFutureScanResult(CompletableFuture<ModFileScanData> future) {
        super.setFutureScanResult(future);
    }

    @Override
    public ModFileScanData getScanResult() {
        return new ModFileScanData();
    }

    @Override
    public void setScanResult(ModFileScanData modFileScanData, Throwable throwable) {
        super.setScanResult(modFileScanData, throwable);
    }

    @Override
    public String getFileName() {
        return "Fermion";
    }

    @Override
    public IModLocator getLocator() {
        return super.getLocator();
    }

    @Override
    public IModFileInfo getModFileInfo() {
        return this.modFileInfo;
    }
}
