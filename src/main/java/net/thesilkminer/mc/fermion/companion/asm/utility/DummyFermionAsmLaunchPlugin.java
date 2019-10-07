package net.thesilkminer.mc.fermion.companion.asm.utility;

import com.google.common.collect.ImmutableSet;
import net.thesilkminer.mc.fermion.asm.api.Environment;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;
import net.thesilkminer.mc.fermion.asm.api.PluginMetadata;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerRegistry;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.Consumer;

public final class DummyFermionAsmLaunchPlugin implements LaunchPlugin {

    @Nonnull
    @Override
    public PluginMetadata getMetadata() {
        return PluginMetadata.Builder.create("fermion.asm.service")
                .setName("Fermion ASM Service")
                .setDescription("This is where the magic happens")
                .setCredits("cpw for creating ModLauncher, sp614x for Optifine and its Transformer idea")
                .addAuthor("RE/SYST")
                .setDisplayUrl("https://thesilkminer.net/mc-mods/fermion")
                .setVersion("1.0.0")
                .build();
    }

    @Nonnull
    @Override
    public Set<String> getRootPackages() {
        return ImmutableSet.of("net.thesilkminer.mc.fermion.asm");
    }

    @Override
    public void validateEnvironment(@Nonnull final Environment environment) {}

    @Nonnull
    @Override
    public Consumer<TransformerRegistry> getTransformerRegister() {
        return it -> {};
    }
}
