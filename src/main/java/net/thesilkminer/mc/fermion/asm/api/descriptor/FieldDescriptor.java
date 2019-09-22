package net.thesilkminer.mc.fermion.asm.api.descriptor;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Describes a field that will be targeted by a transformer either directly or
 * indirectly.
 *
 * <p>This descriptor doesn't store the accessibility of the field, but rather
 * only its name, the type of the field and whether it is static.</p>
 *
 * <p>Note that instances of this class do not cause class loading neither
 * during normal usage or construction, if used properly.</p>
 *
 * @since 1.0.0
 */
public final class FieldDescriptor {

    private final String name;
    private final ClassDescriptor type;
    private final boolean isStatic;

    private FieldDescriptor(@Nonnull final String name, @Nonnull final ClassDescriptor descriptor, final boolean isStatic) {
        this.name = name;
        this.type = descriptor;
        this.isStatic = isStatic;
    }

    /**
     * Constructs a field descriptor from the given information.
     *
     * <p>Note that this does not cause class loading of class containing the
     * target field or of the class that represents the type of the field, if
     * all the objects passed to this method are also appropriately
     * constructed.</p>
     *
     * @param name
     *      The name of the field that should be targeted. It must not be null.
     * @param descriptor
     *      A {@link ClassDescriptor} instance that represents the type of the
     *      field to target. It must not be null. The type targeted by the
     *      descriptor must also be a valid type for a field.
     * @param isStatic
     *      Indicates whether the field is static or not inside the class that
     *      contains it.
     * @return
     *      A new field descriptor constructed with the given information.
     * @throws IllegalArgumentException
     *      When the given descriptor represents a type that is not valid for
     *      a field.
     *
     * @since 1.0.0
     */
    @Nonnull
    public static FieldDescriptor of(@Nonnull final String name, @Nonnull final ClassDescriptor descriptor, final boolean isStatic) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(descriptor);
        Preconditions.checkArgument(!Objects.equals(descriptor, ClassDescriptor.of(void.class)), "Field of void type cannot exist");
        return new FieldDescriptor(name, descriptor, isStatic);
    }

    /**
     * Constructs a non-static field descriptor from the given information.
     *
     * <p>Note that this does not cause class loading of class containing the
     * target field or of the class that represents the type of the field, if
     * all the objects passed to this method are also appropriately
     * constructed.</p>
     *
     * <p>To construct a static field, please refer to
     * {@link #of(String, ClassDescriptor, boolean)} instead.</p>
     *
     * @param name
     *      The name of the field that should be targeted. It must not be null.
     * @param descriptor
     *      A {@link ClassDescriptor} instance that represents the type of the
     *      field to target. It must not be null. The type targeted by the
     *      descriptor must also be a valid type for a field.
     * @return
     *      A new non-static field descriptor constructed with the given
     *      information.
     * @throws IllegalArgumentException
     *      When the given descriptor represents a type that is not valid for
     *      a field.
     *
     * @since 1.0.0
     */
    @Nonnull
    public static FieldDescriptor of(@Nonnull final String name, @Nonnull final ClassDescriptor descriptor) {
        return of(name, descriptor, false);
    }

    /**
     * Gets the name of the field targeted by this descriptor.
     *
     * @return
     *      The name of the field targeted by this descriptor. It is guaranteed
     *      not to be null.
     *
     * @see #toAsmName()
     * @see #toAsmDescriptor()
     * @since 1.0.0
     */
    @Nonnull
    public String getName() {
        return this.name;
    }

    /**
     * Gets the type of the field targeted by this descriptor.
     *
     * @return
     *      The type of the field targeted by this descriptor. It is guaranteed
     *      not to be null.
     *
     * @see ClassDescriptor
     * @since 1.0.0
     */
    @Nonnull
    public ClassDescriptor getType() {
        return this.type;
    }

    /**
     * Gets whether the field targeted by this descriptor is static or not.
     *
     * @return
     *      Whether the field targeted by this descriptor is static or not.
     *
     * @since 1.0.0
     */
    public boolean isStatic() {
        return this.isStatic;
    }

    /**
     * Gets the type of the field targeted by this descriptor opportunely
     * mangled in order to be used in ASM descriptors or any other contexts
     * that require a descriptor-like string.
     *
     * @return
     *      The type of the field targeted by this descriptor opportunely
     *      mangled for usage in the appropriate ASM contexts. It is guaranteed
     *      not to be null.
     *
     * @see #toAsmName()
     * @since 1.0.0
     */
    @Nonnull
    public String toAsmDescriptor() {
        return this.type.toAsmName();
    }

    /**
     * Gets the name of the field targeted by this descriptor opportunely
     * mangled for usage in ASM contexts, such as field declarations.
     *
     * <p>Note that in most cases, you don't need this type of mangling,
     * which is mostly useful in {@link #toString()} representations. In such
     * cases, please check {@link #toAsmDescriptor()},
     * {@link ClassDescriptor#toAsmName()}, and
     * {@link ClassDescriptor#toAsmMethodDescriptor()} first.</p>
     *
     * @return
     *      The name of the field targeted by this descriptor opportunely
     *      mangled for usage in ASM contexts.
     *
     * @since 1.0.0
     */
    @Nonnull
    public String toAsmName() {
        return this.toAsmDescriptor() + " " + this.name;
    }

    @Nonnull
    @Override
    public String toString() {
        return this.toAsmName();
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final FieldDescriptor that = (FieldDescriptor) o;
        return this.isStatic == that.isStatic &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.type, this.isStatic);
    }
}
