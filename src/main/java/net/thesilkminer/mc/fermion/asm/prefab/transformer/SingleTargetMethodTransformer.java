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

/**
 * Transformer that automatically finds the given method in the specified
 * class and passes it to the given visitor for transformation.
 *
 * <p>In other words, this transformer attempts to transform at most one
 * target class and at most one target method. The method found is then passed
 * to the visitor creator and used to create visitors that perform the
 * actual transformation.</p>
 *
 * <p>To ensure safety in this implementation, most of the methods that need
 * to be untouched have been made non-virtual and non-overridable (i.e.
 * final). All the added methods, that represent this transformer's public
 * API are documented in depth.</p>
 *
 * @since 1.0.0
 */
public abstract class SingleTargetMethodTransformer extends TargetMethodTransformer {
    private final MethodDescriptor targetMethod;

    /**
     * Constructs a new instance of this transformer.
     *
     * @param data
     *      The data that identifies this transformer. Refer to
     *      {@link TransformerData} for more information. It cannot be null.
     * @param targetClass
     *      The {@link ClassDescriptor} representing the class to transform.
     *      It cannot be null.
     * @param targetMethod
     *      The {@link MethodDescriptor} representing the target method that
     *      is defined in the class targeted by {@code targetClass} that needs
     *      to be transformed. It cannot be null.
     *
     * @since 1.0.0
     */
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

    /**
     * Gets a new instance of a {@link MethodVisitor} that visits and
     * transforms the target method that has been specified when constructing
     * this transformer.
     *
     * <p>The {@link BiFunction} must return a new method visitor instance
     * every time it is called and it cannot return null. The creation process
     * is also restricted in some ways.</p>
     *
     * <p>The first parameter passed to the {@code BiFunction} is the ASM API
     * version that is being used. The second parameter is the parent
     * {@code MethodVisitor}. The ASM API version <strong>must</strong> be used
     * in the construction of the returned visitor. The parent visitor
     * <strong>should</strong> be used in the construction of the visitor,
     * unless a complete overwrite is wanted. In this case, you
     * <strong>need</strong> to use the passed in visitor to rebuild the
     * entire method's bytecode. Moreover, all of these parameters are
     * guaranteed to be non-null.</p>
     *
     * <pre>
     * // How to correctly implement this method
     * return (v, mv) -> new MethodVisitor(v, mv) {
     *     // Override methods here as needed
     * }
     * </pre>
     *
     * <pre>
     * // How to correctly implement this method if a method needs to be
     * // overwritten
     * return (v, mv) -> new MethodVisitor(v, null) {
     *     // Override methods here as needed. Refer to the parent as
     *     // mv.visitXxx
     * }
     * </pre>
     *
     * <p>Any other operation on the given {@code MethodVisitor} or integer must
     * be deemed illegal in terms of code. Again, <strong>don't attempt to do
     * something with the given visitors</strong>!</p>
     *
     * @return
     *      A {@link BiFunction} used to construct a suitable
     *      {@link MethodVisitor} for the method that needs to be transformed.
     *      It cannot be null.
     *
     * @since 1.0.0
     */
    @Nonnull
    protected abstract BiFunction<Integer, MethodVisitor, MethodVisitor> getMethodVisitorCreator();
}
