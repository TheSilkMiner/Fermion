package net.thesilkminer.mc.fermion.asm.prefab.transformer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.thesilkminer.mc.fermion.asm.api.MappingUtilities;
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
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Transformer that automatically finds the given methods in the specified
 * class and passes them to the given visitor for transformation.
 *
 * <p>In other words, this transformer attempts to transform at most one
 * target class and it attempts to identify all the methods in it that
 * require some sort of transformations. The methods that are found are then
 * passed to the registered visitor creators and used to create visitors that
 * will actually transform the methods.</p>
 *
 * <p>To ensure safety in this implementation, most of the methods that need
 * to be untouched have been made non-virtual and non-overridable (i.e.
 * final). All the added methods, that represent this transformer's public
 * API are documented in depth.</p>
 *
 * @since 1.0.0
 */
public abstract class TargetMethodTransformer extends AbstractTransformer {

    private static final Logger LOGGER = LogManager.getLogger("TargetMethodTransformer");

    private final Marker marker;
    private final List<MethodDescriptor> targetMethods;

    private Map<MethodDescriptor, BiFunction<MethodDescriptor, Pair<Integer, MethodVisitor>, MethodVisitor>> methodVisitors;

    /**
     * Constructs a new instance of this transformer.
     *
     * @param data
     *      The data that identifies this transformer. Refer to
     *      {@link TransformerData} for more information. It cannot be null.
     * @param targetClass
     *      The {@link ClassDescriptor} representing the class to transform.
     *      It cannot be null.
     * @param targetMethods
     *      The {@link MethodDescriptor}s representing the target methods that
     *      are defined in the class targeted by {@code targetClass} that need
     *      to be transformed. There must be at least one target method.
     *
     * @since 1.0.0
     */
    protected TargetMethodTransformer(@Nonnull final TransformerData data, @Nonnull final ClassDescriptor targetClass,
                                      @Nonnull final MethodDescriptor... targetMethods) {
        super(data, targetClass);
        Preconditions.checkArgument(Preconditions.checkNotNull(targetMethods).length > 0, "At least one method target must be given");
        this.targetMethods = ImmutableList.copyOf(Arrays.asList(targetMethods));
        this.marker = MarkerManager.getMarker(this.getData().getOwningPluginId() + ":" + this.getData().getName());
    }

    /**
     * Gets a map containing the target method descriptors mapped to their
     * respective method visitors creators.
     *
     * <p>The {@link BiFunction} must return a new method visitor instance
     * every time it is called and it cannot return null. It is also restricted
     * in how the method visitor instance must be created.</p>
     *
     * <p>The first parameter that is passed to the {@code BiFunction} is the
     * method descriptor that acts as a key to the map. This way, if a visitor
     * requires access to the descriptor it is not necessary to recreate it
     * every single time it gets accessed. The second parameter is a
     * {@link Pair} that stores both the ASM API version as its left value and
     * the parent method visitor as its right value. The ASM API version
     * <strong>must</strong> be used in the construction of the new method
     * visitor. The parent method <strong>should</strong> be used in the
     * construction of the parent, unless you want to overwrite a method
     * completely. In this case, you <strong>need</strong> to use the passed in
     * visitor to rebuild the method's bytecode. Moreover, all of these
     * parameters are not-null.</p>
     *
     * <pre>
     * // How to correctly implement this method
     * return ImmutableMap.of(myMethodDesc,
     *     (desc, pair) -> new MethodVisitor(pair.getLeft(), pair.getRight()) {
     *         // Override methods here as needed.
     *     }
     * );
     * </pre>
     *
     * <pre>
     * // How to correctly implement the BiFunction if a method needs to be
     * // overwritten
     * (desc, pair) -> {
     *     final MethodVisitor parent = pair.getRight();
     *     return new MethodVisitor(pair.getLeft(), null) {
     *         // Override methods here as needed. Refer to the parent
     *         // as parent.visitXxx
     *     };
     * }
     * </pre>
     *
     * <p>Any other operation on the given {@code MethodVisitor} or integer must
     * be deemed illegal in terms of code. Again, <strong>don't attempt to do
     * something with the given visitors</strong>!</p>
     *
     * @return
     *      A {@link Map} that has as keys the target {@link MethodDescriptor}s
     *      and as values {@link BiFunction}s that create the appropriate
     *      {@link MethodVisitor}s for the target methods, constructed as
     *      previously detailed.
     *
     * @since 1.0.0
     */
    @Nonnull
    protected abstract Map<MethodDescriptor, BiFunction<MethodDescriptor, Pair<Integer, MethodVisitor>, MethodVisitor>> getMethodVisitorCreators();

    @Nonnull
    @Override
    public final BiFunction<Integer, ClassVisitor, ClassVisitor> getClassVisitorCreator() {
        if (this.methodVisitors == null) {
            this.methodVisitors = this.getMethodVisitorCreators()
                    .entrySet()
                    .stream()
                    .map(this::remapMethodIfNeeded)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            this.targetMethods.stream()
                    .map(this::remapMethodIfNeeded)
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

    @Nonnull
    private MethodDescriptor remapMethodIfNeeded(@Nonnull final MethodDescriptor in) {
        return MethodDescriptor.of(MappingUtilities.INSTANCE.mapMethod(in.getName()), in.getArguments(), in.getReturnType());
    }

    @Nonnull
    private <V> Map.Entry<MethodDescriptor, V> remapMethodIfNeeded(@Nonnull final Map.Entry<MethodDescriptor, V> in) {
        final MethodDescriptor original = in.getKey();
        final MethodDescriptor remapped = MethodDescriptor.of(MappingUtilities.INSTANCE.mapMethod(original.getName()), original.getArguments(), original.getReturnType());
        return new AbstractMap.SimpleImmutableEntry<>(remapped, in.getValue());
    }
}
