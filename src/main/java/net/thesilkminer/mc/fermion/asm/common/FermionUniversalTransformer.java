package net.thesilkminer.mc.fermion.asm.common;

import com.google.common.collect.ImmutableSet;
import net.thesilkminer.mc.fermion.asm.api.configuration.TransformerConfiguration;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.Transformer;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.common.utility.Log;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

final class FermionUniversalTransformer implements Transformer {
    static final String TRANSFORMER_NAME = "fermion.asm.service:universal";
    private static final Log L = Log.of(TRANSFORMER_NAME);

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
    public Set<ClassDescriptor> getClassesToTransform() {
        return ImmutableSet.of();
    }

    @Nonnull
    @Override
    public Supplier<TransformerConfiguration> provideConfiguration() {
        return () -> TransformerConfiguration.Builder.create().build();
    }

    @Nonnull
    @Override
    public BiFunction<Integer, ClassVisitor, ClassVisitor> getClassVisitorCreator() {
        return (v, w) -> new ClassVisitor(v, w) {
            @Override
            public void visitEnd() {
                final FieldVisitor fv = super.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC,
                        "_re_syst_patch_successful", "Z", null, null);
                fv.visitEnd();
                L.i("Successfully injected field into class");
                super.visitEnd();
            }
        };
    }
}
