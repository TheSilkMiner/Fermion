package net.thesilkminer.mc.fermion.companion.asm.hook;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public final class EffectInstanceHook {
    private static final Logger LOGGER = LogManager.getLogger("EffectInstanceHook");

    public static void logTest(@Nonnull final Logger logger) {
        LOGGER.info("Hey there! EffectInstance has the following logger: " + logger);
    }
}
