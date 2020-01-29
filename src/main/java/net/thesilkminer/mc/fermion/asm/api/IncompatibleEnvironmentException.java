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
import javax.annotation.Nullable;

/**
 * Thrown when an incompatible environment is found by at least one of the
 * launch plugin.
 *
 * @since 1.0.0
 */
public final class IncompatibleEnvironmentException extends Exception {

    /**
     * Constructs a new instance of this exception with the given message and
     * the given cause.
     *
     * @param message
     *      The detail message. It should detail exactly why the environment is
     *      incompatible and indicate a possible resolution. It must not be
     *      null.
     * @param cause
     *      The exception that caused the environment to be incompatible. It
     *      can be null.
     *
     * @since 1.0.0
     */
    public IncompatibleEnvironmentException(@Nonnull final String message, @Nullable final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new instance of this exception with the given cause and
     * the same message as the supplied cause.
     *
     * @param cause
     *      The exception that caused the environment to be incompatible. It
     *      cannot be null.
     *
     * @since 1.0.0
     */
    public IncompatibleEnvironmentException(@Nonnull final Throwable cause) {
        this(cause.getMessage(), cause);
    }

    /**
     * Constructs a new instance of this exception with the given message.
     *
     * @param message
     *      The detail message. It should detail exactly why the environment is
     *      incompatible and indicate a possible resolution. It must not be
     *      null.
     *
     * @since 1.0.0
     */
    public IncompatibleEnvironmentException(@Nonnull final String message) {
        this(message, null);
    }
}
