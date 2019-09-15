package net.thesilkminer.mc.fermion.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;

public enum TransformingUtilities {
    ;

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    private static String transformedFieldName() {
        return null;
    }

    private static boolean wasTransformed(@Nullable final Class<?> clazz,
                                          @SuppressWarnings({"SameParameterValue", "unused"}) @Nullable final Object marker) {
        try {
            if (clazz == null) return false;
            final Field field = clazz.getDeclaredField(transformedFieldName());
            return field.isSynthetic();
        } catch (final ReflectiveOperationException e) {
            return false;
        }
    }

    @Nullable
    private static Class<?> forName(@Nonnull final String name) {
        try {
            return Class.forName(name);
        } catch (@Nonnull final ReflectiveOperationException e) {
            return null;
        }
    }

    public static boolean wasTransformed(@Nonnull final Class<?> clazz) {
        return wasTransformed(clazz, null);
    }

    public static boolean wasTransformed(@Nonnull final Object object) {
        return wasTransformed(object.getClass());
    }

    public static boolean wasTransformed(@Nonnull final String string) {
        return wasTransformed(forName(string), null);
    }
}
