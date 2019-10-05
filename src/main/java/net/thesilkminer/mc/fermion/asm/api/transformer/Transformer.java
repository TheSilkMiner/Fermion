package net.thesilkminer.mc.fermion.asm.api.transformer;

import com.google.gson.JsonObject;
import net.thesilkminer.mc.fermion.asm.api.configuration.TransformerConfiguration;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import org.objectweb.asm.ClassVisitor;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Identifies a Fermion transformer which can be used to visit classes and
 * transform them on the fly according to the launch plugin needs.
 *
 * <p>Note that a transformer instance by itself is useless. It needs to be
 * registered to the {@link TransformerRegistry} at the appropriate moment as
 * specified in {@link net.thesilkminer.mc.fermion.asm.api.LaunchPlugin}
 * before being able to work.</p>
 *
 * <p>The transformer has to modify the relevant classes through what's called
 * a {@link ClassVisitor}. Refer to the ASM manual for more information. This
 * API replaces the previous, error-prone
 * {@link org.objectweb.asm.tree.ClassNode}-based one. Refer to
 * {@link #getClassVisitorCreator()} for more information on how to correctly
 * implement a visitor.</p>
 *
 * @since 1.0.0
 */
public interface Transformer {

    /**
     * Gets all the data associated to this transformer, such as the owning
     * plugin or its name.
     *
     * <p>The data must be complete in all its parts. It may or may not be a
     * new instance every time this method is called, provided it does not
     * change between calls.</p>
     *
     * <p>Refer to {@link TransformerData} for more information on which data
     * it is necessary to provide.</p>
     *
     * @return
     *      A {@link TransformerData} object, which must not be null,
     *      containing the entirety of the transformer data that may be
     *      required.
     *
     * @since 1.0.0
     */
    @Nonnull TransformerData getData();

    /**
     * Gets a {@link Set} with all the classes that this transformer wishes
     * to transform, defined through {@link ClassDescriptor}s.
     *
     * <p>A particular class descriptor may not be present more than once.</p>
     *
     * <p>It is discouraged, but otherwise made possible through this API, to
     * specify more than one class as a target of this transformer. This
     * greatly complicates handling of the {@link ClassVisitor} that will have
     * to be provided. It is nevertheless possible to identify which class the
     * transformer is transforming through the visitor itself.</p>
     *
     * @return
     *      A {@link Set} containing a {@link ClassDescriptor} for every class
     *      that the transformer wants to transform. It cannot be null. It can
     *      be empty (but why?).
     *
     * @since 1.0.0
     */
    @Nonnull Set<ClassDescriptor> getClassesToTransform();

    /**
     * Returns a {@link Supplier} for a {@link TransformerConfiguration}
     * instance that provides all the needed methods to serialize, de-serialize
     * and apply the transformer configuration.
     *
     * <p>The supplier cannot return a null value, but it can return a
     * "defaulted" configuration, as in the one described in
     * {@link TransformerConfiguration.Builder#create()}. Refer to the
     * transformer configuration itself for more information on how to
     * implement the respective methods.</p>
     *
     * @return
     *      A {@link Supplier} that provides a suitable
     *      {@link TransformerConfiguration} for this transformer. It must not
     *      be null. The Supplier must be as described previously.
     *
     * @since 1.0.0
     */
    @Nonnull Supplier<TransformerConfiguration> provideConfiguration();

    /**
     * Gets a new instance of a {@link ClassVisitor} that visits and transforms
     * the target class or classes specified by the transformer in
     * {@link #getClassesToTransform()}.
     *
     * <p>The {@link BiFunction} cannot return a null value and is also
     * restricted in how it can create the class visitor instance. More
     * specifically, it cannot be cached: it must be a new instance every time
     * this method is called.</p>
     *
     * <p>The first parameter passed to the BiFunction is the ASM API version
     * that is being used. The second parameter is a parent class visitor. Both
     * of these elements <strong>have to</strong> be used in the construction
     * of the new instance, like in code that follows this paragraph. Not
     * supplying either one or the other will lead to undesirable behavior,
     * which will break not only this transformer, but the entirety of the
     * Fermion environment. Moreover, both of those parameters are always
     * not-null.</p>
     *
     * <pre>
     * // How to correctly implement this method
     * return (v, cw) -> new ClassVisitor(v, cw) {
     *     // Override methods here as needed.
     * }
     * </pre>
     *
     * <p>Any other operation on the given {@code ClassVisitor} or integer must
     * be deemed illegal in terms of code. Again, <strong>don't attempt to do
     * something with the given visitors</strong>!</p>
     *
     * @return
     *      A {@link BiFunction} used to construct a suitable
     *      {@link ClassVisitor} for the class that needs to be transformed. It
     *      cannot be null.
     *
     * @since 1.0.0
     */
    @Nonnull BiFunction<Integer, ClassVisitor, ClassVisitor> getClassVisitorCreator();

    /**
     * Applies the configuration options stored in the {@link JsonObject} to
     * the transformer.
     *
     * @param configuration
     *      The {@link JsonObject} that stores the configuration. It is
     *      guaranteed not to be null.
     *
     * @implNote
     *      By default, this method gets the {@link TransformerConfiguration}
     *      that the transformer provides and calls the deserializer on it.
     *      It is suggested for implementors of this interface not to touch
     *      this method and leave its default implementation alone, choosing
     *      to use the deserializer for that job instead.
     *
     * @see TransformerConfiguration#getDeserializer()
     * @since 1.0.0
     */
    default void applyConfiguration(@Nonnull final JsonObject configuration) {
        this.provideConfiguration().get().getDeserializer().accept(configuration);
    }
}
