//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
// ---
// nullability annotations and minor changes by TheSilkMiner
//

package net.thesilkminer.mc.fermion.asm.common.shade.net.minecraftforge.fml.loading;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

public final class FileUtils {
    private static final Logger LOGGER = LogManager.getLogger();

    @Nonnull
    @SuppressWarnings("UnusedReturnValue")
    public static Path getOrCreateDirectory(@Nonnull final Path dirPath, @Nonnull final String dirLabel) {
        if (!Files.isDirectory(dirPath.getParent())) {
            getOrCreateDirectory(dirPath.getParent(), "parent of " + dirLabel);
        }

        if (!Files.isDirectory(dirPath)) {
            LOGGER.debug(LogMarkers.CORE, "Making {} directory : {}", dirLabel, dirPath);

            try {
                Files.createDirectory(dirPath);
            } catch (IOException var3) {
                if (var3 instanceof FileAlreadyExistsException) {
                    LOGGER.fatal(LogMarkers.CORE, "Failed to create {} directory - there is a file in the way", dirLabel);
                } else {
                    LOGGER.fatal(LogMarkers.CORE, "Problem with creating {} directory (Permissions?)", dirLabel, var3);
                }

                throw new RuntimeException("Problem creating directory", var3);
            }

            LOGGER.debug(LogMarkers.CORE, "Created {} directory : {}", dirLabel, dirPath);
        } else {
            LOGGER.debug(LogMarkers.CORE, "Found existing {} directory : {}", dirLabel, dirPath);
        }

        return dirPath;
    }

    @Nonnull
    @SuppressWarnings("unused")
    public static String fileExtension(@Nonnull final Path path) {
        String fileName = path.getFileName().toString();
        int idx = fileName.lastIndexOf('.');
        return idx > -1 ? fileName.substring(idx + 1) : "";
    }
}
