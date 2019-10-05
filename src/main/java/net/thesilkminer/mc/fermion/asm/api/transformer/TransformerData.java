package net.thesilkminer.mc.fermion.asm.api.transformer;

import com.google.common.base.Preconditions;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Holds all data that may be associated to a specific transformer.
 *
 * <p>This data is composed of the {@link LaunchPlugin} that owns the
 * transformer associated with thi data, its unique name, its description, and
 * whether it is disabled by default.</p>
 *
 * @since 1.0.0
 */
public final class TransformerData {

    /**
     * A builder used to create instances of {@link TransformerData}.
     *
     * <p>Builder instances can be reused, as in their {@link #build()} method
     * can be called multiple times to build multiple data holders.</p>
     *
     * @since 1.0.0
     */
    public static final class Builder {
        private String owningPluginId;
        private String name;
        private String description;
        private boolean defaultDisabled;

        private Builder() {}

        /**
         * Creates a new builder instance to construct an instance of
         * {@link TransformerData}.
         *
         * <p>The various properties are not populated with default values, but
         * one: the transformer is automatically deemed as enabled by default.
         * All the other properties require explicit initialization before
         * calling {@link #build()}.</p>
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
         * Sets the ID of the Launch Plugin that owns this transformer.
         *
         * <p>The plugin ID must match one of the loaded Launch Plugins. For
         * more security, it is possible to pass directly an instance of a
         * Launch Plugin to this method. Refer to
         * {@link #setOwningPlugin(LaunchPlugin)} for more information.</p>
         *
         * @param id
         *      The ID of the Launch Plugin that owns this transformer. It
         *      cannot be null.
         * @return
         *      This builder for chaining.
         * @throws IllegalStateException
         *      If the owning plugin ID was already set.
         *
         * @since 1.0.0
         */
        @Nonnull
        public Builder setOwningPluginId(@Nonnull final String id) {
            if (!Objects.isNull(this.owningPluginId)) {
                throw new IllegalStateException("Unable to set owning plugin ID multiple times");
            }
            this.owningPluginId = Preconditions.checkNotNull(id);
            return this;
        }

        /**
         * Sets the Launch Plugins that owns this transformer.
         *
         * <p>Since the only important part of the given {@link LaunchPlugin}
         * is its ID, it is possible to rely on
         * {@link #setOwningPluginId(String)} in case an instance of the launch
         * plugin isn't readily available.</p>
         *
         * @param plugin
         *      The {@link LaunchPlugin} that owns this transformer. It cannot
         *      be null.
         * @return
         *      This builder for chaining.
         * @throws IllegalStateException
         *      If the owning plugin was already set.
         *
         * @since 1.0.0
         */
        @Nonnull
        public Builder setOwningPlugin(@Nonnull final LaunchPlugin plugin) {
            return this.setOwningPluginId(Preconditions.checkNotNull(plugin).getMetadata().getId());
        }

        /**
         * Sets the name of this transformer.
         *
         * <p>The name must be unique inside every Launch Plugin. In other
         * words, it is illegal for two transformers owned by the same Launch
         * Plugin to have the same name.</p>
         *
         * <p>Note that the name must be legal. In other words, the name does
         * not have restrictions on the length, though it must be non-zero, and
         * no particular restrictions on character but one. It is in fact
         * illegal for the name to contain a colon ({@code :}).</p>
         *
         * @param name
         *      The name of the transformer. It cannot be null. It must be a
         *      valid name as described.
         * @return
         *      This builder for chaining.
         * @throws IllegalStateException
         *      If the name was already set.
         *
         * @since 1.0.0
         */
        @Nonnull
        public Builder setName(@Nonnull final String name) {
            if (!Objects.isNull(this.name)) {
                throw new IllegalStateException("Unable to set transformer name multiple times");
            }
            this.name = Preconditions.checkNotNull(name);
            return this;
        }

        /**
         * Sets a brief description that accompanies this transformer.
         *
         * <p>The description may be empty, but is otherwise suggested to
         * describe in a few words what this transformer transforms and why it
         * does that.</p>
         *
         * @param description
         *      The description of this transformer. It cannot be null.
         * @return
         *      This builder for chaining.
         * @throws IllegalStateException
         *      If the description was already set.
         *
         * @since 1.0.0
         */
        @Nonnull
        public Builder setDescription(@Nonnull final String description) {
            if (!Objects.isNull(this.description)) {
                throw new IllegalStateException("Unable to set description multiple times");
            }
            this.description = Preconditions.checkNotNull(description);
            return this;
        }

