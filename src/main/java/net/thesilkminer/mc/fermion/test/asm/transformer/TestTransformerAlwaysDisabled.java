package net.thesilkminer.mc.fermion.test.asm.transformer;

import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.prefab.AbstractTransformer;
import org.objectweb.asm.ClassVisitor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiFunction;

public final class TestTransformerAlwaysDisabled extends AbstractTransformer {

    public TestTransformerAlwaysDisabled(@Nonnull final LaunchPlugin owner) {
        super(
                TransformerData.Builder.create()
                        .setOwningPlugin(owner)
                        .setName("test_stay_disabled")
                        .setDescription("This transformer literally does nothing. Do not enable it, thanks")
                        .setDisabledByDefault()
                        .build(),
                ClassDescriptor.of("net.thesilkminer.mc.fermion.Fermion")
        );
    }

    @Nonnull
    @Override
    public BiFunction<Integer, ClassVisitor, ClassVisitor> getClassVisitorCreator() {
        return (v, cw) -> new ClassVisitor(v, cw) {
            @Override
            public void visit(final int version, final int access, @Nonnull final String name, @Nullable final String signature,
                              @Nullable final String superName, @Nullable final String[] interfaces) {
                // Don't do this in your transformers, thanks
                throw new IllegalStateException("THIS TRANSFORMER NEEDS TO STAY DISABLED YOU IDIOT");
            }
        };
    }
}
