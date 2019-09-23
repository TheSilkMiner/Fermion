package net.thesilkminer.mc.fermion.asm.common.utility;

import com.google.common.collect.Lists;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class LaunchPluginDiscoverer {

    private static final class WrappedInputOutputException extends RuntimeException {
        private WrappedInputOutputException(@Nonnull final IOException cause) {
            super(cause);
        }
    }

    @FunctionalInterface
    private interface ExceptionalFunction<T, R, E extends IOException> {
        @Nullable R apply(@Nullable final T t) throws E;
    }

    private static final Log LOGGER = Log.of("LaunchPluginDiscoverer");

    private final List<Path> transformersPaths = Lists.newArrayList();

    private LaunchPluginDiscoverer() {}

    @Nonnull
    public static LaunchPluginDiscoverer create() {
        return new LaunchPluginDiscoverer();
    }

    @Nonnull
    public Iterable<LaunchPlugin> discover() {
        LOGGER.i("Discovering Launch Plugins");
        final List<LaunchPlugin> launchPlugins = Lists.newArrayList();
        final ServiceLoader<LaunchPlugin> classPath = ServiceLoader.load(LaunchPlugin.class);
        for (@Nonnull final LaunchPlugin plugin : classPath) launchPlugins.add(plugin);
        launchPlugins.addAll(this.discoverFromPaths());
        LOGGER.i("Discovered a total of " + this.discoverIteratorSize(launchPlugins) + " plugins");
        return launchPlugins;
    }

    @Nonnull
    private List<LaunchPlugin> discoverFromPaths() {
        final List<LaunchPlugin> launchPlugins = Lists.newArrayList();
        final List<Path> candidateFiles = this.discoverPaths();
        candidateFiles.forEach(it -> this.loadLaunchPlugin(it, launchPlugins));
        return launchPlugins;
    }

    @Nonnull
    private List<Path> discoverPaths() {
        if (this.transformersPaths.isEmpty()) {
            this.transformersPaths.addAll(this.getCandidates(this.hackGameRootFromLauncher()));
        }
        return this.transformersPaths;
    }

    @Nonnull
    private Path hackGameRootFromLauncher() {
        try {
            return this.reflectLauncher();
        } catch (@Nonnull final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Nonnull
    @SuppressWarnings("SpellCheckingInspection")
    private Path reflectLauncher() throws ReflectiveOperationException {
        LOGGER.d("Attempting to obtain game directory through ModLauncher's internals. This may go really badly");
        final Class<?> launcherClass = Class.forName("cpw.mods.modlauncher.Launcher");
        final Field launcherInstance = this.getAccessible(launcherClass, "INSTANCE");
        final Field argumentHandlerField = this.getAccessible(launcherClass, "argumentHandler");
        final Object argumentHandlerObject = argumentHandlerField.get(launcherInstance.get(null));
        final Class<?> argumentHandlerClass = argumentHandlerObject.getClass();
        final Method setPathMethod = this.getAccessible(argumentHandlerClass, "setArgs", String[].class);
        final Field argsField = this.getAccessible(argumentHandlerClass, "args");
        final Object gameDir = setPathMethod.invoke(argumentHandlerObject, argsField.get(argumentHandlerObject));
        return (Path) gameDir;
    }

    @Nonnull
    private Field getAccessible(@Nonnull final Class<?> clazz, @Nonnull final String name) throws ReflectiveOperationException {
        final Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    @Nonnull
    @SuppressWarnings("SameParameterValue")
    private Method getAccessible(@Nonnull final Class<?> clazz, @Nonnull final String name, @Nonnull final Class<?>... params) throws ReflectiveOperationException {
        final Method method = clazz.getDeclaredMethod(name, params);
        method.setAccessible(true);
        return method;
    }

    @Nonnull
    private List<Path> getCandidates(@Nonnull final Path gameRoot) {
        final List<Path> paths = Lists.newArrayList();

        final Path modsDirectory = gameRoot.resolve("mods");
        if (!Files.exists(modsDirectory)) {
            LOGGER.w("Unable to find 'mods' directory. This is not a good thing");
        } else {
            paths.addAll(this.getCandidatesInDirectory(modsDirectory));
        }

        final Path transformersDirectory = modsDirectory.resolve("transformers");
        if (!Files.exists(transformersDirectory)) {
            LOGGER.w("Unable to find 'mods/transformers' directory. This is not a good thing");
        } else {
            paths.addAll(this.getCandidatesInDirectory(transformersDirectory));
        }

        return paths;
    }

    @Nonnull
    private List<Path> getCandidatesInDirectory(@Nonnull final Path root) {
        final List<Path> paths = Lists.newArrayList();
        try {
            Files.walk(root, 1).forEach(it -> this.attemptToLoadFile(it, paths));
        } catch (@Nonnull final IOException e) {
            LOGGER.e("An error has occurred while discovering Launch Plugins!", e);
        }
        return paths;
    }

    private void attemptToLoadFile(@Nonnull final Path file, @Nonnull final List<Path> paths) {
        if (!Files.isRegularFile(file)) return; // It's something like "." or ".." or weird stuff
        if (!this.isJarFile(file)) return; // Transformers must be packaged in JAR files
        if (!this.hasContent(file)) return; // Empty files are bogus
        try {
            if (this.examineZipFile(file)) {
                LOGGER.i("Found valid Launch Plugin '" + file + "'. Adding to loading queue");
                paths.add(file);
            }
        } catch (@Nonnull final IOException e) {
            throw new WrappedInputOutputException(e);
        }
    }

    private boolean isJarFile(@Nonnull final Path file) {
        return file.toString().endsWith(".jar");
    }

    private boolean hasContent(@Nonnull final Path file) {
        try {
            return Files.size(file) != 0;
        } catch (@Nonnull final IOException e) {
            throw new WrappedInputOutputException(e);
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private boolean examineZipFile(@Nonnull final Path file) throws IOException {
        return this.doWithZipFile(file, jar -> {
            Objects.requireNonNull(jar);
            return this.getTargetEntry(jar, "META-INF/services/net.thesilkminer.mc.fermion.asm.api.LaunchPlugin") != null;
        });
    }

    @Nullable
    @SuppressWarnings("SameParameterValue")
    private ZipEntry getTargetEntry(@Nonnull final ZipFile file, @Nonnull final String name) {
        return file.getEntry(name);
    }

    private void loadLaunchPlugin(@Nonnull final Path file, @Nonnull final List<LaunchPlugin> plugins) {
        try {
            this.loadLaunchPluginFromZip(file, plugins);
        } catch (@Nonnull final IOException e) {
            throw new WrappedInputOutputException(e);
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private void loadLaunchPluginFromZip(@Nonnull final Path file, @Nonnull final List<LaunchPlugin> plugins) throws IOException {
        this.doWithZipFile(file, jar -> {
            Objects.requireNonNull(jar);
            final ZipEntry entry = this.getTargetEntry(jar, "META-INF/services/net.thesilkminer.mc.fermion.asm.api.LaunchPlugin");
            Objects.requireNonNull(entry);
            try (final BufferedReader stream = new BufferedReader(new InputStreamReader(jar.getInputStream(entry)))) {
                final String line = stream.readLine();
                plugins.add(this.loadLaunchPlugin(line));
            }
            return null;
        });
    }

    private <T> T doWithZipFile(@Nonnull final Path file, @Nonnull final ExceptionalFunction<ZipFile, T, IOException> consumer) throws IOException {
        try (final ZipFile jar = new ZipFile(new File(file.toUri()))) {
            return consumer.apply(jar);
        }
    }

    @Nonnull
    private LaunchPlugin loadLaunchPlugin(@Nonnull final String className) {
        try {
            return this.loadLaunchPluginWithReflection(className);
        } catch (@Nonnull final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    private LaunchPlugin loadLaunchPluginWithReflection(@Nonnull final String className) throws ReflectiveOperationException {
        final Class<?> launchPluginClass = Class.forName(className, false, this.getClass().getClassLoader());
        return (LaunchPlugin) launchPluginClass.newInstance();
    }

    private int discoverIteratorSize(@Nonnull final Iterable<?> iterable) {
        /*mutable*/ int size = 0;
        for (@Nonnull @SuppressWarnings("unused") final Object o : iterable) {
            ++size;
        }
        return size;
    }

}
