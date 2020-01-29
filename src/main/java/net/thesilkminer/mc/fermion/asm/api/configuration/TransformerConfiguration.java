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

package net.thesilkminer.mc.fermion.asm.api.configuration;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents an instance for serializing and de-serializing a configuration
 * JSON object to and from file for a specific transformer.
 *
 * @since 1.0.0
 */
public final class TransformerConfiguration {

    /**
     * A builder used to create instances of {@link TransformerConfiguration}.
     *
     * <p>Builder instances can be reused, as in their {@link #build()} method
     * can be called multiple times to build multiple configurations.</p>
     *
     * @since 1.0.0
     */
    public static final class Builder {
        private Supplier<JsonObject> serializer;
        private Consumer<JsonObject> deserializer;
        private Function<JsonObject, JsonObject> defaultProvider;

        private Builder() {
            this.serializer = () -> null;
            this.deserializer = it -> {};
            this.defaultProvider = it -> it;
        }

        /**
         * Creates a new builder instance to construct a
         * {@link TransformerConfiguration}.
         *
         * <p>The various properties are populated with default values.
         * There are none that require explicit initialization prior to
         * creating the configuration instance through {@link #build()}.</p>
         *
         * <p>By default, a newly created builder has a null-returning
         * {@link #setSerializer(Supplier) serializer}, a no-op
         * {@link #setDeserializer(Consumer) deserializer}, and an identity
         * returning {@link #setConfigDefaultsProvider(Function) defaults
         * provider}.</p>
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

        /**
         * Sets the serializer supplier.
         *
         * <p>Constraints about the serializer, its implementation, and details
         * that may be required are given in
         * {@link TransformerConfiguration#getSerializer()}.</p>
         *
         * @param serializer
         *      The serializer supplier to set. It cannot be null. Its return
         *      value may be null.
         * @return
         *      This builder for chaining.
         *
         * @since 1.0.0
         */
        @Nonnull
        public Builder setSerializer(@Nonnull final Supplier<JsonObject> serializer) {
            this.serializer = Preconditions.checkNotNull(serializer);
            return this;
        }

        /**
         * Sets the deserializer consumer.
         *
         * <p>Constraints about the deserializer, its implementation and
         * details that may be required are given in
         * {@link TransformerConfiguration#getDeserializer()}.</p>
         *
         * @param deserializer
         *      The deserializer to set. It cannot be null. The value passed
         *      to it is guaranteed to be not-null.
         * @return
         *      This builder for chaining.
         *
         * @since 1.0.0
         */
        @Nonnull
        public Builder setDeserializer(@Nonnull final Consumer<JsonObject> deserializer) {
            this.deserializer = Preconditions.checkNotNull(deserializer);
            return this;
        }

        /**
         * Sets the provider function for default configuration settings.
         *
         * <p>Constraints about the deserializer, its implementation and
         * details that may be required are given in
         * {@link TransformerConfiguration#getDefaultProvider()}.</p>
         *
         * @param defaultProvider
         *      The provider to set. It cannot be null. It cannot return a
         *      null value. The value passed in is guaranteed to be non-null.
         * @return
         *      This builder for chaining.
         *
         * @since 1.0.0
         */
        @Nonnull
        public Builder setConfigDefaultsProvider(@Nonnull final Function<JsonObject, JsonObject> defaultProvider) {
            this.defaultProvider = Preconditions.checkNotNull(defaultProvider);
            return this;
        }

        /**
         * Builds a new {@link TransformerConfiguration} instance with the
         * information provided in this builder.
         *
         * @return
         *      A new configuration instance. Guaranteed to be not-null.
         *
         * @since 1.0.0
         */
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

    /**
     * Gets the serializer associated to this configuration.
     *
     * <p>The serializer is only called when initializing a transformer for
     * the first time and only if no matching configuration object for it
     * was found in the launch plugin transformers configuration file.</p>
     *
     * <p>The serializer has to provide a {@link JsonObject} with all the
     * properties created and initialized to their default value. The
     * serializer can also provide a null return value, which means that
     * the transformer doesn't require a configuration.</p>
     *
     * <p>Note that an empty {@code JsonObject} rather than a null value may
     * be treated differently by the implementation. Optimizing implementations
     * may in fact consider a null value returned by this supplier as a signal
     * of skipping configuration loading completely, thus "shutting down" the
     * configuration providing pipe.</p>
     *
     * @return
     *      A {@link Supplier} providing a {@link JsonObject} as described or
     *      a null value. It is guaranteed not to be null.
     *
     * @since 1.0.0
     */
    @Nonnull
    public Supplier<JsonObject> getSerializer() {
        return this.serializer;
    }

    /**
     * Gets the deserializer associated to this configuration.
     *
     * <p>The deserializer will be called with a {@link JsonObject} containing
     * all the values specified in the serializer and in the default provider,
     * either initialized to their default values or to the ones configured
     * by the user.</p>
     *
     * <p>It is correct for this deserializer to assume that all the JSON
     * properties are present and reference {@link com.google.gson.JsonElement}
     * of the correct type (e.g. a property that has a string as a value can be
     * assumed as having a string as a value).</p>
     *
     * <p>The JsonObject passed to the deserializer is guaranteed to be
     * not-null.</p>
     *
     * @return
     *      A {@link Consumer} that consumes a non-null {@link JsonObject} that
     *      loads the correct configuration in the corresponding transformer.
     *      It is guaranteed not to be null.
     *
     * @since 1.0.0
     */
    @Nonnull
    public Consumer<JsonObject> getDeserializer() {
        return this.deserializer;
    }

    /**
     * Gets the provider of all the default values that may be associated with
     * already present or not-yet present keys of this configuration.
     *
     * <p>This value will be called with a {@link JsonObject} that may or may
     * not contain all the keys that the configuration expects. It is
     * guaranteed to be not-null. The default provider must attempt to load
     * values and if they cannot be found or do not match the requested type,
     * it should replace them with the default values, <strong>without</strong>
     * attempting to carry over user settings.</p>
     *
     * <p>This provider must also ensure that immutable properties (e.g.
     * comments) are not mutated and reset them in case they aren't.</p>
     *
     * <p>It is not correct to assume that the given {@code JsonObject} is not
     * an empty, just initialized object. It is also not correct to assume that
     * it corresponds to the value returned by the {@link #getSerializer()
     * serializer}.</p>
     *
     * <p>This provider is allowed to mutate state on the passed in object, but
     * this states will be ignored by the implementation. For this reason,
     * either a new {@code JsonObject} or the given mutated one should be
     * returned.</p>
     *
     * <p>The returned object must not be null. If a default provider does not
     * want to do any sort of work (a sort of no-op implementation), it should
     * resort to an identity function. In other words, assuming the parameter
     * is called {@code configObject}, the entire provider should be
     * {@code return configObject;}.</p>
     *
     * @return
     *      A {@link Function} that gets a non-null {@link JsonObject} and
     *      modifies it according to the specifications and returns either
     *      the same {@code JsonObject} or a new instance with the
     *      modifications, which must be non-null. It is guaranteed not to be
     *      null.
     *
     * @since 1.0.0
     */
    @Nonnull
    public Function<JsonObject, JsonObject> getDefaultProvider() {
        return this.defaultProvider;
    }
}
