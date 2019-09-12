package net.thesilkminer.mc.fermion.asm.prefab.transformer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.descriptor.MethodDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.prefab.AbstractTransformer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public abstract class TargetMethodTransformer extends AbstractTransformer {

    private static final Logger LOGGER = LogManager.getLogger("TargetMethodTransformer");

    private final Marker marker;
    private final List<MethodDescriptor> targetMethods;

    private Map<MethodDescriptor, BiFunction<MethodDescriptor, Pair<Integer, MethodVisitor>, MethodVisitor>> methodVisitors;

    public TargetMethodTransformer(@Nonnull final TransformerData data, @Nonnull final ClassDescriptor targetClass,
                                   @Nonnull final MethodDescriptor... targetMethods) {
        super(data, targetClass);
        Preconditions.checkArgument(Preconditions.checkNotNull(targetMethods).length > 0, "At least one method target must be given");
        this.targetMethods = ImmutableList.copyOf(Arrays.asList(targetMethods));
        this.marker = MarkerManager.getMarker(this.getData().getOwningPluginId() + ":" + this.getData().getName());
    }

    @Nonnull
    protected abstract Map<MethodDescriptor, BiFunction<MethodDescriptor, Pair<Integer, MethodVisitor>, MethodVisitor>> getMethodVisitorCreators();

    @Nonnull
    @Override
    public final BiFunction<Integer, ClassVisitor, ClassVisitor> getClassVisitorCreator() {
        if (this.methodVisitors == null) {
            this.methodVisitors = this.getMethodVisitorCreators();
            this.targetMethods.stream()
                    .map(this.methodVisitors::get)
                    .filter(Objects::isNull)
                    .findAny()
                    .ifPresent(it -> {
                        throw new IllegalStateException("Found target method descriptor " + it + " but no matching method visitor");
                    });
        }
        return (v, cw) -> new ClassVisitor(v, cw) {
            @Override
            public MethodVisitor visitMethod(final int access, @Nonnull final String name, @Nonnull final String descriptor,
                                             @Nullable final String signature, @Nullable final String[] exceptions) {
                final MethodVisitor parent = super.visitMethod(access, name, descriptor, signature, exceptions);

                final Type methodType = Type.getType(descriptor);
                final Type[] argumentTypes = methodType.getArgumentTypes();
                final Type returnType = methodType.getReturnType();

                final ClassDescriptor returnDesc = ClassDescriptor.of(returnType);
                final List<ClassDescriptor> arguments = Arrays.stream(argumentTypes)
                        .map(ClassDescriptor::of)
                        .collect(Collectors.toList());

                final MethodDescriptor method = MethodDescriptor.of(name, arguments, returnDesc);

                final BiFunction<MethodDescriptor, Pair<Integer, MethodVisitor>, MethodVisitor> creator =
                        TargetMethodTransformer.this.methodVisitors.get(method);

                if (Objects.isNull(creator)) return parent;

                LOGGER.info(TargetMethodTransformer.this.marker,
                        "Found target method described by method descriptor '" + method +
                                "': calling transforming function now");

                return creator.apply(method, ImmutablePair.of(v, parent));
            }
        };
    }
}
