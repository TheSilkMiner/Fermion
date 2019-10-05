package net.thesilkminer.mc.fermion.companion.hook;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public final class PotionEffectHook {
    private static final Logger LOGGER = LogManager.getLogger("PotionEffectHook");

    public static void logTest(@Nonnull final Logger logger) {
        LOGGER.info("Hey there! PotionEffect has the following logger: " + logger);
    }
}
