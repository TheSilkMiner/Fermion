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

package net.thesilkminer.mc.fermion.companion.asm;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.thesilkminer.mc.fermion.asm.api.Environment;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;
import net.thesilkminer.mc.fermion.asm.api.PluginMetadata;
import net.thesilkminer.mc.fermion.asm.api.transformer.Transformer;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerRegistry;
import net.thesilkminer.mc.fermion.asm.prefab.AbstractLaunchPlugin;
import net.thesilkminer.mc.fermion.companion.asm.transformer.ModDiscovererTransformer;
import net.thesilkminer.mc.fermion.companion.asm.transformer.TransformingUtilitiesTransformer;
import net.thesilkminer.mc.fermion.companion.asm.utility.DummyFermionAsmLaunchPlugin;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FermionCoreCompanion extends AbstractLaunchPlugin {

    public FermionCoreCompanion() {
        super("fermion.asm");
        this.registerTransformers();
    }

    @Nonnull
    @Override
    public Set<String> getRootPackages() {
        return ImmutableSet.of("net.thesilkminer.mc.fermion.companion.asm");
    }

    @Override
    protected void populateMetadata(@Nonnull final PluginMetadata.Builder metadataBuilder) {
        metadataBuilder.setVersion("1.0.2+hotfix")
                .setName("Fermion Companion")
                .setLogoPath("fermion_logo.png")
                .addAuthor("TheSilkMiner")
                .setCredits("cpw, LexManos, FML, and the Forge guys")
                .setDescription("Launch Plugin part of Fermion. Responsible for all the edits that Fermion itself performs.\nWhich ones you may ask? Well, you're seeing this, aren't you?");
    }

    private void registerTransformers() {
        /* Actual transformers */
        this.registerTransformer(new ModDiscovererTransformer(this));
        this.registerTransformer(new TransformingUtilitiesTransformer());
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

            final List<LaunchPlugin> pluginsList = Lists.newArrayList();
            pluginsList.add(new DummyFermionAsmLaunchPlugin());
            transformerMap.values().stream().map(Pair::getValue).forEach(pluginsList::add);

            ModDiscovererTransformer.launchPlugins = pluginsList;
            ModDiscovererTransformer.transformers = Lists.newArrayList(transformersMap.keySet());
            ModDiscovererTransformer.registry = registry;
        } catch (@Nonnull final ReflectiveOperationException e) {
            e.printStackTrace(System.err);
        }
    }
}
