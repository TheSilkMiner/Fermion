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

    @Nonnull
    public static String print(@Nonnull final OtherClass instance, @Nonnull final String marker) {
        return "";
    }

    public static int getIdThroughMethod(@Nonnull final Object object) {
        return -1;
    }
}
