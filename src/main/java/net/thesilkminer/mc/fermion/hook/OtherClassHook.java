package net.thesilkminer.mc.fermion.hook;

import net.thesilkminer.mc.fermion.OtherClass;
import net.thesilkminer.mc.fermion.api.TransformingUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

public final class OtherClassHook {

    private static final Logger LOGGER = LogManager.getLogger("Fermion/OtherClassHook");

    static {
        LOGGER.info("Was OtherClassHook transformed (through class)? " + TransformingUtilities.wasTransformed(OtherClassHook.class));
        LOGGER.info("Was Object transformed (through object)? " + TransformingUtilities.wasTransformed(new Object()));
        LOGGER.info("Was Loader transformed (through string)? " + TransformingUtilities.wasTransformed("net.minecraftforge.fml.common.Loader"));
    }

    @Nonnull
    public static String getParameter(@Nonnull final OtherClass instance) {
        return "";
    }

    public static int getId() {
        return -1;
    }

    @Nonnull
    public static String print(@Nonnull final OtherClass instance, @Nonnull final String marker) {
        return "";
    }

    public static int getIdThroughMethod(@Nonnull final Object object) {
        return -1;
    }
}
