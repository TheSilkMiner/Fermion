package net.thesilkminer.mc.fermion.asm.common;

import com.google.common.collect.ImmutableSet;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import net.thesilkminer.mc.fermion.asm.common.utility.LaunchBlackboard;
import net.thesilkminer.mc.fermion.asm.common.utility.Log;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nonnull;
import java.util.Set;

public final class FermionTransformer implements ITransformer<ClassNode> {

    private static final Log LOGGER = Log.of("Transformer");

    private final LaunchBlackboard blackboard;

    FermionTransformer(@Nonnull final LaunchBlackboard blackboard) {
        this.blackboard = blackboard;
    }

    @Nonnull
    @Override
    public ClassNode transform(@Nonnull final ClassNode input, @Nonnull final ITransformerVotingContext context) {
        LOGGER.d("Transforming " + input.name);
        return input;
    }

    @Nonnull
    @Override
    public TransformerVoteResult castVote(@Nonnull final ITransformerVotingContext context) {
        // TODO
        return TransformerVoteResult.YES;
    }

    @Nonnull
    @Override
    public Set<Target> targets() {
        return ImmutableSet.of();
    }
}
