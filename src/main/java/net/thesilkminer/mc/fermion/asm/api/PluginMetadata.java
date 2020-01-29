/*
 * Copyright (C) 2019  TheSilkMiner
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

/**
 * Holds all data that may be associated to a specific launch plugin.
 *
 * <p>This data is composed of the {@link LaunchPlugin} ID, a version, a name,
 * a logo, a URL, the credits, a list of {@link Author authors}, and a
 * description.</p>
 *
 * @since 1.0.0
 */
public final class PluginMetadata {

    /**
     * A builder used to create instances of {@link PluginMetadata}.
     *
     * <p>Builder instances can be reused, as in their {@link #build()} method
     * can be called multiple times to build multiple plugin metadata
     * holders.</p>
     *
     * @since 1.0.0
     */
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

        /**
         * Creates a new builder instance to construct an instance of
         * {@link PluginMetadata} with the specified {@code id}.
         *
         * <p>The various properties are not populated with default values.
         * Not all of them are needed to build this metadata, though. Only
         * some require explicit initialization before calling
         * {@link #build()}. These properties are the ID, the name, and the
         * version.</p>
         *
         * <p>The ID passed in must be completely lowercase, be composed
         * only of lowercase letters from {@code a} to {@code z}, digits
         * and dots ({@code .}), and its length must be between 1 and 64
         * characters long. The ID must be unique across all registered
         * launch plugin.</p>
         *
         * @param id
         *      The ID to check. It must not be null and it must respect the
         *      guide lines outlined.
         * @return
         *      A new, ready to be used, builder instance.
         * @throws IllegalArgumentException
         *      If the ID does not respect the correct guidelines.
         *
         * @since 1.0.0
         */
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

        /**
         * Sets the version of this launch plugin.
         *
         * <p>It is suggested for Launch Plugins to follow the Semantic
         * Versioning specifications when specifying versions. This is not
         * mandatory, though.</p>
         *
         * @param version
         *      The {@link ArtifactVersion} to set as a version. It cannot be
         *      null.
         * @return
         *      This builder, for chaining.
         * @throws IllegalStateException
         *      If the version was already set.
         *
         * @since 1.0.0
         */
        @Nonnull
        public Builder setVersion(@Nonnull final ArtifactVersion version) {
            if (!Objects.isNull(this.version)) {
                throw new IllegalStateException("Unable to set version more than once");
            }
            this.version = Preconditions.checkNotNull(version);
            return this;
        }

        /**
         * Sets the version of this launch plugin.
         *
         * <p>It is suggested for Launch Plugins to follow the Semantic
         * Versioning specifications when specifying versions. This is not
         * mandatory, though.</p>
         *
         * @param version
         *      A String representing the version of the launch plugin. It
         *      cannot be null.
         * @return
         *      This builder, for chaining.
         * @throws IllegalStateException
         *      If the version was already set.
         *
         * @since 1.0.0
         */
        @Nonnull
        public Builder setVersion(@Nonnull final String version) {
            return this.setVersion(new DefaultArtifactVersion(version));
        }

        /**
         * Sets the name of this launch plugin.
         *
         * @param name
         *      The name to set. It cannot be null.
         * @return
         *      This builder, for chaining.
         * @throws IllegalStateException
         *      If the name was already set.
         *
         * @since 1.0.0
         */
        @Nonnull
        public Builder setName(@Nonnull final String name) {
            if (!Objects.isNull(this.name)) {
                throw new IllegalStateException("Unable to set name more than once");
            }
            this.name = Preconditions.checkNotNull(name);
            return this;
        }

        /**
         * Sets the path that points to the logo to display.
         *
         * <p>The logo should be a PNG and it should be around 800x400 px
         * big.</p>
         *
         * @param logoPath
         *      The path to the logo to display. It cannot be null.
         * @return
         *      This builder for chaining.
         * @throws IllegalStateException
         *      If the logo path was already set.
         *
         * @since 1.0.0
         */
        @Nonnull
        public Builder setLogoPath(@Nonnull final String logoPath) {
            if (!Objects.isNull(this.logo)) {
                throw new IllegalStateException("Unable to set logo path more than once");
            }
            this.logo = Preconditions.checkNotNull(logoPath);
            return this;
        }

        /**
         * Sets the URL to display on this launch plugin.
         *
         * <p>The URL should be used to point to the website of your launch
         * plugin, or any other resource related to your launch plugin.</p>
         *
         * <p>The protocol of the URL must be either {@code http} or
         * {@code https}, otherwise users could not browse it.</p>
         *
         * @param url
         *      The URL to set. It cannot be null. It must respect the
         *      guidelines stated before.
         * @return
         *      This builder for chaining.
         * @throws IllegalStateException
         *      If the display URL was already set.
         *
         * @since 1.0.0
         */
        @Nonnull
        public Builder setDisplayUrl(@Nonnull final String url) {
            if (!Objects.isNull(this.url)) {
                throw new IllegalStateException("Unable to set display URL more than once");
            }
            this.url = Preconditions.checkNotNull(url);
            return this;
        }

        /**
         * Sets the credits part of this launch plugin.
         *
         * <p>The credits should be used to name people that made the launch
         * plugin possible, but that are not strictly authors.</p>
         *
         * @param credits
         *      A credits string to set. It must not be null.
         * @return
         *      This builder for chaining.
         * @throws IllegalStateException
         *      If the credits were already set.
         *
         * @since 1.0.0
         */
        @Nonnull
        public Builder setCredits(@Nonnull final String credits) {
            if (!Objects.isNull(this.credits)) {
                throw new IllegalStateException("Unable to set credits more than once");
            }
            this.credits = Preconditions.checkNotNull(credits);
            return this;
        }

