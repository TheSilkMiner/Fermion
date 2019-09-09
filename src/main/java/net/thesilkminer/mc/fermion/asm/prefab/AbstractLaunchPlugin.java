package net.thesilkminer.mc.fermion.asm.prefab;

import net.thesilkminer.mc.fermion.asm.api.Environment;
import net.thesilkminer.mc.fermion.asm.api.IncompatibleEnvironmentException;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;
import net.thesilkminer.mc.fermion.asm.api.PluginMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nonnull;

public abstract class AbstractLaunchPlugin implements LaunchPlugin {

    private final String id;
    protected final Logger logger;

    protected AbstractLaunchPlugin(@Nonnull final String id) {
        this.id = id;
        this.logger = LogManager.getLogger(id);
    }

    @Nonnull
    @Override
    public final PluginMetadata getMetadata() {
        final PluginMetadata.Builder builder = PluginMetadata.Builder.create(this.id);
        this.populateMetadata(builder);
        return builder.build();
    }

    protected abstract void populateMetadata(@Nonnull final PluginMetadata.Builder metadataBuilder);

    @Override
    public void validateEnvironment(@Nonnull final Environment environment) throws IncompatibleEnvironmentException {
        this.logger.info(MarkerManager.getMarker("Environmental check"), "Environment is valid for plugin '" + this.id + "': no particular requirements needed");
    }
}
