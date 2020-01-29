package net.thesilkminer.mc.fermion.asm.prefab.transformer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.thesilkminer.mc.fermion.asm.api.MappingUtilities;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.descriptor.MethodDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.prefab.AbstractTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Transformer that automatically makes the access of the given method public
 * and allows external code to access it through a given method (called
 * "accessor method").
 *
 * <p>In other words, this is a sort of Access Transformer that is implemented
 * using raw bytecode manipulation inside of a Fermion environment rather than
 * using environment-dependent solutions (such as Forge's {@code xxx_at.cfg}
 * file.</p>
 *
 * <p>To ensure safety in this implementation, most of the methods that need
 * to be untouched have been made non-virtual and non-overridable (i.e.
 * final). All the added methods, that represent this transformer's public
 * API are documented in depth.</p>
 *
 * @since 1.0.0
 */
public abstract class RuntimeMethodAccessTransformer extends AbstractTransformer {

    /**
     * Describes one of the target fields of the
     * {@link RuntimeMethodAccessTransformer}.
     *
     * <p>This descriptor stores the method that should be made public, along
     * with the class where the method is located, and the "accessor" method,
     * along with the class where it is located.</p>
     *
     * <p>Note that instances of this class do not cause class loading neither
     * during normal usage or construction, if used properly.</p>
     *
     * @since 1.0.0
     */
    protected static final class TargetDescriptor {

        /**
         * A builder used to create instances of a {@link TargetDescriptor}.
         *
         * <p>Builder instances can be reused, as in their {@link #build()}
         * method can be called multiple times to build multiple target
         * descriptors.</p>
         *
         * @since 1.0.0
         */
        public static final class Builder {

            private ClassDescriptor methodClass;
            private MethodDescriptor method;
            private boolean isTargetMethodStatic;
            private ClassDescriptor accessorClass;
            private MethodDescriptor accessor;

            private Builder() {
            }

            /**
             * Creates a new builder instance to construct an instance of a
             * {@link TargetDescriptor}.
             *
             * <p>No properties are populated with default values. Rather, they
             * all require explicit initialization before calling
             * {@link #build()}.</p>
             *
             * @return
             *      A new, ready to be used, builder instance.
             *
             * @since 1.0.0
             */
            @Nonnull
            public static Builder create() {
                return new Builder();
            }

            @Nonnull
            ClassDescriptor getMethodClass() {
                return this.methodClass;
            }

            @Nonnull
            MethodDescriptor getMethod() {
                return this.method;
            }

            boolean isTargetMethodStatic() {
                return this.isTargetMethodStatic;
            }

            @Nonnull
            ClassDescriptor getAccessorClass() {
                return this.accessorClass;
            }

            @Nonnull
            MethodDescriptor getAccessor() {
                return this.accessor;
            }

            /**
             * Sets the method that acts as a target for this target
             * descriptor.
             *
             * <p>The "target method" is defined as the method that the AT that
             * has the descriptor as a target will attempt to look up and make
             * public, stripping all the access modifiers that may or may not
             * already be present.</p>
             *
             * @param methodClass
             *      The class where the "target method" is located. It cannot
             *      be null.
             * @param method
             *      The "target method". It cannot be null.
             * @param isStatic
             *      Whether the "target method" is static or not.
             * @return
             *      This builder for chaining.
             * @throws IllegalStateException
             *      If the "target method" has already been set.
             *
             * @implNote
             *      The implementation <strong>must</strong> require that the
             *      object remains in a correct state if one of the arguments
             *      is null. For this reason, it is illegal for a field to be
             *      set prior to having checked both of the arguments of this
             *      method for nullability issues.
             *
             * @since 1.0.0
             */
            @Nonnull
            public Builder setTargetMethod(@Nonnull final ClassDescriptor methodClass, @Nonnull final MethodDescriptor method, final boolean isStatic) {
                if (this.methodClass != null || this.method != null) {
                    throw new IllegalStateException("You can set the target method only once");
                }
                Preconditions.checkNotNull(methodClass);
                this.method = Preconditions.checkNotNull(method);
                this.methodClass = methodClass;
                this.isTargetMethodStatic = isStatic;
                return this;
            }

            /**
             * Sets the non-static method that acts as a target for this target
             * descriptor.
             *
             * <p>The "target method" is defined as the method that the AT that
             * has the descriptor as a target will attempt to look up and make
             * public, stripping all the access modifiers that may or may not
             * already be present.</p>
             *
             * <p>By using this method, it is assumed that the "target method"
             * is non-static. In case this assumption is wrong, you should use
             * the
             * {@link #setTargetMethod(ClassDescriptor, MethodDescriptor, boolean)}
             * instead.</p>
             *
             * @param methodClass
             *      The class where the "target method" is located. It cannot
             *      be null.
             * @param method
             *      The "target method". It cannot be null.
             * @return
             *      This builder for chaining.
             * @throws IllegalStateException
             *      If the "target method" has already been set.
             *
             * @implNote
             *      The implementation <strong>must</strong> require that the
             *      object remains in a correct state if one of the arguments
             *      is null. For this reason, it is illegal for a field to be
             *      set prior to having checked both of the arguments of this
             *      method for nullability issues.
             *
             * @since 1.0.0
             */
            @Nonnull
            public Builder setTargetMethod(@Nonnull final ClassDescriptor methodClass, @Nonnull final MethodDescriptor method) {
                return this.setTargetMethod(methodClass, method, false);
            }

            /**
             * Sets the method that acts as an accessor for the "target method"
             * of this target descriptor.
             *
             * <p>For a definition of "target method", refer to the
             * documentation of
             * {@link #setTargetMethod(ClassDescriptor, MethodDescriptor, boolean)}.</p>
             *
             * <p>The "accessor method" is defined as that method whose body
             * gets overwritten and acts as a bridge between clients of the
             * "target method" and the "target method" itself. This is needed
             * because the transformation occurs at runtime and so it is needed
             * that the code compiles.</p>
             *
             * <p>The "accessor method" needs to respect certain parameters to
             * be considered a valid "accessor method". These parameters are
             * outlines in the paragraphs that follow.</p>
             *
             * <p>First of all, it is mandatory for an "accessor method" to
             * have the same exact return type of the "target method".
             * Subclasses or superclasses are not allowed. E.g., it is
             * illegal to target a method with return type {@code List} and
             * have as the return type in the "accessor method"
             * {@code Collection}.</p>
             *
             * <p>If the "target method" is static, the "accessor method" must
             * have the same amount of parameters, excluding receiver types if
             * present, as the "target method". The parameter types must also
             * match one-to-one with the types of the "target method". More
             * formally, the {@code i}-th parameter of the "accessor method",
             * with {@code i} being between {@code 0} and {@code n} (where
             * {@code n} is the amount of parameters of the "target method"),
             * must have the same type as the {@code i}-th parameter of the
             * "target method".</p>
             *
             * <p>If the "target method" is non-static, the "accessor method"
             * must have one more parameter than the "target method", excluding
             * receiver types if present. The parameter types of the "accessor
             * method" must also match with the parameter types of the "target
             * method" in the following manner. Assume {@code n} is the amount
             * of parameters of the "target method" and {@code m} is the amount
             * of parameters of the "accessor method". Then for every {@code i}
             * between {@code 0} and {@code n}, there must be a {@code j}
             * between {@code 1} and {@code m} that points to a parameter that
             * has the same type as the one pointed by {@code i}. Moreover, the
             * value of {@code j} must exactly match {@code i + 1}, otherwise
             * an error occurs. Last but not least, the first parameter of the
             * "accessor method" must be of the same type of the class that
             * holds the "target method", excluding any possible receiver
             * parameters. It cannot be neither a superclass nor a subclass,
             * otherwise an error occurs. E.g., it is illegal to try to access
             * a non-static method of the class {@code AbstractList} and
             * declare the first parameter of the "accessor method" as an
             * {@code ArrayList}.</p>
             *
             * @param accessorClass
             *      The class where the "accessor method" is located. It cannot
             *      be null.
             * @param accessor
             *      The "accessor method". It cannot be null.
             * @return
             *      This builder for chaining.
             * @throws IllegalStateException
             *      If the "accessor method" has already been set.
             *
             * @implNote
             *      The implementation <strong>must</strong> require that the
             *      object remains in a correct state if one of the arguments
             *      is null. For this reason, it is illegal for a field to be
             *      set prior to having checked both of the arguments of this
             *      method for nullability issues.
             *
             * @since 1.0.0
             */
            @Nonnull
            public Builder setAccessorMethod(@Nonnull final ClassDescriptor accessorClass, @Nonnull final MethodDescriptor accessor) {
                if (this.accessorClass != null || this.accessor != null) {
                    throw new IllegalStateException("You can set the accessor method only once");
                }
                Preconditions.checkNotNull(accessorClass);
                this.accessor = Preconditions.checkNotNull(accessor);
                this.accessorClass = accessorClass;
                return this;
            }

            /**
             * Builds a new instance of {@link TargetDescriptor} with the
             * provided data.
             *
             * @return
             *      A new instance of {@link TargetDescriptor}. Guaranteed not
             *      to be null.
             * @throws NullPointerException
             *      If one or more of the required properties hasn't been set
             *      previously.
             * @throws IllegalArgumentException
             *      If either the "accessor method" does not respect the
             *      correct structure or it is the same as the "target method".
             *      Refer to
             *      {@link #setAccessorMethod(ClassDescriptor, MethodDescriptor)}
             *      for more information.
             *
             * @since 1.0.0
             */
            @Nonnull
            public TargetDescriptor build() {
                Preconditions.checkNotNull(this.accessor, "Accessor method cannot be null");
                Preconditions.checkNotNull(this.accessorClass, "Class where the accessor method is cannot be null");
                Preconditions.checkNotNull(this.method, "Method to access cannot be null");
                Preconditions.checkNotNull(this.methodClass, "Class where the method is located cannot be null");

                final ClassDescriptor methodReturnType = this.method.getReturnType();
                final ClassDescriptor accessorReturnType = this.accessor.getReturnType();
                Preconditions.checkArgument(methodReturnType.equals(accessorReturnType), "Accessor return type must be the same as the method return type");

                final List<ClassDescriptor> methodArguments = this.method.getArguments();
                final List<ClassDescriptor> accessorArguments = this.accessor.getArguments();
                final int expectedSize = methodArguments.size() + (this.isTargetMethodStatic ? 0 : 1);

                Preconditions.checkArgument(accessorArguments.size() == expectedSize, this.isTargetMethodStatic ?
                        "Wrong arguments: accessor method must have the same amount of arguments if the target method is static" :
                        "Wrong arguments: accessor method must have one more argument than the target method if the target method is non-static");

                for (int i = 0; i < methodArguments.size(); i++) {
                    for (int j = (this.isTargetMethodStatic ? 0 : 1); j < accessorArguments.size(); j++) {
                        final ClassDescriptor methodArgumentI = methodArguments.get(i);
                        final ClassDescriptor accessorArgumentJ = accessorArguments.get(j);

                        Preconditions.checkArgument(methodArgumentI.equals(accessorArgumentJ),
                                "The argument #" + j + " of the accessor method must match in type the argument #" + i + " of the target method");
                    }
                }

                Preconditions.checkArgument(!(this.methodClass.equals(this.accessorClass) && this.method.equals(this.accessor)),
                        "A method cannot be the accessor of itself");

                return new TargetDescriptor(this);
            }
        }

        private final ClassDescriptor methodClass;
        private final MethodDescriptor method;
        private final boolean isTargetMethodStatic;
        private final ClassDescriptor accessorClass;
        private final MethodDescriptor accessor;

        private TargetDescriptor(@Nonnull final TargetDescriptor.Builder from) {
            this.methodClass = from.getMethodClass();
            this.method = from.getMethod();
            this.isTargetMethodStatic = from.isTargetMethodStatic();
            this.accessorClass = from.getAccessorClass();
            this.accessor = from.getAccessor();
        }

        @Nonnull
        ClassDescriptor getMethodClass() {
            return this.methodClass;
        }

        @Nonnull
        MethodDescriptor getMethod() {
            return this.method;
        }

        boolean isTargetMethodStatic() {
            return this.isTargetMethodStatic;
        }

        @Nonnull
        ClassDescriptor getAccessorClass() {
            return this.accessorClass;
        }

        @Nonnull
        MethodDescriptor getAccessor() {
            return this.accessor;
        }

        @Nonnull
        @Override
        public String toString() {
            return "TargetDescriptor{" +
                    "methodClass=" + this.methodClass +
                    ", method=" + this.method +
                    ", accessorClass=" + this.accessorClass +
                    ", accessor=" + this.accessor +
                    '}';
        }

        @Override
        public boolean equals(@Nullable final Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            final TargetDescriptor that = (TargetDescriptor) o;
            return Objects.equals(this.methodClass, that.methodClass) &&
                    Objects.equals(this.method, that.method) &&
                    Objects.equals(this.accessorClass, that.accessorClass) &&
                    Objects.equals(this.accessor, that.accessor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.methodClass, this.method, this.accessorClass, this.accessor);
        }
    }

    private static final class MethodCallerVisitor extends MethodVisitor {
        private final MethodVisitor visitor;
        private final TargetDescriptor descriptor;
        private final boolean isAccessorMethodStatic;

        private MethodCallerVisitor(final int api, @Nonnull final MethodVisitor methodVisitor,
                                    @Nonnull final TargetDescriptor targetDescriptor, final boolean isStatic) {
            super(api, null);
            this.visitor = methodVisitor;
            this.descriptor = targetDescriptor;
            this.isAccessorMethodStatic = isStatic;
        }

        @Override
        public void visitCode() {
            this.visitor.visitCode();

            final Label l0 = new Label();
            this.visitor.visitLabel(l0);

            final int paramIdPlus = this.isAccessorMethodStatic? 0 : 1;

            if (!this.isAccessorMethodStatic) this.visitor.visitVarInsn(Opcodes.ALOAD, 0);

            final List<ClassDescriptor> args = this.descriptor.getAccessor().getArguments();
            for (int i = 0; i < args.size(); i++) {
                this.visitor.visitVarInsn(this.getLoadOpcodeFromType(args.get(i)), i + paramIdPlus);
            }

            this.visitor.visitMethodInsn(
                    this.descriptor.isTargetMethodStatic()? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL,
                    this.descriptor.getMethodClass().toAsmName(),
                    this.descriptor.getMethod().getName(),
                    this.descriptor.getMethod().toAsmDescriptor(),
                    false);
            this.visitor.visitInsn(this.getReturnOpcodeFromType(this.descriptor.getMethod().getReturnType()));

            final Label l1 = new Label();
            this.visitor.visitLabel(l1);

            if (!this.isAccessorMethodStatic) {
                this.visitor.visitLocalVariable("this", this.descriptor.getAccessorClass().toAsmMethodDescriptor(),
                        null, l0, l1, 0);
            }
            if (!this.descriptor.isTargetMethodStatic()) {
                this.visitor.visitLocalVariable("that", this.descriptor.getMethodClass().toAsmMethodDescriptor(),
                        null, l0, l1, paramIdPlus);
            }

            for (int i = 0; i < args.size(); ++i) {
                this.visitor.visitLocalVariable("param" + i, args.get(i).toAsmMethodDescriptor(), null,
                        l0, l1, i + paramIdPlus + (this.descriptor.isTargetMethodStatic()? 0 : 1));
            }

            this.visitor.visitMaxs(args.size() + 2, args.size() + 2);
            this.visitor.visitEnd();
        }

        @Override
        public AnnotationVisitor visitAnnotation(@Nonnull final String descriptor, boolean visible) {
            return this.visitor.visitAnnotation(descriptor, visible);
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(final int parameter, @Nonnull final String descriptor, final boolean visible) {
            return this.visitor.visitParameterAnnotation(parameter, descriptor, visible);
        }

        private int getLoadOpcodeFromType(@Nonnull final ClassDescriptor descriptor) {
            switch (descriptor.toAsmMethodDescriptor().toCharArray()[0]) {
                case 'V': throw new IllegalArgumentException("Unable to load a void value type");
                case 'Z': case 'C': case 'B': case 'S': case 'I': return Opcodes.ILOAD;
                case 'F': return Opcodes.FLOAD;
                case 'J': return Opcodes.LLOAD;
                case 'D': return Opcodes.DLOAD;
                case 'L': return Opcodes.ALOAD;
                default: throw new IllegalStateException("Not a valid descriptor");
            }
        }

        private int getReturnOpcodeFromType(@Nonnull final ClassDescriptor descriptor) {
            switch (descriptor.toAsmMethodDescriptor().toCharArray()[0]) {
                case 'V': return Opcodes.RETURN;
                case 'Z': case 'C': case 'B': case 'S': case 'I': return Opcodes.IRETURN;
                case 'F': return Opcodes.FRETURN;
                case 'J': return Opcodes.LRETURN;
                case 'D': return Opcodes.DRETURN;
                case 'L': return Opcodes.ARETURN;
                default: throw new IllegalStateException("Not a valid descriptor");
            }
        }
    }

    private static final Logger LOGGER = LogManager.getLogger("RuntimeFieldAccessTransformer");

    private final Marker marker;
    private final Set<TargetDescriptor> descriptors;

    /**
     * Constructs a new instance of this transformer.
     *
     * @param data
     *      The data that identifies this transformer. Refer to
     *      {@link TransformerData} for more information. It cannot be null.
     * @param descriptors
     *      A list of {@link TargetDescriptor}s that identifies the couples of
     *      "target method" and "accessor method" that this transformer should
     *      target. There must be at least one target descriptor.
     *
     * @since 1.0.0
     */
    protected RuntimeMethodAccessTransformer(@Nonnull final TransformerData data, @Nonnull final TargetDescriptor... descriptors) {
        super(data, getClassesFromTargets(descriptors));
        this.descriptors = ImmutableSet.copyOf(new HashSet<>(Arrays.asList(descriptors)));
        this.marker = MarkerManager.getMarker(data.getOwningPluginId() + ":" + data.getName());
    }

    @Nonnull
    private static ClassDescriptor[] getClassesFromTargets(@Nonnull final TargetDescriptor... descriptors) {
        if (descriptors.length == 0) return new ClassDescriptor[0];
        // Each target stores two classes. They may be the same, but who cares?
        // The AbstractTransformer uses a set for this exact purpose
        final ClassDescriptor[] targetArray = new ClassDescriptor[descriptors.length * 2];
        /* mutable */
        int j = 0;
        for (@Nonnull final TargetDescriptor descriptor : descriptors) {
            targetArray[j] = descriptor.getAccessorClass();
            ++j;
            targetArray[j] = descriptor.getMethodClass();
            ++j;
        }
        return targetArray;
    }

    @Nonnull
    @Override
    public final BiFunction<Integer, ClassVisitor, ClassVisitor> getClassVisitorCreator() {
        return (v, cw) -> new ClassVisitor(v, cw) {
            private final Map<TargetDescriptor, Boolean> targetDescriptors = Maps.newIdentityHashMap();

            @Override
            public void visit(final int version, final int access, @Nonnull final String name, @Nullable final String signature,
                              @Nonnull final String superName, @Nullable final String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);

                final ClassDescriptor descriptor = ClassDescriptor.of(name);

                for (@Nonnull final TargetDescriptor desc : RuntimeMethodAccessTransformer.this.descriptors) {
                    final ClassDescriptor methodClass = desc.getMethodClass();
                    final ClassDescriptor accessorClass = desc.getAccessorClass();

                    if (descriptor.equals(methodClass)) {
                        this.targetDescriptors.put(desc, true);
                        LOGGER.info(RuntimeMethodAccessTransformer.this.marker,
                                "Found class '" + name + "' matching target method '" + desc.getMethod().getName() + "'");
                    }
                    if (descriptor.equals(accessorClass)) {
                        this.targetDescriptors.put(desc, false);
                        LOGGER.info(RuntimeMethodAccessTransformer.this.marker,
                                "Found class '" + name + "' matching accessor '" + desc.getAccessor().toString() + "'");
                    }
                }
            }

            @Nullable
            @Override
            public MethodVisitor visitMethod(final int access, @Nonnull final String name, @Nonnull final String descriptor,
                                             @Nullable final String signature, @Nonnull final String[] exceptions) {

                final Type methodType = Type.getType(descriptor);
                final Type[] argumentTypes = methodType.getArgumentTypes();
                final Type returnType = methodType.getReturnType();

                final ClassDescriptor returnDesc = ClassDescriptor.of(returnType);
                final List<ClassDescriptor> arguments = Arrays.stream(argumentTypes)
                        .map(ClassDescriptor::of)
                        .collect(Collectors.toList());

                final MethodDescriptor methodDescriptor = MethodDescriptor.of(name, arguments, returnDesc);

                final int newAccess = this.checkForAccessTransformation(access, methodDescriptor);

                if (newAccess != access) {
                    // This is a method to transform, and a method cannot be the accessor of itself, so...
                    LOGGER.info(RuntimeMethodAccessTransformer.this.marker,
                            "Made method '" + methodDescriptor.toString() + "' public (new access: " + newAccess + ")");
                    return super.visitMethod(newAccess, name, descriptor, signature, exceptions);
                }

                // No method transformed: let's keep on going
                final MethodVisitor parent = super.visitMethod(access, name, descriptor, signature, exceptions);

                final BiFunction<Integer, MethodVisitor, MethodVisitor> getMethodVisitor =
                        this.getVisitorForMethod(access, methodDescriptor);

                final MethodVisitor methodVisitor = getMethodVisitor.apply(v, parent);

                return methodVisitor == null? parent : methodVisitor;
            }

            private int checkForAccessTransformation(final int access, @Nonnull final MethodDescriptor descriptor) {
                final Optional<MethodDescriptor> opt = this.targetDescriptors.entrySet().stream()
                        .filter(Map.Entry::getValue)
                        .map(Map.Entry::getKey)
                        .filter(it -> ((access & Opcodes.ACC_STATIC) != 0 && it.isTargetMethodStatic())
                                || ((access & Opcodes.ACC_STATIC) == 0 && !it.isTargetMethodStatic()))
                        .map(TargetDescriptor::getMethod)
                        .map(this::remapNameIfNeeded)
                        .filter(it -> Objects.equals(it, descriptor))
                        .findAny();

                return opt.isPresent()? ((access & ~Opcodes.ACC_PRIVATE) & ~Opcodes.ACC_PROTECTED) | Opcodes.ACC_PUBLIC : access;
            }

            @Nonnull
            private BiFunction<Integer, MethodVisitor, MethodVisitor> getVisitorForMethod(final int access, final MethodDescriptor descriptor) {
                // Accessor methods won't be remapped because you're supposed to be able to access them since it is
                // your own mod code
                final Optional<TargetDescriptor> opt = this.targetDescriptors.entrySet().stream()
                        .filter(it -> !it.getValue())
                        .map(Map.Entry::getKey)
                        .filter(it -> Objects.equals(it.getAccessor(), descriptor))
                        .findAny();

                if (opt.isPresent()) {
                    final TargetDescriptor found = opt.get();

                    LOGGER.info(RuntimeMethodAccessTransformer.this.marker,
                            "Found accessor method '" + descriptor.toString() + "': overwriting with access code");

                    return (v, mv) -> new MethodCallerVisitor(v, mv, found, (access & Opcodes.ACC_STATIC) != 0);
                }

                return (v, mv) -> null;
            }

            @Nonnull
            private MethodDescriptor remapNameIfNeeded(@Nonnull final MethodDescriptor in) {
                return MethodDescriptor.of(MappingUtilities.INSTANCE.mapField(in.getName()), in.getArguments(), in.getReturnType());
            }
        };
    }
}
