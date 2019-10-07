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

package net.thesilkminer.mc.fermion.asm.api.descriptor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * Describes a method that will be targeted by a transformer either directly or
 * indirectly.
 *
 * <p>This descriptor stores only the name, the return type, and the arguments
 * of the method, all of them erased of any generic signature types.</p>
 *
 * <p>Note that instances of this class, if properly used and properly
 * constructed do not cause unwanted class loading as side effect of their
 * operation.</p>
 *
 * @since 1.0.0
 */
public final class MethodDescriptor {

    private final String name;
    private final ClassDescriptor returnType;
    private final List<ClassDescriptor> arguments;

    private MethodDescriptor(@Nonnull final String name, @Nonnull final List<ClassDescriptor> arguments,
                             @Nonnull final ClassDescriptor returnType) {
        this.name = Preconditions.checkNotNull(name);
        this.arguments = ImmutableList.copyOf(Preconditions.checkNotNull(arguments));
        this.returnType = Preconditions.checkNotNull(returnType);
    }

    /**
     * Constructs a method descriptor that targets the method identified by the
     * passed in information.
     *
     * <p>Note that this does not cause class loading of the class containing
     * the method that is being targeted or any of the classes that represents
     * the method's argument types or return type, provided that all the passed
     * in objects are also constructed correctly.</p>
     *
     * @param name
     *      The name of the method to target. It must not be null.
     * @param arguments
     *      A {@link List} of {@link ClassDescriptor}s representing the
     *      arguments that need to be passed to the target method. It must
     *      not be null. The descriptors must be in the same order as the
     *      arguments passed in and must be in the same number. The size
     *      restriction also applies in case of variable arity methods.
     * @param returnType
     *      A {@link ClassDescriptor} representing the target method's return
     *      type. It must not be null.
     * @return
     *      A new method descriptor that targets the method identified by the
     *      given information.
     *
     * @since 1.0.0
     */
    @Nonnull
    public static MethodDescriptor of(@Nonnull final String name, @Nonnull final List<ClassDescriptor> arguments,
                                      @Nonnull final ClassDescriptor returnType) {
        return new MethodDescriptor(name, arguments, returnType);
    }

    /**
     * Gets the name of the method targeted by this descriptor.
     *
     * @return
     *      The name of the method targeted by this descriptor. It is
     *      guaranteed not to be null.
     *
     * @since 1.0.0
     */
    @Nonnull
    public String getName() {
        return this.name;
    }

    /**
     * Gets the return type of the method targeted by this descriptor.
     *
     * @return
     *      The return type of the method targeted by this descriptor. It is
     *      guaranteed not to be null.
     *
     * @see ClassDescriptor
     * @since 1.0.0
     */
    @Nonnull
    public ClassDescriptor getReturnType() {
        return this.returnType;
    }

    /**
     * Gets a list of all the argument types of the method targeted by this
     * descriptor.
     *
     * <p>The returned list may not be mutated through external calls.</p>
     *
     * @return
     *      A list of all the argument types of the method targeted by this
     *      descriptor. It is guaranteed not to be null. The returned list
     *      may be empty.
     *
     * @see ClassDescriptor
     * @since 1.0.0
     */
    @Nonnull
    public List<ClassDescriptor> getArguments() {
        return ImmutableList.copyOf(this.arguments);
    }

    /**
     * Gets the descriptor of the method targeted by this descriptor for usage
     * in the appropriate ASM contexts.
     *
     * <p>Appropriate contexts are, e.g., method descriptors (duh) or any other
     * kind of place that requires a method-descriptor-like string.</p>
     *
     * @return
     *      The descriptor of the method targeted by this descriptor for usage
     *      in the appropriate ASM contexts. It is guaranteed not to be null.
     *
     * @see #toAsmName()
     * @since 1.0.0
     */
    @Nonnull
    public String toAsmDescriptor() {
        final StringBuilder builder = new StringBuilder("(");
        this.arguments.forEach(it -> builder.append(it.toAsmMethodDescriptor()));
        builder.append(")");
        builder.append(this.returnType.toAsmMethodDescriptor());
        return builder.toString();
    }

    /**
     * Gets both the name and the descriptor of the method targeted by this
     * descriptor opportunely mangled in order to be used in the appropriate
     * ASM contexts.
     *
     * <p>Note that in most cases you don't need this type of mangling, which
     * is mostly useful in {@link #toString()} representations. Before
     * resorting to this method, please refer to {@link #toAsmDescriptor()}
     * too.</p>
     *
     * @return
     *      The name and the descriptor of the method targeted by this
     *      descriptor opportunely mangled for usage in the appropriate ASM
     *      contexts. It is guaranteed not to be null.
     */
    @Nonnull
    public String toAsmName() {
        return this.name + this.toAsmDescriptor();
    }

    @Override
    public String toString() {
        return this.toAsmName();
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final MethodDescriptor that = (MethodDescriptor) o;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.returnType, that.returnType) &&
                Objects.equals(this.arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.returnType, this.arguments);
    }
}
