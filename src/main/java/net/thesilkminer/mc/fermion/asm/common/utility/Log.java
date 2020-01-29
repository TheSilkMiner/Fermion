package net.thesilkminer.mc.fermion.asm.common.utility;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nonnull;

public final class Log {

    private static final Logger LOGGER = LogManager.getLogger("Fermion ASM");

    private final Marker marker;

    private Log(@Nonnull final String logName) {
        this.marker = MarkerManager.getMarker(logName);
    }

    @Nonnull
    public static Log of(@Nonnull final String name) {
        return new Log(Preconditions.checkNotNull(name));
    }

    public void d(@Nonnull final Object message) {
        LOGGER.debug(this.marker, message);
    }

    public void d(@Nonnull final Object message, @Nonnull final Throwable t) {
        LOGGER.debug(this.marker, message, t);
    }

    public void d(@Nonnull final String message) {
        LOGGER.debug(this.marker, message);
    }

    public void d(@Nonnull final String message, @Nonnull final Object... params) {
        LOGGER.debug(this.marker, message, params);
    }

    public void d(@Nonnull final String message, @Nonnull final Throwable t) {
        LOGGER.debug(this.marker, message, t);
    }

    public void e(@Nonnull final Object message) {
        LOGGER.error(this.marker, message);
    }

    public void e(@Nonnull final Object message, @Nonnull final Throwable t) {
        LOGGER.error(this.marker, message, t);
    }

    public void e(@Nonnull final String message) {
        LOGGER.error(this.marker, message);
    }

    public void e(@Nonnull final String message, @Nonnull final Object... params) {
        LOGGER.error(this.marker, message, params);
    }

    public void e(@Nonnull final String message, @Nonnull final Throwable t) {
        LOGGER.error(this.marker, message, t);
    }

    public void f(@Nonnull final Object message) {
        LOGGER.fatal(this.marker, message);
    }

    public void f(@Nonnull final Object message, @Nonnull final Throwable t) {
        LOGGER.fatal(this.marker, message, t);
    }

    public void f(@Nonnull final String message) {
        LOGGER.fatal(this.marker, message);
    }

    public void f(@Nonnull final String message, @Nonnull final Object... params) {
        LOGGER.fatal(this.marker, message, params);
    }

    public void f(@Nonnull final String message, @Nonnull final Throwable t) {
        LOGGER.fatal(this.marker, message, t);
    }

    public void i(@Nonnull final Object message) {
        LOGGER.info(this.marker, message);
    }

    public void i(@Nonnull final Object message, @Nonnull final Throwable t) {
        LOGGER.info(this.marker, message, t);
    }

    public void i(@Nonnull final String message) {
        LOGGER.info(this.marker, message);
    }

    public void i(@Nonnull final String message, @Nonnull final Object... params) {
        LOGGER.info(this.marker, message, params);
    }

    public void i(@Nonnull final String message, @Nonnull final Throwable t) {
        LOGGER.info(this.marker, message, t);
    }

    public void t(@Nonnull final Object message) {
        LOGGER.trace(this.marker, message);
    }

    public void t(@Nonnull final Object message, @Nonnull final Throwable t) {
        LOGGER.trace(this.marker, message, t);
    }

    public void t(@Nonnull final String message) {
        LOGGER.trace(this.marker, message);
    }

    public void t(@Nonnull final String message, @Nonnull final Object... params) {
        LOGGER.trace(this.marker, message, params);
    }

    public void t(@Nonnull final String message, @Nonnull final Throwable t) {
        LOGGER.trace(this.marker, message, t);
    }

    public void w(@Nonnull final Object message) {
        LOGGER.warn(this.marker, message);
    }

    public void w(@Nonnull final Object message, @Nonnull final Throwable t) {
        LOGGER.warn(this.marker, message, t);
    }

    public void w(@Nonnull final String message) {
        LOGGER.warn(this.marker, message);
    }

    public void w(@Nonnull final String message, @Nonnull final Object... params) {
        LOGGER.warn(this.marker, message, params);
    }

    public void w(@Nonnull final String message, @Nonnull final Throwable t) {
        LOGGER.warn(this.marker, message, t);
    }
}
