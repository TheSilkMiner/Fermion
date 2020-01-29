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

package net.thesilkminer.mc.fermion.companion;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.thesilkminer.mc.fermion.asm.api.PluginMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public final class LaunchPluginContainer extends DummyModContainer {

    private static final Logger LOGGER = LogManager.getLogger("LaunchPluginContainer");

    private final List<String> ownedPackages;

    public LaunchPluginContainer(@Nonnull final PluginMetadata metadata, @Nonnull final List<String> ownedPackages) {
        super(cast(metadata));
        this.ownedPackages = ImmutableList.copyOf(ownedPackages);
        LOGGER.info("Generated container '" + this + "'");
    }

    @Nonnull
    private static ModMetadata cast(@Nonnull final PluginMetadata metadata) {
        LOGGER.debug("Attempting to cast to ModMetadata the plugin metadata: " + metadata);

        final ModMetadata instance = new ModMetadata();
        instance.authorList = cast(metadata.getAuthors());
        instance.autogenerated = false;
        if (metadata.getCredits() != null) instance.credits = metadata.getCredits();
        if (metadata.getDescription() != null) instance.description = metadata.getDescription();
        if (metadata.getLogo() != null) instance.logoFile = metadata.getLogo().startsWith("/")? metadata.getLogo() : ("/" + metadata.getLogo());
        instance.modId = metadata.getId();
        instance.name = metadata.getName();
        if (metadata.getUrl() != null) instance.url = metadata.getUrl();
        instance.version = metadata.getVersion().toString();
        return instance;
    }

    @Nonnull
    private static List<String> cast(@Nonnull final List<PluginMetadata.Author> authors) {
        return authors.stream().map(PluginMetadata.Author::getName).collect(Collectors.toList());
    }

    @Override
    public boolean registerBus(@Nonnull @SuppressWarnings("UnstableApiUsage") final EventBus bus, @Nonnull final LoadController controller) {
        return true;
    }

    @Override
    public List<String> getOwnedPackages() {
        return this.ownedPackages;
    }
}
