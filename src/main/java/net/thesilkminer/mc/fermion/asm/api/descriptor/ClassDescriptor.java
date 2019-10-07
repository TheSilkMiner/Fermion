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
import com.google.common.collect.Maps;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

/**
 * Describes a class that will be targeted by a transformer either directly or
 * indirectly.
 *
 * <p>This descriptor stores the class name of a class and whether it is a
 * primitive type or not. It acts as a sort of wrapper around raw strings,
 * simplifying the implementation.</p>
 *
 * <p>Note that instances of this class, if properly used, do not cause class
 * loading neither during construction or during normal usage.</p>
 *
 * @since 1.0.0
 */
public final class ClassDescriptor {

    private static final Map<String, ClassDescriptor> CACHE = Maps.newHashMap();

    private final String className;
    private final boolean primitive;

    private ClassDescriptor(@Nonnull final String className, final boolean primitive) {
        this.className = className;
        this.primitive = primitive;
    }

    @Nonnull
    private static ClassDescriptor of(@Nonnull final String className, final boolean isPrimitive) {
        Preconditions.checkNotNull(className);
        final String name = className.contains("/")? className.replace('/', '.') : className;
        return CACHE.computeIfAbsent(name, k -> new ClassDescriptor(k, isPrimitive));
    }

    /**
     * Constructs a non-primitive class descriptor using the provided class
     * name as a target.
     *
     * <p>Note that this does not cause class loading of the target class nor
     * of any related classes. Also, there are no constraints on the existence
     * of the class.</p>
     *
     * <p>To construct a class descriptor of a primitive type, refer to
     * {@link #of(Class)}.</p>
     *
     * @param className
     *      The class name to target. It must not be null.
     * @return
     *      A new non-primitive class descriptor targeting the class identified
     *      by the given name.
     *
     * @since 1.0.0
     */
    @Nonnull
    public static ClassDescriptor of(@Nonnull final String className) {
        return of(className, false);
    }

    /**
     * Constructs a class descriptor using the given class to obtain the
     * necessary information.
     *
     * <p>Note that this <strong>will</strong> cause class loading of the
     * target class so that the object can be constructed. You should thus
     * rely on {@linkplain #of(String) the String version}.</p>
     *
     * <p>It is also suggested to use this method only to refer to types
     * that are already loaded prior to the transformer, such as all
     * primitives, base Java classes such as {@link Object} or the Collections
     * Framework, or Guava classes. Any class that you want to transform is
     * <strong>NOT</strong> loaded prior to your transformer.</p>
     *
     * @param clazz
     *      The class to use to obtain the necessary information. It must not
     *      be null.
     * @return
     *      A new class descriptor that targets the class passed in.
     *
     * @since 1.0.0
     */
    @Nonnull
    public static ClassDescriptor of(@Nonnull final Class<?> clazz) {
        return of(clazz.getName(), clazz.isPrimitive());
    }

    /**
     * Constructs a class descriptor using the class of the given object to
     * obtain the necessary information.
     *
     * <p>Note that this <strong>will</strong> cause class loading of the
     * target class (after all, there is an object of that class that was
     * created prior to the invocation of this method). You should thus rely
     * on {@linkplain #of(String) the String version} wherever possible.</p>
     *
     * <p>It is also suggested to use this method only to refer to types
     * that are already loaded prior to the transformer, such as all
     * primitives, base Java classes such as {@link Object} or the Collections
     * Framework, or Guava classes. Any class that you want to transform is
     * <strong>NOT</strong> loaded prior to your transformer.</p>
     *
     * @param object
     *      The object that is an instance of the class that you want to
     *      target. It must not be null.
     * @return
     *      A new class descriptor that targets the class of the object passed
     *      in.
     *
     * @since 1.0.0
     */
    @Nonnull
    public static ClassDescriptor of(@Nonnull final Object object) {
        return of(object.getClass());
    }

