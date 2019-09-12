package net.thesilkminer.mc.fermion.companion.asm.transformer.vanity;

import com.google.common.collect.ImmutableList;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.descriptor.MethodDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.prefab.transformer.SingleTargetMethodTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

public final class BackToSingleThreadsTransformer extends SingleTargetMethodTransformer {

    private static final Logger LOGGER = LogManager.getLogger("fermion.asm");
    private static final Marker MARKER = MarkerManager.getMarker("Back To Single Threads!");

    public BackToSingleThreadsTransformer() {
        super(
                TransformerData.Builder.create()
                        .setOwningPluginId("fermion.asm")
                        .setName("vanity_back_to_single_threads")
                        .setDescription("If enabled, this transformer makes the entire loading process go back to a single thread instead of being multi-threaded")
                        .setDisabledByDefault()
                        .build(),
                ClassDescriptor.of("net.minecraftforge.fml.ModList"),
                MethodDescriptor.of("<init>",
                        ImmutableList.of(ClassDescriptor.of("java.util.List"), ClassDescriptor.of("java.util.List")),
                        ClassDescriptor.of(void.class))
        );
    }

    @Nonnull
    @Override
    protected BiFunction<Integer, MethodVisitor, MethodVisitor> getMethodVisitorCreator() {
        return (v, mv) -> new MethodVisitor(v, mv) {
            @Override
            public void visitMethodInsn(final int opcode, @Nonnull final String owner, @Nonnull final String name,
                                        @Nonnull final String descriptor, final boolean isInterface) {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

                if (opcode == Opcodes.INVOKESTATIC && "net/minecraftforge/fml/loading/FMLConfig".equals(owner)
                        && "loadingThreadCount".equals(name) && "()I".equals(descriptor)) {
                    LOGGER.info(MARKER, "Found INVOKESTATIC to FMLConfig.loadingThreadCount: injecting");
                    super.visitInsn(Opcodes.POP);
                    super.visitInsn(Opcodes.ICONST_1);
                    LOGGER.info(MARKER, "Injection done");
                }
            }
        };
    }
}
