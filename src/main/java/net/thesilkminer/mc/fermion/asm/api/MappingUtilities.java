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

package net.thesilkminer.mc.fermion.asm.api;

import javax.annotation.Nonnull;

/**
 * Collection of utilities used to map names from their obfuscated to their
 * de-obfuscated counterpart in case its needed.
 *
 * @since 1.0.0
 */
public enum MappingUtilities {
    /**
     * The unique instance of these utility class.
     *
     * @since 1.0.0
     */
    INSTANCE;

    // Mapping utilities is useless pre-1.13, because Fermion runs in an already
    // de-obfuscated, MCP-named environment.

    /**
     * Maps the given obfuscated method name to its de-obfuscated counterpart
     * if needed.
     *
     * @param name
     *      The name of the method to map. It cannot be null.
     * @return
     *      The mapped name, if needed, otherwise the same name. Guaranteed to
     *      be not-null.
     *
     * @since 1.0.0
     */
    @Nonnull
    public String mapMethod(@Nonnull final String name) {
        return name;
    }

    /**
     * Maps the given obfuscated field name to its de-obfuscated counterpart if
     * needed.
     *
     * @param name
     *      The name of the field to map. It cannot be null.
     * @return
     *      The mapped name, if needed, otherwise the same name. Guaranteed to
     *      be not-null.
     *
     * @since 1.0.0
     */
    @Nonnull
    public String mapField(@Nonnull final String name) {
        return name;
    }
}
