package net.thesilkminer.mc.fermion.asm.api.transformer;

import com.google.gson.JsonObject;
import net.thesilkminer.mc.fermion.asm.api.configuration.TransformerConfiguration;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import org.objectweb.asm.ClassVisitor;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public interface Transformer {

    @Nonnull TransformerData getData();
    @Nonnull Set<ClassDescriptor> getClassesToTransform();
    @Nonnull Supplier<TransformerConfiguration> provideConfiguration();
    @Nonnull BiFunction<Integer, ClassVisitor, ClassVisitor> getClassVisitorCreator();

    default void applyConfiguration(@Nonnull final JsonObject configuration) {
        this.provideConfiguration().get().getDeserializer().accept(configuration);
    }
}
