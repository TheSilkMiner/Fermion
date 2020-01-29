/*
 * Copyright (C) 2020  TheSilkMiner
 *
 * This file is part of Fermion.
 *
 * Fermion is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Fermion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Fermion.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact information:
 * E-mail: thesilkminer <at> outlook <dot> com
 */

package net.thesilkminer.mc.fermion.asm.prefab;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import net.thesilkminer.mc.fermion.asm.api.configuration.TransformerConfiguration;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.Transformer;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import org.objectweb.asm.ClassVisitor;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * This class provides a skeletal implementation of the {@link Transformer}
 * interface to minimize the efforts needed.
 *
 * <p>To create a transformer, the user should just extend this class and
 * implement the {@link #getClassVisitorCreator()} method. Also, the
 * {@link TransformerData} instance and the class targets should be passed
 * in through the constructor.</p>
 *
 * <p>To ensure safety in usage of this transformer implementation, most of
 * the methods have been marked final, i.e. non-virtual and non extendable. All
 * the other methods that were either added or left to the user to implement
 * have documentation that describes their implementation in the respective
 * section. Added methods are instead simply documented.</p>
 *
 * <p>It is highly suggested for clients of this library to extend this class
 * instead of directly implementing {@code Transformer}. It is nevertheless not
 * a hard requirement and implementations must not special case this type of
 * transformers or assume that all transformers that get registered extend this
 * class.</p>
 *
 * @since 1.0.0
 */
public abstract class AbstractTransformer implements Transformer {

    private final TransformerData data;
    private final Set<ClassDescriptor> targets;

    /**
     * Constructs a new instance of this abstract transformer.
     *
     * @param data
     *      The data that identifies this transformer. It must be complete in
     *      all its parts. Refer to {@link TransformerData} for more
     *      information. It cannot be null.
     * @param targets
     *      The {@link ClassDescriptor}s representing the targets that this
     *      transformer aims to transform. There must be at least one.
     *
     * @since 1.0.0
     */
    protected AbstractTransformer(@Nonnull final TransformerData data, @Nonnull final ClassDescriptor... targets) {
        this.data = Preconditions.checkNotNull(data);
        Preconditions.checkArgument(Preconditions.checkNotNull(targets).length > 0, "At least one target must be given");
        this.targets = new HashSet<>(Arrays.asList(targets));
    }

    @Nonnull
    @Override
    public final TransformerData getData() {
        return this.data;
    }

    @Nonnull
    @Override
    public final Set<ClassDescriptor> getClassesToTransform() {
        return ImmutableSet.copyOf(this.targets);
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec
     *      This implementation provides by default a new empty
     *      {@link TransformerConfiguration} every time it is called. This
     *      defines a transformer which provides no special configuration other
     *      than enabling or disabling.
     *
     * @since 1.0.0
     */
    @Nonnull
    @Override
    public Supplier<TransformerConfiguration> provideConfiguration() {
        return () -> TransformerConfiguration.Builder.create().build();
    }

    @Nonnull
    @Override
    public abstract BiFunction<Integer, ClassVisitor, ClassVisitor> getClassVisitorCreator();
}
