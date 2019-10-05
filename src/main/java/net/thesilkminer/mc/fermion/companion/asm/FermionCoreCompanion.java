package net.thesilkminer.mc.fermion.companion.asm;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.thesilkminer.mc.fermion.asm.api.Environment;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;
import net.thesilkminer.mc.fermion.asm.api.PluginMetadata;
import net.thesilkminer.mc.fermion.asm.api.transformer.Transformer;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerRegistry;
import net.thesilkminer.mc.fermion.asm.prefab.AbstractLaunchPlugin;
import net.thesilkminer.mc.fermion.companion.asm.transformer.ModListTransformer;
import net.thesilkminer.mc.fermion.companion.asm.transformer.ModLoaderTransformer;
import net.thesilkminer.mc.fermion.companion.asm.transformer.TransformingUtilitiesTransformer;
import net.thesilkminer.mc.fermion.companion.asm.transformer.test.TestHookingVanillaTransformer;
import net.thesilkminer.mc.fermion.companion.asm.transformer.test.TestRuntimeFieldAccessTransformer;
import net.thesilkminer.mc.fermion.companion.asm.transformer.test.TestRuntimeMethodAccessTransformer;
import net.thesilkminer.mc.fermion.companion.asm.transformer.test.TestSingleTargetMethodTransformer;
import net.thesilkminer.mc.fermion.companion.asm.transformer.test.TestTargetMethodTransformer;
import net.thesilkminer.mc.fermion.companion.asm.transformer.test.TestTransformerAlwaysDisabled;
import net.thesilkminer.mc.fermion.companion.asm.transformer.vanity.BackToSingleThreadsTransformer;
import net.thesilkminer.mc.fermion.companion.asm.transformer.vanity.StartupMessagesColorizerTransformer;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FermionCoreCompanion extends AbstractLaunchPlugin {

    public FermionCoreCompanion() {
        super("fermion.asm");
        //this.registerTransformers();
    }

    @Nonnull
    @Override
    public Set<String> getRootPackages() {
        return ImmutableSet.of("net.thesilkminer.mc.fermion");
    }

    @Override
    protected void populateMetadata(@Nonnull final PluginMetadata.Builder metadataBuilder) {
        metadataBuilder.setVersion("1.0.0")
                .setName("Fermion Companion")
                .addAuthor("TheSilkMiner")
                .setCredits("cpw, LexManos, FML, and the Forge guys")
                .setDescription("Launch Plugin part of Fermion. Responsible for all the edits that Fermion itself performs.\nWhich ones you may ask? Well, you're seeing this, aren't you?");
    }

    private void registerTransformers() {
        /* Actual transformers */
        this.registerTransformer(new ModLoaderTransformer());
        this.registerTransformer(new ModListTransformer());
        this.registerTransformer(new TransformingUtilitiesTransformer());

        /* Vanity transformers */
        this.registerTransformer(new BackToSingleThreadsTransformer());
        this.registerTransformer(new StartupMessagesColorizerTransformer());

        /* Test transformers */
        this.registerTransformer(new TestHookingVanillaTransformer());
        this.registerTransformer(new TestSingleTargetMethodTransformer());
        this.registerTransformer(new TestTargetMethodTransformer());
        this.registerTransformer(new TestRuntimeFieldAccessTransformer());
        this.registerTransformer(new TestRuntimeMethodAccessTransformer());
        this.registerTransformer(new TestTransformerAlwaysDisabled(this));
    }

    @Override
    public void onPostTransformersRegistration(@Nonnull final Environment environment, @Nonnull final TransformerRegistry registry) {
        // NOTE FOR USERS: This is a huge hack, you should not attempt to replicate this in your own
        // launch plugin. Also, this is part of the "being a companion" for the Fermion Transformer.
        // You are not a companion: don't try to do it!

        try {
            final Class<?> launchBlackboardClass = Class.forName("net.thesilkminer.mc.fermion.asm.common.utility.LaunchBlackboard");
            final Field pluginsMap = launchBlackboardClass.getDeclaredField("pluginsMap");
            pluginsMap.setAccessible(true);
            @SuppressWarnings("unchecked")
            final Map<String, Pair<PluginMetadata, LaunchPlugin>> transformerMap =
                    (Map<String, Pair<PluginMetadata, LaunchPlugin>>) pluginsMap.get(registry);

            final Field transformers = launchBlackboardClass.getDeclaredField("transformers");
            transformers.setAccessible(true);
            @SuppressWarnings("unchecked")
            final Map<String, Transformer> transformersMap =
                    (Map<String, Transformer>) transformers.get(registry);

            final List<PluginMetadata> dataList = Lists.newArrayList();
            dataList.add(
                    PluginMetadata.Builder.create("fermion.asm.service")
                            .setName("Fermion ASM Service")
                            .setDescription("This is where the magic happens")
                            .setCredits("cpw for creating ModLauncher, sp614x for Optifine and its Transformer idea")
                            .addAuthor("RE/SYST")
                            .setDisplayUrl("https://thesilkminer.net/mc-mods/fermion")
                            .setVersion("1.0.0")
                            .build()
            );
            transformerMap.values().stream().map(Pair::getKey).forEach(dataList::add);

            //ModListTransformer.pluginMetadataList = dataList;
            //ModListTransformer.transformers = Lists.newArrayList(transformersMap.keySet());
            //ModListTransformer.registry = registry;
        } catch (@Nonnull final ReflectiveOperationException e) {
            e.printStackTrace(System.err);
        }
    }
}
