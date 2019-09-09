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

public abstract class AbstractLaunchPlugin implements LaunchPlugin {

    private final String id;
    private final List<Transformer> transformers;
    protected final Logger logger;

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

    protected abstract void populateMetadata(@Nonnull final PluginMetadata.Builder metadataBuilder);

    @Override
    public void validateEnvironment(@Nonnull final Environment environment) throws IncompatibleEnvironmentException {
        this.logger.info("Environment is valid for plugin '" + this.id + "': no particular requirements needed");
    }

    @Nonnull
    @Override
    public final Consumer<TransformerRegistry> getTransformerRegister() {
        return it -> this.transformers.forEach(it::registerTransformer);
    }

    protected void registerTransformer(@Nonnull final Transformer transformer) {
        Preconditions.checkNotNull(transformer);
        this.transformers.add(transformer);
    }
}
