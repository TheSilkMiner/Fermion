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

package net.thesilkminer.mc.fermion;

import javax.annotation.Nonnull;

public final class OtherClass {

    private static int ID = 0;

    private final String parameter;
    private final String printed;

    OtherClass(@Nonnull final String parameter) {
        ++ID;
        this.parameter = parameter.replaceAll("[Aa]", "\\$");
        this.printed = this.print(parameter);
    }

    private static int getId(@SuppressWarnings("unused") @Nonnull final Object object) {
        return ID;
    }

    @Nonnull
    private String print(@Nonnull final String marker) {
        final String toPrint = marker + ">> " + getId(this) + " " + this.parameter;
        System.out.println(toPrint);
        return toPrint;
    }

    @Nonnull
    final String getPrinted() {
        return this.printed;
    }
}
