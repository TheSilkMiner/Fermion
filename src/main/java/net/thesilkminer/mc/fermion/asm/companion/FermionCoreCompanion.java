package net.thesilkminer.mc.fermion.asm.companion;

import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;
import net.thesilkminer.mc.fermion.asm.api.PluginMetadata;

import javax.annotation.Nonnull;

public final class FermionCoreCompanion implements LaunchPlugin {
    @Nonnull
    @Override
    public PluginMetadata getMetadata() {
        return PluginMetadata.Builder.create("fermion.asm")
                .setVersion("1")
                .setName("Fermion Companion")
                .addAuthor("TheSilkMiner")
                .setCredits("cpw, LexManos, FML, and the Forge guys")
                .setDescription("Core Mod part of Fermion. Responsible for all the edits that Fermion itself performs.\nWhich ones you may ask? Well, you're seeing this, aren't you?")
                .build();
    }
}
