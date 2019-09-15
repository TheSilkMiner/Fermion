package net.thesilkminer.mc.fermion.asm.prefab.transformer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.thesilkminer.mc.fermion.asm.api.descriptor.ClassDescriptor;
import net.thesilkminer.mc.fermion.asm.api.descriptor.MethodDescriptor;
import net.thesilkminer.mc.fermion.asm.api.transformer.TransformerData;
import net.thesilkminer.mc.fermion.asm.prefab.AbstractTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.objectweb.asm.*;

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

public abstract class RuntimeMethodAccessTransformer extends AbstractTransformer {

    protected static final class TargetDescriptor {

        public static final class Builder {

            private ClassDescriptor methodClass;
            private MethodDescriptor method;
            private boolean isTargetMethodStatic;
            private ClassDescriptor accessorClass;
            private MethodDescriptor accessor;

            private Builder() {
            }

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

            @Nonnull
            public Builder setTargetMethod(@Nonnull final ClassDescriptor methodClass, @Nonnull final MethodDescriptor method) {
                return this.setTargetMethod(methodClass, method, false);
            }

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

    public RuntimeMethodAccessTransformer(@Nonnull final TransformerData data, @Nonnull final TargetDescriptor... descriptors) {
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
                        .filter(it -> Objects.equals(it, descriptor))
                        .findAny();

                return opt.isPresent()? ((access & ~Opcodes.ACC_PRIVATE) & ~Opcodes.ACC_PROTECTED) | Opcodes.ACC_PUBLIC : access;
            }

            @Nonnull
            private BiFunction<Integer, MethodVisitor, MethodVisitor> getVisitorForMethod(final int access, final MethodDescriptor descriptor) {
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
        };
    }
}
