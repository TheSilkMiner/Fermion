package net.thesilkminer.mc.fermion.asm.common.utility;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

@SuppressWarnings("MethodCanBeVariableArityMethod")
public final class EffectivelyFinalByteArray {
    private byte[] classBytes;

    private EffectivelyFinalByteArray(@Nonnull final byte[] classBytes) {
        this.classBytes = classBytes;
    }

    public static EffectivelyFinalByteArray of(@Nonnull final byte[] classBytes) {
        return new EffectivelyFinalByteArray(Preconditions.checkNotNull(classBytes));
    }

    public void transformInto(@Nonnull final byte[] classBytes) {
        this.classBytes = classBytes;
    }

    @Nonnull
    public byte[] get() {
        return this.classBytes;
    }
}
