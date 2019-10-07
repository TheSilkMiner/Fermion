/*
 * Copyright (C) 2019  TheSilkMiner
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

package net.thesilkminer.mc.fermion.asm.prefab;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.thesilkminer.mc.fermion.asm.api.Environment;
import net.thesilkminer.mc.fermion.asm.api.IncompatibleEnvironmentException;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;
import net.thesilkminer.mc.fermion.asm.api.PluginMetadata;
import net.thesilkminer.mc.fermion.asm.api.transformer.Transformer;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

/**
 * This class provides a skeletal implementation of the {@link LaunchPlugin}
 * interface to minimize the efforts needed when implementing this interface
 * manually.
 *
 * <p>To create a valid Launch Plugin, the user should just extend this class
 * and implement the {@link #populateMetadata(PluginMetadata.Builder)} method.
 * The plugin ID should be passed through the constructor, and the list of
 * {@linkplain Transformer transformers} that need to be registered should be
 * populated in the constructor via calls to the non-overridable
 * {@link #registerTransformer(Transformer)} method.</p>
 *
 * <p>To ensure safety in the usage of this launch plugin implementation, most
 * of the methods have been marked as final, i.e. non-virtual and not
 * overridable. All the other methods that were either added or left to the
 * user to implement have documentation that describes their implementation in
 * detail in the respective section. Added methods are simply documented,
 * instead.</p>
 *
 * <p>It is highly suggested for clients of this library to extend this class
 * rather than implementing the raw {@code LaunchPlugin} interface manually.
 * This is just a suggestion and it is not mandatory. It is illegal for an
 * implementation to assume that any registered launch plugin is an instance
 * of this class or attempt to special case those that are.</p>
 *
 * @since 1.0.0
 */
public abstract class AbstractLaunchPlugin implements LaunchPlugin {

    private final String id;
    private final List<Transformer> transformers;

    /**
     * The logger instance that is provided by this skeletal implementation.
     *
     * <p>It is automatically initialized to the ID of this launch plugin.</p>
     *
     * @see Logger
     * @since 1.0.0
     */
    protected final Logger logger;

    /**
     * Constructs a new instance of this launch plugin skeletal implementation.
     *
     * @param id
     *      The ID of this launch plugin. Refer to
     *      {@link PluginMetadata.Builder#create(String)} for more information
     *      regarding the structure of the launch plugin ID.
     *
     * @since 1.0.0
     */
    protected AbstractLaunchPlugin(@Nonnull final String id) {
        this.id = id;
        this.transformers = Lists.newArrayList();
        this.logger = LogManager.getLogger(id);
    }

    @Nonnull
    @Override
    public final PluginMetadata getMetadata() {
        final PluginMetadata.Builder builder = PluginMetadata.Builder.create(this.id);
        this.populateMetadata(builder);
        return builder.build();
    }

    /**
     * Populates a {@link PluginMetadata} instance with all the data related to
     * this launch plugin.
     *
     * <p>Refer to {@link PluginMetadata.Builder}'s methods for more information
     * regarding which data is needed.</p>
     *
     * @param metadataBuilder
     *      The builder to use to construct the plugin metadata instance. It is
     *      never null.
     *
     * @since 1.0.0
     */
    protected abstract void populateMetadata(@Nonnull final PluginMetadata.Builder metadataBuilder);

    /**
     * {@inheritDoc}
     *
     * @implSpec
     *      This implementation by default logs a message on
     *      {@link org.apache.logging.log4j.Level#INFO INFO} level stating that
     *      the environment is compatible. Implementors don't need to call
     *      super. They'd rather overwrite this method completely in case
     *      checks are needed.
     *
     * @since 1.0.0
     */
    @Override
    public void validateEnvironment(@Nonnull final Environment environment) throws IncompatibleEnvironmentException {
        this.logger.info("Environment is valid for plugin '" + this.id + "': no particular requirements needed");
    }

    @Nonnull
    @Override
    public final Consumer<TransformerRegistry> getTransformerRegister() {
        return it -> this.transformers.forEach(it::registerTransformer);
    }

    /**
     * Adds a transformer to the list of transformers that will be registered
     * at the appropriate moment.
     *
     * @param transformer
     *      The transformer to add. It cannot be null.
     *
     * @since 1.0.0
     */
    protected final void registerTransformer(@Nonnull final Transformer transformer) {
        Preconditions.checkNotNull(transformer);
        this.transformers.add(transformer);
    }
}
