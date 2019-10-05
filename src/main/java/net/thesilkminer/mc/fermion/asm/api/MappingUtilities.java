package net.thesilkminer.mc.fermion.asm.api;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

/**
 * Collection of utilities used to map names from their obfuscated to their
 * de-obfuscated counterpart in case its needed.
 *
 * @since 1.0.0
 */
public enum MappingUtilities {
    /**
     * The unique instance of these utility class.
     *
     * @since 1.0.0
     */
    INSTANCE;

    /**
     * Maps the given obfuscated method name to its de-obfuscated counterpart
     * if needed.
     *
     * @param name
     *      The name of the method to map. It cannot be null.
     * @return
     *      The mapped name, if needed, otherwise the same name. Guaranteed to
     *      be not-null.
     *
     * @since 1.0.0
     */
    @Nonnull
    public String mapMethod(@Nonnull final String name) {
        return this.map(name, "methodNameMaps");
    }

    /**
     * Maps the given obfuscated field name to its de-obfuscated counterpart if
     * needed.
     *
     * @param name
     *      The name of the field to map. It cannot be null.
     * @return
     *      The mapped name, if needed, otherwise the same name. Guaranteed to
     *      be not-null.
     *
     * @since 1.0.0
     */
    @Nonnull
    public String mapField(@Nonnull final String name) {
        return this.map(name, "fieldNameMaps");
    }

    @Nonnull
    private String map(@Nonnull final String name, @Nonnull final String mapName) {
        try {
            return this.reflectAndMap(name, mapName);
        } catch (@Nonnull final ReflectiveOperationException e) {
            System.err.println("An error has occurred while attempting to remap the given name '" + name + "'");
            e.printStackTrace(System.err);
            return name;
        }
    }

    @Nonnull
    private String reflectAndMap(@Nonnull final String name, @Nonnull final String mapName) throws ReflectiveOperationException {
        final Object remap = this.obtainRemapperInstance();
        final Map<String, Map<String, String>> nameMap = this.reflectMapField(remap, mapName);
        // names are in the key of the value of the entry, so...
        return nameMap.values()
                .stream()
                .map(Map::keySet)
                .flatMap(Set::stream)
                .filter(it -> it.startsWith(name))
                .findFirst()
                .orElse(name);
    }

    @Nonnull
    @SuppressWarnings("SpellCheckingInspection")
    private Object obtainRemapperInstance() throws ReflectiveOperationException {
        final Class<?> remap = Class.forName("net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper");
        return remap.getDeclaredField("INSTANCE").get(null);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    private Map<String, Map<String, String>> reflectMapField(@Nonnull final Object remap, @Nonnull final String mapName) throws ReflectiveOperationException {
        final Field mapField = remap.getClass().getDeclaredField(mapName);
        mapField.setAccessible(true);
        return (Map<String, Map<String, String>>) mapField.get(remap);
    }
}
