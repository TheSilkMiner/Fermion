package net.thesilkminer.mc.fermion.companion;

import com.google.common.collect.Lists;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.CoreModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.jar.Manifest;

public final class LaunchPluginFile extends ModFile {

    private static final class DummyModLocator implements IModLocator {

        private static final DummyModLocator INSTANCE = new DummyModLocator();

        private IModFile fermionFile;

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
            if (this.fermionFile == null) {
                this.fermionFile = FMLLoader.getLoadingModList().getModFileById("fermion").getFile();
            }

            if (path.length == 1 && Objects.equals(path[0], "pack.mcmeta")) {
                return this.fermionFile.findResource("dummy.mcmeta");
            } else {
                return this.fermionFile.getLocator().findPath(this.fermionFile, path);
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
    public ModFileScanData getScanResult() {
        return new ModFileScanData();
    }

    @Override
    public String getFileName() {
        return "Fermion";
    }

    @Override
    public IModFileInfo getModFileInfo() {
        return this.modFileInfo;
    }

    @Override
    public Path getFilePath() {
        return FMLLoader.getLoadingModList().getModFileById("fermion").getFile().getFilePath();
    }
}