        /**
         * Sets this transformer as disabled by default.
         *
         * <p>Transformers that are disabled by default are not loaded unless
         * the users of the Launch Plugin enable them manually in the
         * configuration file. If they get enabled, they act normally like any
         * other transformer. For this reason, implementations may choose to
         * ignore this field when loading up an already known transformer.</p>
         *
         * <p>Note that a transformer that has been set as disabled by default
         * cannot be re-enabled unless a user specifically does that.</p>
         *
         * @return
         *      This builder for chaining.
         * @throws IllegalStateException
         *      If this transformer was already disabled by default.
         *
         * @since 1.0.0
         */
        @Nonnull
        public Builder setDisabledByDefault() {
            if (this.defaultDisabled) {
                throw new IllegalStateException("Unable to set disabled by default multiple times");
            }
            this.defaultDisabled = true;
            return this;
        }

        @Nonnull
        String getOwningPlugin() {
            return this.owningPluginId;
        }

        @Nonnull
        String getName() {
            return this.name;
        }

        @Nonnull
        String getDescription() {
            return this.description;
        }

        boolean getDefaultDisabled() {
            return this.defaultDisabled;
        }

        /**
         * Builds a new {@link TransformerData} instance with the provided
         * information.
         *
         * @return
         *      A new data instance. Guaranteed to be non-null.
         * @throws NullPointerException
         *      If one or more of the
         *      {@link TransformerData required properties} hasn't been set
         *      previously.
         * @throws IllegalArgumentException
         *      If the name given does not respect the requirements outlined
         *      {@link #setName(String) here}.
         *
         * @since 1.0.0
         */
        @Nonnull
        public TransformerData build() {
            Preconditions.checkNotNull(this.owningPluginId, "Owning plugin cannot be null");
            Preconditions.checkNotNull(this.name, "Name cannot be null");
            Preconditions.checkNotNull(this.description, "Description cannot be null");
            Preconditions.checkArgument(this.testName(), "The name cannot contain columns");
            return new TransformerData(this);
        }

        private boolean testName() {
            return this.name.indexOf(':') == -1;
        }
    }

    private final String owningPluginId;
    private final String name;
    private final String description;
    private final boolean enabled;

    private TransformerData(@Nonnull final TransformerData.Builder builder) {
        this.owningPluginId = Preconditions.checkNotNull(builder.getOwningPlugin());
        this.name = Preconditions.checkNotNull(builder.getName());
        this.description = Preconditions.checkNotNull(builder.getDescription());
        this.enabled = !builder.getDefaultDisabled();
    }

    /**
     * Gets the ID of the {@link LaunchPlugin} that owns this transformer.
     *
     * <p>The given ID is guaranteed to be of a launch plugin that has been
     * previously been loaded and registered.</p>
     *
     * @return
     *      The ID of the {@link LaunchPlugin} that owns this transformer.
     *      Guaranteed not to be null.
     *
     * @since 1.0.0
     */
    @Nonnull
    public String getOwningPluginId() {
        return this.owningPluginId;
    }

    /**
     * Gets the name of this transformer.
     *
     * @return
     *      The name of this transformer. Guaranteed not to be null.
     *
     * @since 1.0.0
     */
    @Nonnull
    public String getName() {
        return this.name;
    }

    /**
     * Gets the description associated to this transformer.
     *
     * @return
     *      The description associated to this transformer. Guaranteed not to
     *      be null.
     *
     * @since 1.0.0
     */
    @Nonnull
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets whether this transformer is enabled by default.
     *
     * <p>Note that this does not mean that the transformer will remain in the
     * state identified by the return value of this method. It may be enabled
     * or disabled subsequently through configuration.</p>
     *
     * @return
     *      Whether this transformer is enabled by default.
     *
     * @since 1.0.0
     */
    public boolean isEnabledByDefault() {
        return this.enabled;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final TransformerData that = (TransformerData) o;
        return Objects.equals(this.owningPluginId, that.owningPluginId) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.enabled, that.enabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.owningPluginId, this.name, this.description, this.enabled);
    }

    @Override
    public String toString() {
        return "TransformerData{" +
                "owningPluginId='" + this.owningPluginId + '\'' +
                ", name='" + this.name + '\'' +
                ", description='" + this.description + '\'' +
                ", enabled=" + this.enabled +
                '}';
    }
}
