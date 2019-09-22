package net.thesilkminer.mc.fermion.asm.api;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.INameMappingService;

import javax.annotation.Nonnull;
import java.util.Optional;

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
    // TODO: Map them automatically?

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
        return this.map(name, INameMappingService.Domain.METHOD);
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
        return this.map(name, INameMappingService.Domain.FIELD);
    }

    @Nonnull
    private String map(@Nonnull final String name, @Nonnull final INameMappingService.Domain domain) {
        return Optional.ofNullable(Launcher.INSTANCE)
                .map(Launcher::environment)
                .flatMap(it -> it.findNameMapping("srg"))
                .map(it -> it.apply(domain, name))
                .orElse(name);
    }
}
