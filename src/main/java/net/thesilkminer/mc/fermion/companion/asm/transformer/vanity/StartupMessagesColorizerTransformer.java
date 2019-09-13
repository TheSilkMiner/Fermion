package net.thesilkminer.mc.fermion.companion.asm.transformer.vanity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.thesilkminer.mc.fermion.asm.api.configuration.TransformerConfiguration;
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
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class StartupMessagesColorizerTransformer extends SingleTargetMethodTransformer {

    private static final Logger LOGGER = LogManager.getLogger("fermion.asm");
    private static final Marker MARKER = MarkerManager.getMarker("Color Startup Messages");

    private final Map<String, float[]> targetColors = Maps.newHashMap();

    public StartupMessagesColorizerTransformer() {
        super(
                TransformerData.Builder.create()
                        .setOwningPluginId("fermion.asm")
                        .setName("vanity_color_startup_messages")
                        .setDescription("This transformers modifies the colors of the startup messages as you choose. " +
                                "Refer to the configuration")
                        .build(),
                ClassDescriptor.of("net.minecraftforge.fml.StartupMessageManager$MessageType"),
                MethodDescriptor.of("<clinit>", ImmutableList.of(), ClassDescriptor.of(void.class))
        );
    }

    @Nonnull
    @Override
    @SuppressWarnings("SpellCheckingInspection")
    protected BiFunction<Integer, MethodVisitor, MethodVisitor> getMethodVisitorCreator() {
        return (v, mv) -> new MethodVisitor(v, mv) {
            private boolean isHijackingInsnVisits;
            private int latestIntConstOpcode;

            @Override
            public void visitCode() {
                this.isHijackingInsnVisits = false;
                this.latestIntConstOpcode = -1;
                super.visitCode();
            }

            @Override
            public void visitLdcInsn(@Nonnull final Object value) {
                if (this.isHijackingInsnVisits) return;

                super.visitLdcInsn(value);

                if (value instanceof String) {
                    final String string = (String) value;
                    final float[] colors = StartupMessagesColorizerTransformer.this.targetColors.get(string);
                    if (colors == null) {
                        LOGGER.warn(MARKER, "Unable to find colors for " + string + ". We will leave it alone, but it is a serious error!");
                        return;
                    }

                    // It's show-time
                    this.isHijackingInsnVisits = true;

                    final Integer matchingIntConstOpcode = this.getMatchingIntConstOpcode();
                    if (matchingIntConstOpcode == null) {
                        // We are past the ICONST_x range
                        // We need to create our own LDC
                        super.visitLdcInsn(new Integer(Integer.toString(this.latestIntConstOpcode)));
                    } else {
                        // We have an opcode for that, of the type ICONST_x
                        // Let's use that
                        super.visitInsn(matchingIntConstOpcode);
                    }

                    for (final float color : colors) {
                        final Integer floatOpcode = this.getFloatOpcode(color);

                        if (floatOpcode != null) {
                            super.visitInsn(floatOpcode);
                        } else {
                            super.visitLdcInsn(new Float(Float.toString(color)));
                        }
                    }
                }
            }

            @Override
            public void visitInsn(final int opcode) {
                if (this.isHijackingInsnVisits) return;
                super.visitInsn(opcode);
            }

            @Override
            public void visitMethodInsn(final int opcode, @Nonnull final String owner, @Nonnull final String name,
                                        @Nonnull final String descriptor, final boolean isInterface) {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

                if (opcode == Opcodes.INVOKESPECIAL && "net/minecraftforge/fml/StartupMessageManager$MessageType".equals(owner)
                        && "<init>".equals(name) && this.isHijackingInsnVisits) {
                    this.isHijackingInsnVisits = false;
                }
            }

            @Nullable
            private Integer getMatchingIntConstOpcode() {
                ++this.latestIntConstOpcode;
                switch (this.latestIntConstOpcode) {
                    case 0: return Opcodes.ICONST_0;
                    case 1: return Opcodes.ICONST_1;
                    case 2: return Opcodes.ICONST_2;
                    case 3: return Opcodes.ICONST_3;
                    case 4: return Opcodes.ICONST_4;
                    case 5: return Opcodes.ICONST_5;
                    default: return null;
                }
            }

            @Nullable
            private Integer getFloatOpcode(final float target) {
                if (target == 0.0F) return Opcodes.FCONST_0;
                if (target == 1.0F) return Opcodes.FCONST_1;
                if (target == (1.0F + 1.0F)) return Opcodes.FCONST_2;
                return null;
            }
        };
    }

    @Nonnull
    @Override
    public Supplier<TransformerConfiguration> provideConfiguration() {
        return () -> TransformerConfiguration.Builder.create()
                .setSerializer(() -> {
                    final JsonObject configObject = new JsonObject();
                    configObject.add("__comment", new JsonPrimitive(this.getComment("_")));
                    configObject.add("minecraft", this.createConfigJsonObject("MC"));
                    configObject.add("fml", this.createConfigJsonObject("ML"));
                    configObject.add("mods", this.createConfigJsonObject("MOD"));
                    return configObject;
                })
                .setConfigDefaultsProvider(it -> {
                    it.add("__comment", new JsonPrimitive(this.getComment("_")));
                    this.checkJsonObject(it, "MC", it.has("minecraft"), () -> it.get("minecraft"));
                    this.checkJsonObject(it, "ML", it.has("fml"), () -> it.get("fml"));
                    this.checkJsonObject(it, "MOD", it.has("mods"), () -> it.get("mods"));
                    return it;
                })
                .setDeserializer(it -> {
                    this.deserializeObject("MC", it.get("minecraft").getAsJsonObject());
                    this.deserializeObject("ML", it.get("fml").getAsJsonObject());
                    this.deserializeObject("MOD", it.get("mods").getAsJsonObject());
                    LOGGER.info(MARKER, "Read configuration successfully");
                    LOGGER.debug(MARKER, "Currently loaded properties: ");
                    this.targetColors.forEach((k, v) -> LOGGER.debug(MARKER, "    " + k + " --> " + Arrays.toString(v)));
                })
                .build();
    }

    @Nonnull
    private JsonObject createConfigJsonObject(@Nonnull final String of) {
        final float[] colors = this.getColorsFor(of);
        final JsonObject toReturn = new JsonObject();
        toReturn.add("__comment", new JsonPrimitive(this.getComment(of)));
        toReturn.add("red", new JsonPrimitive(colors[0]));
        toReturn.add("green", new JsonPrimitive(colors[1]));
        toReturn.add("blue", new JsonPrimitive(colors[2]));
        return toReturn;
    }

    @Nonnull
    @SuppressWarnings("MagicNumber")
    private float[] getColorsFor(@Nonnull final String of) {
        switch (of) {
            case "MC": return new float[] { 0, 0.5F, 0 }; // Not 0 0 0 because I don't like it
            case "ML": return new float[] { 0, 0, 0.5F };
            case "MOD": return new float[] { 0.5F, 0, 0 };
            default: throw new IllegalStateException("New enum values?");
        }
    }

    @Nonnull
    private String getComment(@Nonnull final String of) {
        switch (of) {
            case "MC": return "The color that MC should use to print messages. Default: 0, 0.5, 0";
            case "ML": return "The color that Forge and FML should use for their messages on the loading screen. Default: 0, 0, 0.5F";
            case "MOD": return "The color that mods should use for their messages on the loading screen. Default: 0.5F, 0, 0";
            case "_": return "The colors are specified in floating point using RGB: 0 = not present, 1 = maximum intensity";
            default: return "This is weird and won't be considered";
        }
    }

    private void checkJsonObject(@Nonnull final JsonObject main, @Nonnull final String of, final boolean has,
                                 @Nonnull final Supplier<JsonElement> elementSupplier) {

        final JsonElement target = has? elementSupplier.get() : new JsonObject();
        final JsonObject toCheck = target.isJsonObject()? target.getAsJsonObject() : new JsonObject();
        final String property = this.getPropertyFromOf(of);
        final JsonObject checked = this.checkJsonObject(of, has, toCheck);
        main.add(property, checked);
    }

    @Nonnull
    private JsonObject checkJsonObject(@Nonnull final String of, final boolean has, @Nonnull final JsonObject object) {
        final JsonObject defaultJsonObject = this.createConfigJsonObject(of);
        if (!has) {
            return defaultJsonObject;
        }
        object.add("__comment", new JsonPrimitive(this.getComment(of)));
        if (!this.hasValidFloatProperty(object, "red")) object.add("red", defaultJsonObject.get("red"));
        if (!this.hasValidFloatProperty(object, "green")) object.add("green", defaultJsonObject.get("green"));
        if (!this.hasValidFloatProperty(object, "blue")) object.add("blue", defaultJsonObject.get("blue"));
        return object;
    }

    @Nonnull
    private String getPropertyFromOf(@Nonnull final String of) {
        switch (of) {
            case "MC": return "minecraft";
            case "ML": return "fml";
            case "MOD": return "mods";
            default: throw new IllegalArgumentException(of);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean hasValidFloatProperty(@Nonnull final JsonObject obj, @Nonnull final String name) {
        if (!this.hasFloatProperty(obj, name)) return false;
        try {
            final float value = obj.get(name).getAsJsonPrimitive().getAsFloat();
            if (value < 0.0F || value > 1.0F) return false;
        } catch (@Nonnull final NumberFormatException e) {
            return false;
        }
        return true;
    }

    private boolean hasFloatProperty(@Nonnull final JsonObject obj, @Nonnull final String name) {
        return obj.has(name) && obj.get(name).isJsonPrimitive() && obj.get(name).getAsJsonPrimitive().isNumber();
    }

    private void deserializeObject(@Nonnull final String of, @Nonnull final JsonObject object) {
        final float[] colors = new float[3];
        colors[0] = object.get("red").getAsJsonPrimitive().getAsFloat();
        colors[1] = object.get("green").getAsJsonPrimitive().getAsFloat();
        colors[2] = object.get("blue").getAsJsonPrimitive().getAsFloat();
        this.targetColors.put(of, colors);
    }
}
