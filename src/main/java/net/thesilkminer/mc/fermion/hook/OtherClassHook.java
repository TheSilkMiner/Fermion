package net.thesilkminer.mc.fermion.hook;

import net.thesilkminer.mc.fermion.OtherClass;

import javax.annotation.Nonnull;

public final class OtherClassHook {

    @Nonnull
    public static String getParameter(@Nonnull final OtherClass instance) {
        return "";
    }

    public static int getId() {
        return -1;
    }
}
