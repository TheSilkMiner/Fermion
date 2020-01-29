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

package net.thesilkminer.mc.fermion.asm.common.utility;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

public final class Log {
    
    private final Logger logger;

    private Log(@Nonnull final String logName) {
        this.logger = LogManager.getLogger("Fermion ASM/" + logName);
    }

    @Nonnull
    public static Log of(@Nonnull final String name) {
        return new Log(Preconditions.checkNotNull(name));
    }

    public void d(@Nonnull final Object message) {
        this.logger.debug(message);
    }

    public void d(@Nonnull final Object message, @Nonnull final Throwable t) {
        this.logger.debug(message, t);
    }

    public void d(@Nonnull final String message) {
        this.logger.debug(message);
    }

    public void d(@Nonnull final String message, @Nonnull final Object... params) {
        this.logger.debug(message, params);
    }

    public void d(@Nonnull final String message, @Nonnull final Throwable t) {
        this.logger.debug(message, t);
    }

    public void e(@Nonnull final Object message) {
        this.logger.error(message);
    }

    public void e(@Nonnull final Object message, @Nonnull final Throwable t) {
        this.logger.error(message, t);
    }

    public void e(@Nonnull final String message) {
        this.logger.error(message);
    }

    public void e(@Nonnull final String message, @Nonnull final Object... params) {
        this.logger.error(message, params);
    }

    public void e(@Nonnull final String message, @Nonnull final Throwable t) {
        this.logger.error(message, t);
    }

    public void f(@Nonnull final Object message) {
        this.logger.fatal(message);
    }

    public void f(@Nonnull final Object message, @Nonnull final Throwable t) {
        this.logger.fatal(message, t);
    }

    public void f(@Nonnull final String message) {
        this.logger.fatal(message);
    }

    public void f(@Nonnull final String message, @Nonnull final Object... params) {
        this.logger.fatal(message, params);
    }

    public void f(@Nonnull final String message, @Nonnull final Throwable t) {
        this.logger.fatal(message, t);
    }

    public void i(@Nonnull final Object message) {
        this.logger.info(message);
    }

    public void i(@Nonnull final Object message, @Nonnull final Throwable t) {
        this.logger.info(message, t);
    }

    public void i(@Nonnull final String message) {
        this.logger.info(message);
    }

    public void i(@Nonnull final String message, @Nonnull final Object... params) {
        this.logger.info(message, params);
    }

    public void i(@Nonnull final String message, @Nonnull final Throwable t) {
        this.logger.info(message, t);
    }

    public void t(@Nonnull final Object message) {
        this.logger.trace(message);
    }

    public void t(@Nonnull final Object message, @Nonnull final Throwable t) {
        this.logger.trace(message, t);
    }

    public void t(@Nonnull final String message) {
        this.logger.trace(message);
    }

    public void t(@Nonnull final String message, @Nonnull final Object... params) {
        this.logger.trace(message, params);
    }

    public void t(@Nonnull final String message, @Nonnull final Throwable t) {
        this.logger.trace(message, t);
    }

    public void w(@Nonnull final Object message) {
        this.logger.warn(message);
    }

    public void w(@Nonnull final Object message, @Nonnull final Throwable t) {
        this.logger.warn(message, t);
    }

    public void w(@Nonnull final String message) {
        this.logger.warn(message);
    }

    public void w(@Nonnull final String message, @Nonnull final Object... params) {
        this.logger.warn(message, params);
    }

    public void w(@Nonnull final String message, @Nonnull final Throwable t) {
        this.logger.warn(message, t);
    }
}
