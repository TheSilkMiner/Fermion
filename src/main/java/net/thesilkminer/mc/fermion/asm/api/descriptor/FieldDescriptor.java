package net.thesilkminer.mc.fermion.asm.api.descriptor;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public final class FieldDescriptor {

    private final String name;
    private final ClassDescriptor type;
    private final boolean isStatic;

    private FieldDescriptor(@Nonnull final String name, @Nonnull final ClassDescriptor descriptor, final boolean isStatic) {
        this.name = name;
        this.type = descriptor;
        this.isStatic = isStatic;
    }

    @Nonnull
    public static FieldDescriptor of(@Nonnull final String name, @Nonnull final ClassDescriptor descriptor, final boolean isStatic) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(descriptor);
        Preconditions.checkArgument(!Objects.equals(descriptor, ClassDescriptor.of(void.class)), "Field of void type cannot exist");
        return new FieldDescriptor(name, descriptor, isStatic);
    }

    @Nonnull
    public static FieldDescriptor of(@Nonnull final String name, @Nonnull final ClassDescriptor descriptor) {
        return of(name, descriptor, false);
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    @Nonnull
    public ClassDescriptor getType() {
        return this.type;
    }

    public boolean isStatic() {
        return this.isStatic;
    }

    @Nonnull
    public String toAsmDescriptor() {
        return this.type.toAsmName();
    }

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
