package net.thesilkminer.mc.fermion.asm.api.configuration;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class TransformerConfiguration {

    public static final class Builder {
        private Supplier<JsonObject> serializer;
        private Consumer<JsonObject> deserializer;
        private Function<JsonObject, JsonObject> defaultProvider;

        private Builder() {
            this.serializer = () -> null;
            this.deserializer = it -> {};
            this.defaultProvider = it -> it;
        }

        @Nonnull
        public static Builder create() {
            return new Builder();
        }

        @Nonnull
        public Builder setSerializer(@Nonnull final Supplier<JsonObject> serializer) {
            this.serializer = Preconditions.checkNotNull(serializer);
            return this;
        }

        @Nonnull
        public Builder setDeserializer(@Nonnull final Consumer<JsonObject> deserializer) {
            this.deserializer = Preconditions.checkNotNull(deserializer);
            return this;
        }

        @Nonnull
        public Builder setConfigDefaultsProvider(@Nonnull final Function<JsonObject, JsonObject> defaultProvider) {
            this.defaultProvider = Preconditions.checkNotNull(defaultProvider);
            return this;
        }

        @Nonnull
        public TransformerConfiguration build() {
            return new TransformerConfiguration(this);
        }

        @Nonnull
        Supplier<JsonObject> getSerializer() {
            return this.serializer;
        }

        @Nonnull
        Consumer<JsonObject> getDeserializer() {
            return this.deserializer;
        }

        @Nonnull
        Function<JsonObject, JsonObject> getDefaultProvider() {
            return this.defaultProvider;
        }
    }

    private final Supplier<JsonObject> serializer;
    private final Consumer<JsonObject> deserializer;
    private final Function<JsonObject, JsonObject> defaultProvider;

    private TransformerConfiguration(@Nonnull final TransformerConfiguration.Builder builder) {
        this.serializer = builder.getSerializer();
        this.deserializer = builder.getDeserializer();
        this.defaultProvider = builder.getDefaultProvider();
    }

    @Nonnull
    public Supplier<JsonObject> getSerializer() {
        return this.serializer;
    }

    @Nonnull
    public Consumer<JsonObject> getDeserializer() {
        return this.deserializer;
    }

    @Nonnull
    public Function<JsonObject, JsonObject> getDefaultProvider() {
        return this.defaultProvider;
    }
}
