package net.thesilkminer.mc.fermion.asm.common;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("Fermion ASM Service")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions({"net.thesilkminer.mc.fermion.asm", "net.thesilkminer.mc.fermion.companion.asm"})
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
