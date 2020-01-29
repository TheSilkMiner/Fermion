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
                .setLogoPath("fermion_asm_service_logo.png")
                .setDescription("This is where the magic happens")
                .setCredits("cpw for creating ModLauncher, sp614x for Optifine and its Transformer idea")
                .addAuthor("RE/SYST")
                .setDisplayUrl("https://thesilkminer.net/mc-mods/fermion")
                .setVersion("1.0.1")
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
