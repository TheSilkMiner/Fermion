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

package net.thesilkminer.mc.fermion.asm.common;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("Fermion ASM Service")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions({"net.thesilkminer.mc.fermion.asm", "net.thesilkminer.mc.fermion.companion.asm", "cpw.mods.gross"})
public final class FermionPlugin implements IFMLLoadingPlugin {

    static Map<String, Object> injectedData;

    @Nonnull
    @Override
    public String[] getASMTransformerClass() {
        return new String[] { "net.thesilkminer.mc.fermion.asm.common.FermionTransformer" };
    }

    // Handled through the Transformer
    @Nullable
    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return "net.thesilkminer.mc.fermion.asm.common.FermionTransformationSetup";
    }

    @Override
    public void injectData(@Nonnull final Map<String, Object> data) {
        injectedData = data;
    }

    @Nullable
    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
