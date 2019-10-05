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
