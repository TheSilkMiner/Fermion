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

package net.thesilkminer.mc.fermion;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.thesilkminer.mc.fermion.hook.OtherClassHook;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

@Mod(modid = "fermion", name = "Fermion", version = "@VERSION@", dependencies = "required-after:forge@[14.23.5.2768,);",
        acceptedMinecraftVersions = "1.12.2", certificateFingerprint = "@FINGERPRINT@")
@Mod.EventBusSubscriber(modid = "fermion")
public final class Fermion {

    private static final Logger LOGGER = LogManager.getLogger("Fermion/Mod Loading");
    @SuppressWarnings("unused") // Used for transformation tests
    private static final Logger TRANSFORMER_LOGGER = LogManager.getLogger("Fermion/TRANSFORMED MOD LOADING");
    private static final Logger CHECK_LOGGER = LogManager.getLogger("Fermion/OtherClass Checks");

    public Fermion() {
        LOGGER.info("Class constructed");
    }

    @Mod.EventHandler
    public void onConstruction(@Nonnull final FMLConstructionEvent event) {
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
            CHECK_LOGGER.info("Successfully transformed OtherClassHook: it works");
        } else {
            CHECK_LOGGER.warn("Attempted to get parameter of otherClass: expected '" + expectedParameter + "'; found '" + parameterOfOtherClass + "'");
            CHECK_LOGGER.warn("Attempted to get ID of otherClass: expected " + expectedId + "; found " + idOfOtherClass);
            CHECK_LOGGER.warn("Attempted to call 'print' of otherClass: expected '" + expectedPrinted + "'; found '" + printedOfOtherClass + "'");
            CHECK_LOGGER.warn("Attempted to call 'getId' of otherClass: expected " + expectedIdThroughMethod + "; found " + idOfOtherClassThroughMethod);
            CHECK_LOGGER.warn("If you've disabled the testing transformer, that's okay. If you haven't, report the issue on our GitHub!");
        }
    }

    @Mod.EventHandler
    public void onPreInitialization(@Nonnull final FMLPreInitializationEvent event) {
        LOGGER.info("FMLPreInitializationEvent");
    }

    @Mod.EventHandler
    public void onInitialization(@Nonnull final FMLInitializationEvent event) {
        LOGGER.info("FMLInitializationEvent");
    }

    @Mod.EventHandler
    public void onPostInitialization(@Nonnull final FMLPostInitializationEvent event) {
        LOGGER.info("FMLPostInitializationEvent");
    }
}
