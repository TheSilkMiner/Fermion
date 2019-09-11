package net.thesilkminer.mc.fermion.asm.prefab.transformer;

import com.google.common.collect.ImmutableMap;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.descriptor.MethodDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.MethodVisitor;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.BiFunction;

public abstract class SingleTargetMethodTransformer extends TargetMethodTransformer {
    private final MethodDescriptor targetMethod;

    public SingleTargetMethodTransformer(@Nonnull final TransformerData data, @Nonnull final ClassDescriptor targetClass,
                                         @Nonnull final MethodDescriptor targetMethod) {
        super(data, targetClass, targetMethod);
        this.targetMethod = targetMethod;
    }

    @Nonnull
    @Override
    protected final Map<MethodDescriptor, BiFunction<MethodDescriptor, Pair<Integer, MethodVisitor>, MethodVisitor>> getMethodVisitorCreators() {
        return ImmutableMap.of(this.targetMethod, (desc, pair) -> this.getMethodVisitorCreator().apply(pair.getLeft(), pair.getRight()));
    }

    @Nonnull
    protected abstract BiFunction<Integer, MethodVisitor, MethodVisitor> getMethodVisitorCreator();
}