    /**
     * Constructs a class descriptor from the given {@link Type}.
     *
     * <p>While this is a valid way to construct a class descriptor, it is
     * suggested to use any of the other construction methods provided
     * ({@link #of(String)}, {@link #of(Class)}, {@link #of(Object)} -- in the
     * order of suggestion) instead of this method. This is only provided to
     * be compatible with internals of the implementation.</p>
     *
     * <p>This will not cause classloading of the class identified by this
     * type. Classes constructed from this method <strong>must</strong> exist,
     * otherwise an error is thrown.</p>
     *
     * @param type
     *      The {@link Type} containing information about the class that should
     *      be targeted. It must not be null.
     * @return
     *      A new class descriptor that targets the class identified by the
     *      Type instance passed in.
     * @throws IllegalArgumentException
     *      If the given type is of sort {@link Type#METHOD} or if its sort is
     *      unrecognized by the system.
     *
     * @since 1.0.0
     */
    @Nonnull
    public static ClassDescriptor of(@Nonnull final Type type) {
        switch (type.getSort()) {
            case Type.VOID: return of(void.class);
            case Type.BOOLEAN: return of(boolean.class);
            case Type.CHAR: return of(char.class);
            case Type.BYTE: return of(byte.class);
            case Type.SHORT: return of(short.class);
            case Type.INT: return of(int.class);
            case Type.FLOAT: return of(float.class);
            case Type.LONG: return of(long.class);
            case Type.DOUBLE: return of(double.class);
            case Type.ARRAY: return of(type.getInternalName());
            case Type.OBJECT: return of(type.getClassName());
            case Type.METHOD: throw new IllegalArgumentException("Method type as a ClassDescriptor?");
            default: throw new IllegalArgumentException("What type is this even?");
        }
    }

    /**
     * Gets the name of the class that is targeted by this descriptor.
     *
     * <p>Note that the returned name cannot be used safely in ASM contexts,
     * such as in visitors or signatures. For usage in ASM contexts, you should
     * refer to either {@link #toAsmMethodDescriptor()} or {@link #toAsmName()}
     * instead.</p>
     *
     * @return
     *      The name of the class targeted by this descriptor. Guaranteed not
     *      to be null.
     *
     * @since 1.0.0
     */
    @Nonnull
    public String getClassName() {
        return this.className;
    }

    /**
     * Gets the name of the class targeted by this descriptor opportunely
     * mangled for usage in ASM visitors.
     *
     * <p>Note that the returned name is not safe for a method descriptor
     * context. In this case you should refer to
     * {@link #toAsmMethodDescriptor()}.</p>
     *
     * @return
     *      The name of the class targeted by this descriptor, opportunely
     *      mangled for usage in ASM visitors. Guaranteed not to be null.
     *
     * @since 1.0.0
     */
    @Nonnull
    public String toAsmName() {
        if (!this.primitive) return this.className.replace('.', '/');
        switch (this.className) {
            case "void": return "V";
            case "boolean": return "Z";
            case "char": return "C";
            case "byte": return "B";
            case "short": return "S";
            case "int": return "I";
            case "float": return "F";
            case "long": return "J";
            case "double": return "D";
            default: throw new IllegalStateException("Not a primitive");
        }
    }

    /**
     * Gets the name of the class targeted by this descriptor opportunely
     * mangled for usage in ASM method descriptors or other ASM contexts
     * that require method-descriptor-like descriptions.
     *
     * <p>Note that the returned name is not safe for all ASM visitor
     * contexts. In certain cases, you may have to refer to
     * {@link #toAsmName()}.</p>
     *
     * @return
     *      The name of the class targeted by this descriptor opportunely
     *      mangled for usage in the appropriate ASM contexts.
     *
     * @since 1.0.0
     */
    @Nonnull
    public String toAsmMethodDescriptor() {
        return this.primitive? this.toAsmName() : "L" + this.toAsmName() + ";";
    }

    @Override
    public String toString() {
        return this.toAsmName();
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final ClassDescriptor that = (ClassDescriptor) o;
        return Objects.equals(this.className, that.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.className);
    }
}
