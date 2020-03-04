package net.thesilkminer.mc.fermion.test.asm.transformer;

import com.google.common.collect.ImmutableList;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;
import net.thesilkminer.mc.fermion.asm.api.MappingUtilities;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.descriptor.MethodDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.prefab.transformer.SingleTargetMethodTransformer;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.MethodVisitor;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

public final class TestMethodNameRemappingTransformer extends SingleTargetMethodTransformer {

    public TestMethodNameRemappingTransformer(@Nonnull final LaunchPlugin owner) {
        super(
                TransformerData.Builder.create()
                        .setOwningPlugin(owner)
                        .setName("test_method_name_remapping_transformer")
                        .setDescription("Tests where method names get correctly remapped from SRG to MCP if needed. Fields are handled in 'test_hooking_vanilla_transformer'")
                        .setDisabledByDefault()
                        .build(),
                ClassDescriptor.of("net.minecraft.block.Block"),
                MethodDescriptor.of(
                        "func_180652_a",
                        ImmutableList.of(
                                ClassDescriptor.of("net.minecraft.world.World"),
                                ClassDescriptor.of("net.minecraft.util.math.BlockPos"),
                                ClassDescriptor.of("net.minecraft.world.Explosion")
                        ),
                        ClassDescriptor.of(void.class)
                )
        );
    }

    @Nonnull
    @Override
    protected BiFunction<Integer, MethodVisitor, MethodVisitor> getMethodVisitorCreator() {
        return (v, mv) -> new MethodVisitor(v, mv) {
            @Override
            public void visitCode() {
                super.visitCode();
                // This transformer doesn't do anything but log a message to say that the transformation actually worked
                LogManager.getLogger("TestMethodRemappingTransformer/Visitor")
                        .info("Successfully found method 'func_180652_a' (remapped to '" + MappingUtilities.INSTANCE.mapMethod("func_180652_a") + "'): remapping works!");
            }
        };
    }
}
