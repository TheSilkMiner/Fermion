package net.thesilkminer.mc.fermion.hook;

import net.thesilkminer.mc.fermion.OtherClass;
import net.thesilkminer.mc.fermion.api.TransformingUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nonnull;

public final class OtherClassHook {

    private static final Logger LOGGER = LogManager.getLogger("Fermion");
    private static final Marker MARKER = MarkerManager.getMarker("OtherClassHook");

    static {
        LOGGER.info(MARKER, "Was OtherClassHook transformed (through class)? " + TransformingUtilities.wasTransformed(OtherClassHook.class));
        LOGGER.info(MARKER, "Was Object transformed (through object)? " + TransformingUtilities.wasTransformed(new Object()));
        LOGGER.info(MARKER, "Was ModList transformed (through string)? " + TransformingUtilities.wasTransformed("net.minecraftforge.fml.ModList"));
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
