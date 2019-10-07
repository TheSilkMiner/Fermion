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

package net.thesilkminer.mc.fermion.asm.common.utility;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

@SuppressWarnings("MethodCanBeVariableArityMethod")
public final class EffectivelyFinalByteArray {
    private byte[] classBytes;
    private boolean wasTransformed;

    private EffectivelyFinalByteArray(@Nonnull final byte[] classBytes) {
        this.classBytes = classBytes;
    }

    public static EffectivelyFinalByteArray of(@Nonnull final byte[] classBytes) {
        return new EffectivelyFinalByteArray(Preconditions.checkNotNull(classBytes));
    }

    public void transformInto(@Nonnull final byte[] classBytes) {
        this.classBytes = classBytes;
        this.wasTransformed = true;
    }

    public boolean wasTransformed() {
        return this.wasTransformed;
    }

    @Nonnull
    public byte[] get() {
        return this.classBytes;
    }
}
