//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
// ---
// nullability annotations and minor changes by TheSilkMiner
//

package net.thesilkminer.mc.fermion.asm.common.shade.net.minecraftforge.fml.loading;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

@SuppressWarnings("SpellCheckingInspection")
public class LogMarkers {
    public static final Marker CORE = MarkerManager.getMarker("CORE");
    public static final Marker LOADING = MarkerManager.getMarker("LOADING");
    public static final Marker SCAN = MarkerManager.getMarker("SCAN");
    public static final Marker SPLASH = MarkerManager.getMarker("SPLASH");
    public static final Marker FORGEMOD;

    static {
        FORGEMOD = MarkerManager.getMarker("FORGEMOD").addParents(LOADING);
    }
}
