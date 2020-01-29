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

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Identifies the environment in which a launch plugin is currently running.
 *
 * <p>The environment is defined as the Launch Plugins that are currently
 * loaded and various properties that may or may not be present and/or may
 * be computed by other plugins on the fly.</p>
 *
 * @apiNote
 *      This interface has not been designed for implementation by
 *      <strong>clients</strong>. This means that you should not try to
 *      implement this interface in your own Launch Plugin. You should only
 *      rely on instances that are given to you via parameters or getters.
 *
 * @since 1.0.0
 */
public interface Environment {

    /**
     * Gets a list with all the IDs of the Launch Plugins that are currently
     * loaded.
     *
     * @return
     *      A {@link Collection} with all the IDs of the launch plugins that
     *      are currently loaded into the environment.
     *
     * @since 1.0.0
     */
    @Nonnull Collection<String> getLoadedLaunchPlugins();

    /**
     * Gets the value of the property identified by the passed in key, if
     * present.
     *
     * @param key
     *      The key whose associated value is to be found and then returned.
     *      It cannot be null.
     * @param <T>
     *      The type of the property that needs to be obtained.
     * @return
     *      An {@link Optional} containing the value of the property identified
     *      by the given key, if such a property is present. Otherwise
     *      {@link Optional#empty()}.
     * @throws ClassCastException
     *      If the property's value is not of the specified type.
     *
     * @since 1.0.0
     */
    @Nonnull <T> Optional<T> getProperty(@Nonnull final String key);

    /**
     * Attempts to get the value of the property identified by the passed in
     * key and returns that, if present, otherwise computes a new property,
     * stores it and returns the computed value.
     *
     * <p>Note that calling this method and ignoring the value does not
     * correspond to an addition of the property to the environment. Any other
     * Launch Plugin may or may not have added it previously to it, so no
     * guarantees can be done about the actual value of the property that is
     * stored under the given key.</p>
     *
     * @param key
     *      The key whose associated value is to be found and then returned.
     *      It cannot be null.
     * @param supplier
     *      A supplier that should be used to populate the given property
     *      in case it does not exist yet in the map. It cannot be null. It
     *      cannot return null.
     * @param <T>
     *      The type of the property that needs to be obtained.
     * @return
     *      The property requested, either the value that was already present
     *      or a newly computed one if none was found.
     * @throws ClassCastException
     *      If the property's value is not of the specified type.
     *
     * @since 1.0.0
     */
    @Nonnull <T> T computeIfNotPresent(@Nonnull final String key, @Nonnull final Supplier<T> supplier);

    /**
     * Checks whether a {@link LaunchPlugin} with the given ID exists and if so
     * it returns its instance.
     *
     * @param id
     *      The ID of the launch plugin to look up. It must not be null.
     * @return
     *      An {@link Optional} containing either the plugin instance that was
     *      found, if one was found, or nothing (i.e. {@link Optional#empty()})
     *      if no such instance was found.
     *
     * @since 1.0.0
     */
    @Nonnull Optional<LaunchPlugin> hasLaunchPlugin(@Nonnull final String id);
}
