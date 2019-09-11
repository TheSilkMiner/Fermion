package net.thesilkminer.mc.fermion.asm.api.descriptor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

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

    @Nonnull
    public static MethodDescriptor of(@Nonnull final String name, @Nonnull final List<ClassDescriptor> arguments,
                                      @Nonnull final ClassDescriptor returnType) {
        return new MethodDescriptor(name, arguments, returnType);
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    @Nonnull
    public ClassDescriptor getReturnType() {
        return this.returnType;
    }

    @Nonnull
    public List<ClassDescriptor> getArguments() {
        return ImmutableList.copyOf(this.arguments);
    }

    @Nonnull
    public String toAsmDescriptor() {
        final StringBuilder builder = new StringBuilder("(");
        this.arguments.forEach(it -> builder.append(it.toAsmMethodDescriptor()));
        builder.append(")");
        builder.append(this.returnType.toAsmMethodDescriptor());
        return builder.toString();
    }

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
