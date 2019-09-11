package net.thesilkminer.mc.fermion.asm.prefab;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import net.thesilkminer.mc.fermion.asm.api.configuration.TransformerConfiguration;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.Transformer;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import org.objectweb.asm.ClassVisitor;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class AbstractTransformer implements Transformer {

    private final TransformerData data;
    private final Set<ClassDescriptor> targets;

    protected AbstractTransformer(@Nonnull final TransformerData data, @Nonnull final ClassDescriptor... targets) {
        this.data = Preconditions.checkNotNull(data);
        Preconditions.checkArgument(Preconditions.checkNotNull(targets).length > 0, "At least one target must be given");
        this.targets = new HashSet<>(Arrays.asList(targets));
    }

    @Nonnull
    @Override
    public final TransformerData getData() {
        return this.data;
    }

    @Nonnull
    @Override
    public final Set<ClassDescriptor> getClassesToTransform() {
        return ImmutableSet.copyOf(this.targets);
    }

    @Nonnull
    @Override
    public Supplier<TransformerConfiguration> provideConfiguration() {
        return () -> TransformerConfiguration.Builder.create().build();
    }

    @Nonnull
    @Override
    public abstract BiFunction<Integer, ClassVisitor, ClassVisitor> getClassVisitorCreator();
}
