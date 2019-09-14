package net.thesilkminer.mc.fermion;

import javax.annotation.Nonnull;

public final class OtherClass {

    private static int ID = 0;

    private final String parameter;

    OtherClass(@Nonnull final String parameter) {
        ++ID;
        this.parameter = parameter.replaceAll("[Aa]", "\\$");
    }

    public void print() {
        System.out.println("" + ID + " " + this.parameter);
    }
}
