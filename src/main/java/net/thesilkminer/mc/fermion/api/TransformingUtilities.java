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

package net.thesilkminer.mc.fermion.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;

/**
 * Collection of utilities used to check whether the classes were transformed
 * or not during a previous pass of one of the Fermion Launch Plugins.
 *
 * @since 1.0.0
 */
public enum TransformingUtilities {
    ;

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    private static String transformedFieldName() {
        return null;
    }

    private static boolean wasTransformed(@Nullable final Class<?> clazz,
                                          @SuppressWarnings({"SameParameterValue", "unused"}) @Nullable final Object marker) {
        try {
            if (clazz == null) return false;
            final Field field = clazz.getDeclaredField(transformedFieldName());
            return field.isSynthetic();
        } catch (final ReflectiveOperationException e) {
            return false;
        }
    }

    @Nullable
    private static Class<?> forName(@Nonnull final String name) {
        try {
            return Class.forName(name);
        } catch (@Nonnull final ReflectiveOperationException e) {
            return null;
        }
    }

    /**
     * Gets whether this class was transformed by at least one enabled Fermion
     * Launch Plugin.
     *
     * @param clazz
     *      The Class to check.
     * @return
     *      Whether this class was transformed by at least one enabled Fermion
     *      Launch Plugin.
     *
     * @see #wasTransformed(String)
     * @see #wasTransformed(Object)
     * @since 1.0.0
     */
    public static boolean wasTransformed(@Nonnull final Class<?> clazz) {
        return wasTransformed(clazz, null);
    }

    /**
     * Gets whether the class of which the given object is an instance was
     * transformed by at least one enabled Fermion Launch Plugin.
     *
     * @param object
     *      An instance of the class that needs to be checked.
     * @return
     *      Whether the class of which the given object in an instance was
     *      transformed by at least one enabled Fermion Launch Plugin.
     *
     * @see #wasTransformed(String)
     * @see #wasTransformed(Class)
     * @since 1.0.0
     */
    public static boolean wasTransformed(@Nonnull final Object object) {
        return wasTransformed(object.getClass());
    }

    /**
     * Gets whether the class identified by the given name was transformed by
     * at least one enabled Fermion Launch Plugin.
     *
     * @param string
     *      The name of the class to check.
     * @return
     *      Whether the class identified by the given name was transformed by
     *      at least one enabled Fermion Launch Plugin, or {@code false} if no
     *      class with the given name could be found.
     *
     * @see #wasTransformed(Object)
     * @see #wasTransformed(Class)
     * @since 1.0.0
     */
    public static boolean wasTransformed(@Nonnull final String string) {
        return wasTransformed(forName(string), null);
    }
}
