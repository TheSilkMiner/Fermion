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
