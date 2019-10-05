package net.thesilkminer.mc.fermion.asm.prefab.transformer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.thesilkminer.mc.fermion.asm.api.MappingUtilities;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.descriptor.FieldDescriptor;
import net.thesilkminer.mc.fermion.asm.api.descriptor.MethodDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.prefab.AbstractTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
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
 * Transformer that automatically makes the access of the given field public
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
public abstract class RuntimeFieldAccessTransformer extends AbstractTransformer {

    /**
     * Describes one of the target fields of the
     * {@link RuntimeFieldAccessTransformer}.
     *
     * <p>This descriptor stores the field that should be made public, along
     * with the class where the field is located, and the "accessor" method,
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

            private ClassDescriptor fieldClass;
            private FieldDescriptor field;
            private ClassDescriptor accessorClass;
            private MethodDescriptor accessor;

            private Builder() {}

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
            ClassDescriptor getFieldClass() {
                return this.fieldClass;
            }

            @Nonnull
            FieldDescriptor getField() {
                return this.field;
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
             * Sets the field that acts as a target for this target descriptor.
             *
             * <p>The "target field" is defined as the field that the AT that
             * has the descriptor as a target will attempt to look up and make
             * public, stripping all the access modifiers that may or may not
             * already be present.</p>
             *
             * @param fieldClass
             *      The class where the "target field" is located. It cannot be
             *      null.
             * @param field
             *      The "target field". It cannot be null.
             * @return
             *      This builder for chaining.
             * @throws IllegalStateException
             *      If the "target field" has already been set.
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
            public Builder setTargetField(@Nonnull final ClassDescriptor fieldClass, @Nonnull final FieldDescriptor field) {
                if (this.fieldClass != null || this.field != null) {
                    throw new IllegalStateException("You can set the target field only once");
                }
                Preconditions.checkNotNull(fieldClass);
                this.field = Preconditions.checkNotNull(field);
                this.fieldClass = fieldClass;
                return this;
            }

            /**
             * Sets the method that acts as an accessor for the "target field"
             * of this target descriptor.
             *
             * <p>For a definition of "target field", refer to the
             * documentation of
             * {@link #setTargetField(ClassDescriptor, FieldDescriptor)}.</p>
             *
             * <p>The "accessor method" is defined as that method whose body
             * gets overwritten and acts as a bridge between clients of the
             * "target field" and the "target field" itself. This is needed
             * because the transformation occurs at runtime and so it is needed
             * that the code compiles.</p>
             *
             * <p>The "accessor method" needs to respect certain parameters to
             * be considered a valid "accessor method". These parameters are
             * outlines in the paragraphs that follow.</p>
             *
             * <p>First of all, it is mandatory for an "accessor method" to
             * have the same exact return type of the "target field".
             * Subclasses or superclasses are not allowed, so it is not
             * possible, e.g., to target a field of type {@code List} and
             * reference it in the "accessor method" as a {@code Collection}
             * type.</p>
             *
             * <p>If the "target field" is static, the "accessor method" must
             * have an empty parameter list (also known as, it cannot have
             * parameters). The receiver parameter is allowed, but only if the
             * "accessor" method is non-static. Its presence must anyway not
             * be specified in its descriptor.</p>
             *
             * <p>If the "target field" is non-static, the "accessor method"
             * must have exactly one parameter, excluding the receiver
             * parameter if present. The receiver parameter must anyway not be
             * specified in the "accessor method" descriptor. The type of the
             * "actual parameter" (i.e. the parameter that is not the receiver
             * parameter) must match exactly the "target field"'s class. It
             * cannot be neither a superclass nor a subclass, otherwise an
             * error occurs. E.g., it is illegal to try to access a non-static
             * field of the class {@code AbstractList} and declare the
             * "actual parameter" of the "accessor method" as a
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
             *      If the "accessor method" does not respect the correct
             *      structure. Refer to
             *      {@link #setAccessorMethod(ClassDescriptor, MethodDescriptor)}
             *      for more information.
             *
             * @since 1.0.0
             */
            @Nonnull
            public TargetDescriptor build() {
                Preconditions.checkNotNull(this.accessor, "Accessor method cannot be null");
                Preconditions.checkNotNull(this.accessorClass, "Class where the accessor method is cannot be null");
                Preconditions.checkNotNull(this.field, "Field to access cannot be null");
                Preconditions.checkNotNull(this.fieldClass, "Class where the field is located cannot be null");

                final ClassDescriptor fieldType = this.field.getType();
                final ClassDescriptor returnType = this.accessor.getReturnType();
                Preconditions.checkArgument(fieldType.equals(returnType), "Accessor return type must be the same as the field type");

                final boolean fieldStatic = this.field.isStatic();
                final List<ClassDescriptor> arguments = this.accessor.getArguments();
                final int expectedSize = fieldStatic? 0 : 1;
                final ClassDescriptor expectedClassDescriptor = fieldStatic? null : this.fieldClass;

                Preconditions.checkArgument(arguments.size() == expectedSize, fieldStatic?
                        "Wrong arguments: accessor method must have 0 arguments if field to access is static" :
                        "Wrong arguments: accessor method must have 1 argument if field to access is non-static");
                if (expectedSize != 0) {
                    Preconditions.checkArgument(expectedClassDescriptor.equals(arguments.get(0)),
                            "Wrong type: accessor method parameter must be the same as the field's class");
                }

                return new TargetDescriptor(this);
            }
        }

        private final ClassDescriptor fieldClass;
        private final FieldDescriptor field;
        private final ClassDescriptor accessorClass;
        private final MethodDescriptor accessor;

        private TargetDescriptor(@Nonnull final TargetDescriptor.Builder from) {
            this.fieldClass = from.getFieldClass();
            this.field = from.getField();
            this.accessorClass = from.getAccessorClass();
            this.accessor = from.getAccessor();
        }

        @Nonnull
        ClassDescriptor getFieldClass() {
            return this.fieldClass;
        }

        @Nonnull
        FieldDescriptor getField() {
            return this.field;
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
                    "fieldClass=" + this.fieldClass +
                    ", field=" + this.field +
                    ", accessorClass=" + this.accessorClass +
                    ", accessor=" + this.accessor +
                    '}';
        }

        @Override
        public boolean equals(@Nullable final Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            final TargetDescriptor that = (TargetDescriptor) o;
            return Objects.equals(this.fieldClass, that.fieldClass) &&
                    Objects.equals(this.field, that.field) &&
                    Objects.equals(this.accessorClass, that.accessorClass) &&
                    Objects.equals(this.accessor, that.accessor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.fieldClass, this.field, this.accessorClass, this.accessor);
        }
    }

    private static final class MethodCallerVisitor extends MethodVisitor {
        private final MethodVisitor visitor;
        private final TargetDescriptor descriptor;
        private final boolean isMethodStatic;
        private final boolean isFieldStatic;

        private MethodCallerVisitor(final int api, @Nonnull final MethodVisitor methodVisitor,
                                    @Nonnull final TargetDescriptor targetDescriptor, final boolean isStatic) {
            super(api, null);
            this.visitor = methodVisitor;
            this.descriptor = targetDescriptor;
            this.isMethodStatic = isStatic;
            this.isFieldStatic = targetDescriptor.getField().isStatic();
        }

        @Override
        public void visitCode() {
            this.visitor.visitCode();
            final Label l0 = new Label();
            this.visitor.visitLabel(l0);
            if (!this.isFieldStatic) this.visitor.visitVarInsn(Opcodes.ALOAD, this.isMethodStatic ? 0 : 1);
            this.visitor.visitFieldInsn(this.isFieldStatic? Opcodes.GETSTATIC : Opcodes.GETFIELD,
                    this.descriptor.getFieldClass().toAsmName(),
                    this.descriptor.getField().getName(),
                    this.descriptor.getField().getType().toAsmMethodDescriptor());
            this.visitor.visitInsn(this.getReturnOpcodeFromType(this.descriptor.getField().getType()));
            final Label l1 = new Label();
            this.visitor.visitLabel(l1);
            if (!this.isMethodStatic) {
                this.visitor.visitLocalVariable("this", this.descriptor.getAccessorClass().toAsmMethodDescriptor(),
                        null, l0, l1, 0);
            }
            if (!this.isFieldStatic) {
                this.visitor.visitLocalVariable("instance", this.descriptor.getFieldClass().toAsmMethodDescriptor(),
                        null, l0, l1, this.isMethodStatic ? 0 : 1);
            }
            this.visitor.visitMaxs(1, this.isMethodStatic ? (this.isFieldStatic? 0 : 1) : (this.isFieldStatic? 1 : 2));
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

    private final Logger logger;
    private final Set<TargetDescriptor> descriptors;

    /**
     * Constructs a new instance of this transformer.
     *
     * @param data
     *      The data that identifies this transformer. Refer to
     *      {@link TransformerData} for more information. It cannot be null.
     * @param descriptors
     *      A list of {@link TargetDescriptor}s that identifies the couples of
     *      "target field" and "accessor method" that this transformer should
     *      target. There must be at least one target descriptor.
     *
     * @since 1.0.0
     */
    protected RuntimeFieldAccessTransformer(@Nonnull final TransformerData data, @Nonnull final TargetDescriptor... descriptors) {
        super(data, getClassesFromTargets(descriptors));
        this.descriptors = ImmutableSet.copyOf(new HashSet<>(Arrays.asList(descriptors)));
        this.logger = LogManager.getLogger("RuntimeFieldAccessTransformer/" + data.getOwningPluginId() + ":" + data.getName());
    }

    @Nonnull
    private static ClassDescriptor[] getClassesFromTargets(@Nonnull final TargetDescriptor... descriptors) {
        if (descriptors.length == 0) return new ClassDescriptor[0];
        // Each target stores two classes. They may be the same, but who cares?
        // The AbstractTransformer uses a set for this exact purpose
        final ClassDescriptor[] targetArray = new ClassDescriptor[descriptors.length * 2];
        /* mutable */ int j = 0;
        for (@Nonnull final TargetDescriptor descriptor : descriptors) {
            targetArray[j] = descriptor.getAccessorClass();
            ++j;
            targetArray[j] = descriptor.getFieldClass();
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

                for (@Nonnull final TargetDescriptor desc : RuntimeFieldAccessTransformer.this.descriptors) {
                    final ClassDescriptor fieldClass = desc.getFieldClass();
                    final ClassDescriptor accessorClass = desc.getAccessorClass();

                    if (descriptor.equals(fieldClass)) {
                        this.targetDescriptors.put(desc, true);
                        RuntimeFieldAccessTransformer.this.logger.info("Found class '" + name + "' matching target field '" + desc.getField().getName() + "'");
                    }
                    if (descriptor.equals(accessorClass)) {
                        this.targetDescriptors.put(desc, false);
                        RuntimeFieldAccessTransformer.this.logger.info("Found class '" + name + "' matching accessor '" + desc.getAccessor().toString() + "'");
                    }
                }
            }

            @Nullable
            @Override
            public FieldVisitor visitField(final int access, @Nonnull final String name, @Nonnull final String descriptor,
                                           @Nullable final String signature, @Nullable final Object value) {

                /*mutable*/ int newAccess = access;

                final Type fieldType = Type.getType(descriptor);
                final FieldDescriptor fieldDescriptor = FieldDescriptor.of(name, ClassDescriptor.of(fieldType));
                final FieldDescriptor staticFieldDescriptor = FieldDescriptor.of(name, ClassDescriptor.of(fieldType), true);

                /*mutable*/ Optional<FieldDescriptor> opt = this.targetDescriptors.entrySet().stream()
                        .filter(Map.Entry::getValue)
                        .map(Map.Entry::getKey)
                        .map(TargetDescriptor::getField)
                        .map(this::remapNameIfNeeded)
                        .filter(it -> Objects.equals(it, fieldDescriptor))
                        .findAny();

                if (!opt.isPresent()) {
                    opt = this.targetDescriptors.entrySet().stream()
                            .filter(Map.Entry::getValue)
                            .map(Map.Entry::getKey)
                            .map(TargetDescriptor::getField)
                            .map(this::remapNameIfNeeded)
                            .filter(it -> Objects.equals(it, staticFieldDescriptor))
                            .findAny();
                }

                if (opt.isPresent()) newAccess = ((access & ~Opcodes.ACC_PRIVATE) & ~Opcodes.ACC_PROTECTED) | Opcodes.ACC_PUBLIC;

                if (newAccess != access) {
                    RuntimeFieldAccessTransformer.this.logger.info("Made field '" + fieldDescriptor.toString() + "' public (new access: " + newAccess + ")");
                }

                return super.visitField(newAccess, name, descriptor, signature, value);
            }

            @Nullable
            @Override
            public MethodVisitor visitMethod(final int access, @Nonnull final String name, @Nonnull final String descriptor,
                                             @Nullable final String signature, @Nonnull final String[] exceptions) {
                final MethodVisitor parent = super.visitMethod(access, name, descriptor, signature, exceptions);

                final Type methodType = Type.getType(descriptor);
                final Type[] argumentTypes = methodType.getArgumentTypes();
                final Type returnType = methodType.getReturnType();

                final ClassDescriptor returnDesc = ClassDescriptor.of(returnType);
                final List<ClassDescriptor> arguments = Arrays.stream(argumentTypes)
                        .map(ClassDescriptor::of)
                        .collect(Collectors.toList());

                final MethodDescriptor methodDescriptor = MethodDescriptor.of(name, arguments, returnDesc);

                // Accessor methods won't be remapped because you're supposed to be able to access them since it is
                // your own mod code
                final Optional<TargetDescriptor> opt = this.targetDescriptors.entrySet().stream()
                        .filter(it -> !it.getValue())
                        .map(Map.Entry::getKey)
                        .filter(it -> Objects.equals(it.getAccessor(), methodDescriptor))
                        .findAny();

                if (opt.isPresent()) {
                    final TargetDescriptor found = opt.get();

                    RuntimeFieldAccessTransformer.this.logger.info("Found accessor method '" + methodDescriptor.toString() + "': overwriting with access code");

                    return new MethodCallerVisitor(v, parent, found, (access & Opcodes.ACC_STATIC) != 0);
                }

                return parent;
            }

            @Nonnull
            private FieldDescriptor remapNameIfNeeded(@Nonnull final FieldDescriptor in) {
                return FieldDescriptor.of(MappingUtilities.INSTANCE.mapField(in.getName()), in.getType(), in.isStatic());
            }
        };
    }
}
