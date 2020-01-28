package net.thesilkminer.mc.fermion.test.asm;

import com.google.common.collect.ImmutableSet;
import net.thesilkminer.mc.fermion.asm.api.PluginMetadata;
import net.thesilkminer.mc.fermion.asm.prefab.AbstractLaunchPlugin;
import net.thesilkminer.mc.fermion.test.asm.transformer.TestHookingVanillaTransformer;
import net.thesilkminer.mc.fermion.test.asm.transformer.TestRuntimeFieldAccessTransformer;
import net.thesilkminer.mc.fermion.test.asm.transformer.TestRuntimeMethodAccessTransformer;
import net.thesilkminer.mc.fermion.test.asm.transformer.TestSingleTargetMethodTransformer;
import net.thesilkminer.mc.fermion.test.asm.transformer.TestTargetMethodTransformer;
import net.thesilkminer.mc.fermion.test.asm.transformer.TestTransformerAlwaysDisabled;

import javax.annotation.Nonnull;
import java.util.Set;

public final class FermionTestSuite extends AbstractLaunchPlugin {

    public FermionTestSuite() {
        super("fermion.asm.test");
        this.registerTransformers();
    }

    @Nonnull
    @Override
    public Set<String> getRootPackages() {
        return ImmutableSet.of("net.thesilkminer.mc.fermion.test.asm");
    }

    @Override
    protected void populateMetadata(@Nonnull final PluginMetadata.Builder metadataBuilder) {
        metadataBuilder.setVersion("1.0.1")
                .setName("Fermion Test Suite")
                .setLogoPath("fermion_test_suite_logo.png")
                .addAuthor("TheSilkMiner")
                .setCredits("cpw, LexManos, FML, and the Forge guys")
                .setDescription("Set of tests that check whether Fermion works without issues.\n" +
                        "Usually you want these to be disabled, but if you have problems, try to have a run with these.");
    }

    private void registerTransformers() {
        this.registerTransformer(new TestHookingVanillaTransformer(this));
        this.registerTransformer(new TestRuntimeFieldAccessTransformer(this));
        this.registerTransformer(new TestRuntimeMethodAccessTransformer(this));
        this.registerTransformer(new TestSingleTargetMethodTransformer(this));
        this.registerTransformer(new TestTargetMethodTransformer(this));
        this.registerTransformer(new TestTransformerAlwaysDisabled(this));
    }
}
