package net.thesilkminer.mc.fermion.asm.api;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.INameMappingService;

import javax.annotation.Nonnull;
import java.util.Optional;

public enum MappingUtilities {
    INSTANCE;

    @Nonnull
    public String mapMethod(@Nonnull final String name) {
        return this.map(name, INameMappingService.Domain.METHOD);
    }

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
