package net.thesilkminer.mc.fermion.test.asm.transformer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.descriptor.MethodDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.prefab.transformer.TargetMethodTransformer;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.BiFunction;

public final class TestTargetMethodTransformer extends TargetMethodTransformer {

    private static final Logger LOGGER = LogManager.getLogger("TestTargetMethodTransformer");

    private static final MethodDescriptor SETUP_METHOD_DESCRIPTOR = MethodDescriptor.of("setup",
            ImmutableList.of(ClassDescriptor.of("net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent")),
            ClassDescriptor.of(void.class));
    private static final MethodDescriptor CONSTRUCTOR = MethodDescriptor.of("<init>",
            ImmutableList.of(),
            ClassDescriptor.of(void.class));

    public TestTargetMethodTransformer(@Nonnull final LaunchPlugin owner) {
        super(
                TransformerData.Builder.create()
                        .setOwningPlugin(owner)
                        .setName("test_target_method_transformer")
                        .setDescription("This is a test for the Target Method Transformer prefab")
                        .setDisabledByDefault()
                        .build(),
                ClassDescriptor.of("net.thesilkminer.mc.fermion.Fermion"),
                SETUP_METHOD_DESCRIPTOR, CONSTRUCTOR
        );
    }

    @Nonnull
    @Override
    protected Map<MethodDescriptor, BiFunction<MethodDescriptor, Pair<Integer, MethodVisitor>, MethodVisitor>> getMethodVisitorCreators() {
        return ImmutableMap.of(
                SETUP_METHOD_DESCRIPTOR, (desc, pair) -> new MethodVisitor(pair.getLeft(), pair.getRight()) {
                    @Override
                    public void visitLdcInsn(@Nonnull final Object value) {
                        if (value instanceof String) {
                            final String ldcConstant = (String) value;
                            if ("FMLCommonSetupEvent".equalsIgnoreCase(ldcConstant)) {
                                super.visitLdcInsn(ldcConstant + " (Hey there, TestTargetMethodTransformer here)");
                                return;
                            }
                        }
                        super.visitLdcInsn(value);
                    }
                },
                CONSTRUCTOR, (desc, pair) -> new MethodVisitor(pair.getLeft(), pair.getRight()) {
                    private boolean hasSeenLoggerGetStatic = false;

                    @Override
                    public void visitFieldInsn(final int opcode, @Nonnull final String owner, @Nonnull final String name,
                                               @Nonnull final String descriptor) {
                        if (!this.hasSeenLoggerGetStatic) {
                            super.visitFieldInsn(opcode, owner, name, descriptor);
                            if (opcode == Opcodes.GETSTATIC && "LOGGER".equals(name)) {
                                this.hasSeenLoggerGetStatic = true;
                            }
                        } else {
                            if (opcode == Opcodes.GETSTATIC && "MARKER".equals(name)) {
                                super.visitFieldInsn(opcode, owner, "TRANSFORMER_MARKER", descriptor);
                                LOGGER.info("Replaced GETSTATIC for 'MARKER' with transformed version in method " + desc + " successfully");
                            } else {
                                super.visitFieldInsn(opcode, owner, name, descriptor);
                            }
                            this.hasSeenLoggerGetStatic = false;
                        }
                    }
                }
        );
    }
}
