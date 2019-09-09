package net.thesilkminer.mc.fermion.asm.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class IncompatibleEnvironmentException extends Exception {
    public IncompatibleEnvironmentException(@Nonnull final String message, @Nullable final Throwable cause) {
        super(message, cause);
    }
    public IncompatibleEnvironmentException(@Nonnull final Throwable cause) {
        this(cause.getMessage(), cause);
    }
    public IncompatibleEnvironmentException(@Nonnull final String message) {
        this(message, null);
    }
}
