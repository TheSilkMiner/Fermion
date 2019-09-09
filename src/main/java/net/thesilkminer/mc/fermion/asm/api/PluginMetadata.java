package net.thesilkminer.mc.fermion.asm.api;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class PluginMetadata {

    public static final class Builder {

        private final String id;
        private ArtifactVersion version;
        private String name;
        private String logo;
        private String url;
        private String credits;
        private final List<Author> authors;
        private String description;

        private Builder(@Nonnull final String id) {
            this.id = id;
            this.authors = Lists.newArrayList();
        }

        @Nonnull
        public static Builder create(@Nonnull final String id) {
            return new Builder(checkId(Preconditions.checkNotNull(id)));
        }

        private static String checkId(@Nonnull final String id) {
            if (!id.toLowerCase(Locale.ENGLISH).equals(id)) {
                throw new IllegalArgumentException("Mod ID must be all lowercase");
            }
            return id;
        }

        @Nonnull
        String getId() {
            return this.id;
        }

        @Nonnull
        ArtifactVersion getVersion() {
            return this.version;
        }

        @Nonnull
        String getName() {
            return this.name;
        }

        @Nullable
        String getLogoPath() {
            return this.logo;
        }

        @Nullable
        String getDisplayUrl() {
            return this.url;
        }

        @Nullable
        String getCredits() {
            return this.credits;
        }

        @Nonnull
        List<Author> getAuthors() {
            return ImmutableList.copyOf(this.authors);
        }

        @Nullable
        String getDescription() {
            return this.description;
        }

        @Nonnull
        public Builder setVersion(@Nonnull final ArtifactVersion version) {
            if (!Objects.isNull(this.version)) {
                throw new IllegalStateException("Unable to set version more than once");
            }
            this.version = Preconditions.checkNotNull(version);
            return this;
        }

        @Nonnull
        public Builder setVersion(@Nonnull final String version) {
            return this.setVersion(new DefaultArtifactVersion(version));
        }

        @Nonnull
        public Builder setName(@Nonnull final String name) {
            if (!Objects.isNull(this.name)) {
                throw new IllegalStateException("Unable to set name more than once");
            }
            this.name = Preconditions.checkNotNull(name);
            return this;
        }

        @Nonnull
        public Builder setLogoPath(@Nonnull final String logoPath) {
            if (!Objects.isNull(this.logo)) {
                throw new IllegalStateException("Unable to set logo path more than once");
            }
            this.logo = Preconditions.checkNotNull(logoPath);
            return this;
        }

        @Nonnull
        public Builder setDisplayUrl(@Nonnull final String url) {
            if (!Objects.isNull(this.url)) {
                throw new IllegalStateException("Unable to set display URL more than once");
            }
            this.url = Preconditions.checkNotNull(url);
            return this;
        }

        @Nonnull
        public Builder setCredits(@Nonnull final String credits) {
            if (!Objects.isNull(this.credits)) {
                throw new IllegalStateException("Unable to set credits more than once");
            }
            this.credits = Preconditions.checkNotNull(credits);
            return this;
        }

        @Nonnull
        public Builder setDescription(@Nonnull final String description) {
            if (!Objects.isNull(this.description)) {
                throw new IllegalStateException("Unable to set description more than once");
            }
            this.description = Preconditions.checkNotNull(description);
            return this;
        }

        @Nonnull
        public Builder addAuthor(@Nonnull final Author author) {
            if (this.authors.contains(author)) {
                throw new IllegalStateException("Unable to add the same author more than once");
            }
            this.authors.add(Preconditions.checkNotNull(author));
            return this;
        }

        @Nonnull
        public Builder addAuthor(@Nonnull final String author) {
            return this.addAuthor(Author.of(author));
        }

        @Nonnull
        public PluginMetadata build() {
            Preconditions.checkNotNull(this.name, "You must specify a name for your plugin");
            Preconditions.checkNotNull(this.version, "You must specify a version for your plugin");
            Preconditions.checkArgument(this.testId(), "The plugin id cannot contain colons");
            Preconditions.checkArgument(this.testUrl(), "The given URL is not valid");
            return new PluginMetadata(this);
        }

        private boolean testId() {
            return this.id.matches("[a-z0-9.]{1,64}");
        }

        private boolean testUrl() {
            if (Objects.isNull(this.url)) return true;

            try {
                final URL url = new URL(this.url);
                return "http".equalsIgnoreCase(url.getProtocol()) || "https".equalsIgnoreCase(url.getProtocol());
            } catch (final MalformedURLException e) {
                return false;
            }
        }
    }

    public static final class Author {
        private final String name;

        private Author(@Nonnull final String name) {
            this.name = name;
        }

        @Nonnull
        public static Author of (@Nonnull final String name) {
            return new Author(Preconditions.checkNotNull(name));
        }

        @Nonnull
        public String getName() {
            return this.name;
        }

        @Override
        public boolean equals(@Nullable final Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            final Author author = (Author) o;
            return Objects.equals(this.name, author.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.name);
        }

        @Override
        public String toString() {
            return "Author{" +
                    "name='" + this.name + '\'' +
                    '}';
        }
    }

    private final String id;
    private final ArtifactVersion version;
    private final String name;
    private final String logo;
    private final String url;
    private final String credits;
    private final List<Author> authors;
    private final String description;

    private PluginMetadata(@Nonnull final PluginMetadata.Builder builder) {
        this.id = Preconditions.checkNotNull(builder.getId());
        this.version = Preconditions.checkNotNull(builder.getVersion());
        this.name = Preconditions.checkNotNull(builder.getName());
        this.logo = builder.getLogoPath();
        this.url = builder.getDisplayUrl();
        this.credits = builder.getCredits();
        this.authors = builder.getAuthors();
        this.description = builder.getDescription();
    }

    @Nonnull
    public String getId() {
        return this.id;
    }

    @Nonnull
    public ArtifactVersion getVersion() {
        return this.version;
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    @Nullable
    public String getLogo() {
        return this.logo;
    }

    @Nullable
    public String getUrl() {
        return this.url;
    }

    @Nullable
    public String getCredits() {
        return this.credits;
    }

    @Nonnull
    public List<Author> getAuthors() {
        return this.authors;
    }

    @Nullable
    public String getDescription() {
        return this.description;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final PluginMetadata that = (PluginMetadata) o;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.version, that.version) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.logo, that.logo) &&
                Objects.equals(this.url, that.url) &&
                Objects.equals(this.credits, that.credits) &&
                Objects.equals(this.authors, that.authors) &&
                Objects.equals(this.description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.version, this.name, this.logo, this.url, this.credits, this.authors, this.description);
    }

    @Override
    public String toString() {
        return "PluginMetadata{" +
                "id='" + this.id + '\'' +
                ", version=" + this.version +
                ", name='" + this.name + '\'' +
                ", logo='" + this.logo + '\'' +
                ", url='" + this.url + '\'' +
                ", credits='" + this.credits + '\'' +
                ", authors=" + this.authors +
                ", description='" + this.description + '\'' +
                '}';
    }
}
