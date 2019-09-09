package net.thesilkminer.mc.fermion.companion.asm;

import net.thesilkminer.mc.fermion.asm.api.PluginMetadata;
import net.thesilkminer.mc.fermion.asm.prefab.AbstractLaunchPlugin;

import javax.annotation.Nonnull;

public final class FermionCoreCompanion extends AbstractLaunchPlugin {

    public FermionCoreCompanion() {
        super("fermion.asm");
    }

    @Override
    protected void populateMetadata(@Nonnull final PluginMetadata.Builder metadataBuilder) {
        metadataBuilder.setVersion("1")
                .setName("Fermion Companion")
                .addAuthor("TheSilkMiner")
                .setCredits("cpw, LexManos, FML, and the Forge guys")
                .setDescription("Core Mod part of Fermion. Responsible for all the edits that Fermion itself performs.\nWhich ones you may ask? Well, you're seeing this, aren't you?");
    }
}
