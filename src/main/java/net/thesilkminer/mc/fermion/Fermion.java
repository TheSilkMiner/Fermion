package net.thesilkminer.mc.fermion;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.StartupMessageManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.thesilkminer.mc.fermion.hook.OtherClassHook;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nonnull;

@Mod("fermion")
public final class Fermion {

    private static final Logger LOGGER = LogManager.getLogger("Fermion");
    private static final Marker MARKER = MarkerManager.getMarker("Mod Loading");
    @SuppressWarnings("unused") // Used for transformation tests
    private static final Marker TRANSFORMER_MARKER = MarkerManager.getMarker("TRANSFORMED MOD LOADING");
    private static final Marker CHECK_MARKER = MarkerManager.getMarker("OtherClass Checks");

    public Fermion() {
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info(MARKER, "Constructed");
        StartupMessageManager.addModMessage("Ahoy there! Fermion is here!");
    }

    @SubscribeEvent
    public void newRegistry(@Nonnull final RegistryEvent.NewRegistry registry) {
        // Mainly so that we have a different event to do our manipulations in

        // Init parameters
        new OtherClass("");
        new OtherClass("");
        final OtherClass otherClass = new OtherClass("Assuming a is bills");

        // Expected
        @SuppressWarnings("SpellCheckingInspection")
        final String expectedParameter = "$ssuming $ is bills";
        final int expectedId = 3;
        final String expectedPrinted = otherClass.getPrinted();
        final int expectedIdThroughMethod = 3;

        // Let's check
        final String parameterOfOtherClass = OtherClassHook.getParameter(otherClass);
        final int idOfOtherClass = OtherClassHook.getId();
        final String printedOfOtherClass = OtherClassHook.print(otherClass, "Assuming a is bills");
        final int idOfOtherClassThroughMethod = OtherClassHook.getIdThroughMethod(new Object());

        boolean allMatches = expectedParameter.equals(parameterOfOtherClass)
                && expectedId == idOfOtherClass
                && expectedPrinted.equals(printedOfOtherClass)
                && expectedIdThroughMethod == idOfOtherClassThroughMethod;

        if (allMatches) {
            LOGGER.info(CHECK_MARKER, "Successfully transformed OtherClassHook: it works");
        } else {
            LOGGER.warn(CHECK_MARKER, "Attempted to get parameter of otherClass: expected '" + expectedParameter + "'; found '" + parameterOfOtherClass + "'");
            LOGGER.warn(CHECK_MARKER, "Attempted to get ID of otherClass: expected " + expectedId + "; found " + idOfOtherClass);
            LOGGER.warn(CHECK_MARKER, "Attempted to call 'print' of otherClass: expected '" + expectedPrinted + "'; found '" + printedOfOtherClass + "'");
            LOGGER.warn(CHECK_MARKER, "Attempted to call 'getId' of otherClass: expected " + expectedIdThroughMethod + "; found " + idOfOtherClassThroughMethod);
            LOGGER.warn(CHECK_MARKER, "If you've disabled the transformer, that's okay. If you haven't, report the issue on our GitHub!");
        }
    }

    @SubscribeEvent
    public void setup(@Nonnull final FMLCommonSetupEvent event) {
        LOGGER.info(MARKER, "FMLCommonSetupEvent");
    }

    @SubscribeEvent
    public void clientSetup(@Nonnull final FMLClientSetupEvent event) {
        LOGGER.info(MARKER, "FMLClientSetupEvent");
    }

    @SubscribeEvent
    public void onServerStarting(@Nonnull final FMLServerStartingEvent event) {
        LOGGER.info(MARKER, "FMLServerStartingEvent");
    }
}
