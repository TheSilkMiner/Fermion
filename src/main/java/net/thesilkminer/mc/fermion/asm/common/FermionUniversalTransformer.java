package net.thesilkminer.mc.fermion.asm.common;

import com.google.common.collect.ImmutableSet;
import net.thesilkminer.mc.fermion.asm.api.configuration.TransformerConfiguration;
import net.thesilkminer.mc.fermion.asm.api.transformer.Transformer;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

class FermionUniversalTransformer implements Transformer {
    static final String TRANSFORMER_NAME = "fermion.asm.service:universal";

    @Nonnull
    @Override
    public TransformerData getData() {
        return TransformerData.Builder.create()
                .setOwningPluginId("fermion.asm.service")
                .setName("universal")
                .setDescription("The universal plugin")
                .build();
    }

    @Nonnull
    @Override
    public Set<String> getClassesToTransform() {
        return ImmutableSet.of("every.single.one");
    }

    @Nonnull
    @Override
    public Supplier<TransformerConfiguration> provideConfiguration() {
        return () -> TransformerConfiguration.Builder.create().build();
    }

    @Nonnull
    @Override
    public BiFunction<Integer, ClassVisitor, ClassVisitor> getClassVisitorCreator() {
        return (v, w) -> {
            final FieldVisitor fv = w.visitField(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                    "_re_syst_patch_successful", "Z", null, null);
            fv.visitEnd();
            w.visitEnd();

            return new ClassVisitor(v, w) {};
        };
    }
}
