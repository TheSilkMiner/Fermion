package net.thesilkminer.mc.fermion.asm.api.transformer;

import com.google.common.base.Preconditions;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public final class TransformerData {

    public static final class Builder {
        private String owningPluginId;
        private String name;
        private String description;

        private Builder() {}

        @Nonnull
        public static Builder create() {
            return new Builder();
        }

        @Nonnull
        public Builder setOwningPluginId(@Nonnull final String id) {
            if (!Objects.isNull(this.owningPluginId)) {
                throw new IllegalStateException("Unable to set owning plugin ID multiple times");
            }
            this.owningPluginId = Preconditions.checkNotNull(id);
            return this;
        }

        @Nonnull
        public Builder setOwningPlugin(@Nonnull final LaunchPlugin plugin) {
            return this.setOwningPluginId(Preconditions.checkNotNull(plugin).getMetadata().getId());
        }

        @Nonnull
        public Builder setName(@Nonnull final String name) {
            if (!Objects.isNull(this.name)) {
                throw new IllegalStateException("Unable to set transformer name multiple times");
            }
            this.name = Preconditions.checkNotNull(name);
            return this;
        }

        @Nonnull
        public Builder setDescription(@Nonnull final String description) {
            if (!Objects.isNull(this.description)) {
                throw new IllegalStateException("Unable to set description multiple times");
            }
            this.description = Preconditions.checkNotNull(description);
            return this;
        }

        @Nullable
        String getOwningPlugin() {
            return this.owningPluginId;
        }

        @Nullable
        String getName() {
            return this.name;
        }

        @Nullable
        String getDescription() {
            return this.description;
        }

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

    private TransformerData(@Nonnull final TransformerData.Builder builder) {
        this.owningPluginId = builder.getOwningPlugin();
        this.name = builder.getName();
        this.description = builder.getDescription();
    }

    @Nullable
    public String getOwningPluginId() {
        return this.owningPluginId;
    }

    @Nullable
    public String getName() {
        return this.name;
    }

    @Nullable
    public String getDescription() {
        return this.description;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final TransformerData that = (TransformerData) o;
        return Objects.equals(this.owningPluginId, that.owningPluginId) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.owningPluginId, this.name, this.description);
    }

    @Override
    public String toString() {
        return "TransformerData{" +
                "owningPluginId='" + this.owningPluginId + '\'' +
                ", name='" + this.name + '\'' +
                ", description='" + this.description + '\'' +
                '}';
    }
}
