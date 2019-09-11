package net.thesilkminer.mc.fermion;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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

    public Fermion() {
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info(MARKER, "Constructed");
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
