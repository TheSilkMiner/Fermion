package net.thesilkminer.mc.fermion.asm.api.descriptor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

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

    @Nonnull
    public static ClassDescriptor of(@Nonnull final String className) {
        return of(className, false);
    }

    @Nonnull
    public static ClassDescriptor of(@Nonnull final Class<?> clazz) {
        return of(clazz.getName(), clazz.isPrimitive());
    }

    @Nonnull
    public static ClassDescriptor of(@Nonnull final Object object) {
        return of(object.getClass());
    }

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

    @Nonnull
    public String getClassName() {
        return this.className;
    }

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
