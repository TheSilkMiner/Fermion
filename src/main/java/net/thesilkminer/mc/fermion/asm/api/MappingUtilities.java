/*
 * Copyright (C) 2020  TheSilkMiner
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

import net.thesilkminer.mc.fermion.asm.common.utility.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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

    // Remapping in Forge 1.12.2 and lower is weird: it goes the opposite direction for some reason
    // or at least this is what it looks like.

    private final Log L = Log.of("Mapping Utilities");

    private final Object target;
    private final Field rawFieldMaps;
    private final Field rawMethodMaps;

    @SuppressWarnings("SpellCheckingInspection")
    MappingUtilities() {
        /*mutable*/ Field rawFieldMaps;
        /*mutable*/ Field rawMethodMaps;
        /*mutable*/ Object target;
        try {
            final Class<?> clazz = Class.forName("net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper");
            final Field instance = clazz.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            target = instance.get(clazz);
            rawFieldMaps = clazz.getDeclaredField("rawFieldMaps");
            rawFieldMaps.setAccessible(true);
            rawMethodMaps = clazz.getDeclaredField("rawMethodMaps");
            rawMethodMaps.setAccessible(true);
        } catch (@Nonnull final ReflectiveOperationException e) {
            rawFieldMaps = null;
            rawMethodMaps = null;
            target = null;
            L.e("Unable to retrieve renaming maps: this is a serious error! Remapping won't be available!", e);
        }
        this.rawFieldMaps = rawFieldMaps;
        this.rawMethodMaps = rawMethodMaps;
        this.target = target;
    }

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
        if (this.rawMethodMaps == null) {
            L.w("Unable to remap method name '" + name + "': no remapping tables loaded");
            return name;
        }
        final String mappedName = this.findMcpFromSrg(this.rawMethodMaps, name);
        return mappedName == null? name : mappedName;
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
        if (this.rawFieldMaps == null) {
            L.w("Unable to remap field name '" + name + "': no remapping tables loaded");
            return name;
        }
        final String mappedName = this.findMcpFromSrg(this.rawFieldMaps, name);
        return mappedName == null? name : mappedName;
    }

    @Nullable
    private String findMcpFromSrg(@Nonnull final Field from, @Nonnull final String name) {
        final Map<String, Map<String, String>> map = this.fieldToMap(from);
        if (map == null) return null;
        return map.values().stream()
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .filter(it -> this.isValidName(it.getKey(), name, from == this.rawFieldMaps))
                .map(Map.Entry::getValue)
                .findAny()
                .orElse(null);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private Map<String, Map<String, String>> fieldToMap(@Nonnull final Field from) {
        try {
            from.setAccessible(true);
            return (Map<String, Map<String, String>>) from.get(this.target);
        } catch (@Nonnull final ReflectiveOperationException | ClassCastException e) {
            L.e("Unable to reflectively obtain map from given fields! This is serious: remapping won't be available", e);
            return null;
        }
    }

    private boolean isValidName(@Nonnull final String name, @Nonnull final String target, final boolean parseField) {
        return parseField? this.isValidFieldName(name, target) : this.isValidMethodName(name, target);
    }

    private boolean isValidFieldName(@Nonnull final String name, @Nonnull final String target) {
        final String[] parts = name.split(Pattern.quote(":"));
        if (parts.length != 2) throw new IllegalStateException("Invalid maps loaded: " + name + " does not respect the correct format");
        return target.equals(parts[0]);
    }

    private boolean isValidMethodName(@Nonnull final String name, @Nonnull final String target) {
        final String[] parts = name.split(Pattern.quote("("));
        if (parts.length < 1) throw new IllegalStateException("Invalid maps loaded: " + name + " does not respect the correct format");
        return target.equals(parts[0]);
    }
}