        /**
         * Sets the description of this launch plugin.
         *
         * <p>The description should be brief, yet complete and tell users what
         * this launch plugin does and possibly why it's needed. It can be
         * empty, but it's not suggested.</p>
         *
         * @param description
         *      The description to set. It cannot be null.
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
                throw new IllegalStateException("Unable to set description more than once");
            }
            this.description = Preconditions.checkNotNull(description);
            return this;
        }

        /**
         * Adds the given {@link Author} to the authors list.
         *
         * <p>Each author can only be added once.</p>
         *
         * @param author
         *      The author to add. It cannot be null.
         * @return
         *      This builder for chaining.
         * @throws IllegalStateException
         *      If the given author was already added.
         *
         * @since 1.0.0
         */
        @Nonnull
        public Builder addAuthor(@Nonnull final Author author) {
            if (this.authors.contains(author)) {
                throw new IllegalStateException("Unable to add the same author more than once");
            }
            this.authors.add(Preconditions.checkNotNull(author));
            return this;
        }

        /**
         * Constructs an author with the given name and adds it to the authors
         * list.
         *
         * <p>Each author can only be added once.</p>
         *
         * @param author
         *      The author to add. It cannot be null.
         * @return
         *      This builder for chaining.
         * @throws IllegalStateException
         *      If the given author was already added.
         *
         * @since 1.0.0
         */
        @Nonnull
        public Builder addAuthor(@Nonnull final String author) {
            return this.addAuthor(Author.of(author));
        }

        /**
         * Builds a new {@link PluginMetadata} instance with the provided
         * information.
         *
         * @return
         *      A new metadata instance. Guaranteed not to be null.
         * @throws NullPointerException
         *      If either the name or the version hasn't been set.
         * @throws IllegalArgumentException
         *      If either the plugin ID does not respect the correct guidelines
         *      given in {@link #create(String)} or the URL does not respect
         *      the protocol restrictions given in
         *      {@link #setDisplayUrl(String)}.
         *
         * @since 1.0.0
         */
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

    /**
     * Identifies an author of the target {@link LaunchPlugin}.
     *
     * @since 1.0.0
     */
    public static final class Author {
        private final String name;

        private Author(@Nonnull final String name) {
            this.name = name;
        }

        /**
         * Creates a new author instance from the given name.
         *
         * @param name
         *      The name of the author. It cannot be null.
         * @return
         *      A new {@link Author} instance with the given name.
         *
         * @since 1.0.0
         */
        @Nonnull
        public static Author of (@Nonnull final String name) {
            return new Author(Preconditions.checkNotNull(name));
        }

        /**
         * Gets the name of this author.
         *
         * @return
         *      The name of this author. Guaranteed not to be null.
         *
         * @since 1.0.0
         */
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

    /**
     * Gets the ID of this plugin metadata.
     *
     * <p>The ID is guaranteed to be unique among the pool of registered
     * {@link LaunchPlugin}s.</p>
     *
     * @return
     *      The ID of this plugin metadata. Guaranteed not to be null.
     *
     * @since 1.0.0
     */
    @Nonnull
    public String getId() {
        return this.id;
    }

    /**
     * Gets the version of this plugin metadata as an {@link ArtifactVersion}.
     *
     * @return
     *      The version of this plugin metadata. Guaranteed not to be null.
     *
     * @since 1.0.0
     */
    @Nonnull
    public ArtifactVersion getVersion() {
        return this.version;
    }

    /**
     * Gets the name of this plugin metadata.
     *
     * @return
     *      The name of this plugin metadata.
     *
     * @since 1.0.0
     */
    @Nonnull
    public String getName() {
        return this.name;
    }

    /**
     * Gets the logo path of this plugin metadata.
     *
     * @return
     *      The logo path of this plugin metadata.
     *
     * @since 1.0.0
     */
    @Nullable
    public String getLogo() {
        return this.logo;
    }

    /**
     * Gets the display URL of this plugin metadata, if present.
     *
     * @return
     *      The display URL of this plugin metadata, if present. Null
     *      otherwise.
     *
     * @since 1.0.0
     */
    @Nullable
    public String getUrl() {
        return this.url;
    }

    /**
     * Gets the credits of this plugin metadata, if present.
     *
     * @return
     *      The credits of this plugin metadata, if present. Null otherwise.
     *
     * @since 1.0.0
     */
    @Nullable
    public String getCredits() {
        return this.credits;
    }

    /**
     * Gets the list of authors of this plugin metadata.
     *
     * @return
     *      The list of authors of this plugin metadata. Guaranteed not to be
     *      null. Possible to be empty.
     *
     * @since 1.0.0
     */
    @Nonnull
    public List<Author> getAuthors() {
        return this.authors;
    }

    /**
     * Gets the description of this plugin metadata, if present.
     *
     * @return
     *      The description of this plugin metadata, if present. Null
     *      otherwise.
     *
     * @since 1.0.0
     */
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
                ", description='" + (this.description == null? "<null>" : this.description.replace("\n", "\\n").replace("\r", "\\r") + '\'') +
                '}';
    }
}
