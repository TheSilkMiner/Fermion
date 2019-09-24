package net.thesilkminer.mc.fermion.asm.common.utility;

import com.google.common.collect.Lists;
import cpw.mods.gross.Java9ClassLoaderUtil;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Enumeration;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public final class LaunchPluginDiscoverer {

    private static final class LaunchPluginClassLoader extends URLClassLoader {
        private LaunchPluginClassLoader(@Nonnull final URL[] urls, @Nonnull final ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        protected void addURL(@Nullable final URL url) {
            super.addURL(url);
        }

        private void addPaths(@Nonnull final List<Path> paths) {
            paths.stream()
                    .filter(Objects::nonNull)
                    .map(Path::toUri)
                    .map(this::toUrl)
                    .forEach(this::addURL);
        }

        @Nonnull
        private URL toUrl(@Nonnull final URI uri) {
            try {
                return uri.toURL();
            } catch (@Nonnull final MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }

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

    private Path fermionJar = null;

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

    public void extractFermion(final boolean isEnabled) {
        LOGGER.i("Copying Fermion Launch Plugin JAR to allow it to be loaded by Forge's classloader");
        LOGGER.d("So, this is a FUCKING DIRTY HACK! For some reason Forge thought that services can only be discovered once");
        LOGGER.d("This means that files that are identified as transformers will not get loaded again, because 'boo hoo classloading'");
        LOGGER.d("So fuck off, we'll copy the files over and let Forge go fuck itself.");
        LOGGER.d("If you're worried about leftovers, you need to be, but Fermion is good enough to cleanup after himself");
        LOGGER.d("We'll delete all the fucking files when the client or server shuts down, I promise");

        if (Objects.isNull(this.fermionJar)) {
            LOGGER.e("Actually, this is a gigantic issue... WE DO NOT EXIST!!!", new IllegalStateException("Fermion JAR path is null, but Fermion is currently loading. Like, WTF?"));
            LOGGER.e("Maybe we're in a dev environment? I'll skip processing for now");
            return;
        }

        final Path newJarFilePath = this.fermionJar.resolve("./transformers/../../__extraction__fermion_extraction__mod_file_Fermion_mod.jar").normalize();
        Runtime.getRuntime().addShutdownHook(new Thread(() ->  {
            try {
                Files.deleteIfExists(newJarFilePath);
            } catch (@Nonnull final IOException e) {
                LOGGER.e("Well, nice", e);
            }
        }));

        if (!isEnabled) {
            LOGGER.w("Disabling due to request: this won't go well, honey");
            return;
        }

        this.doCopy(this.fermionJar, newJarFilePath);
    }

    private void doCopy(@Nonnull final Path in, @Nonnull final Path out) {
        try {
            this.doWithZipFile(in, inFile -> {
                Objects.requireNonNull(inFile);
                LOGGER.d("Using '" + in + "' as input");
                LOGGER.d("Using '" + out + "' as output");
                if (Files.exists(out)) {
                    LOGGER.d("For some reason the output file exists... Deleting");
                    Files.delete(out);
                }
                Files.createFile(out);
                try {
                    Thread.sleep(1000);
                } catch (@Nonnull final InterruptedException ignored) {}
                this.doZipFileCopy(inFile, out.toAbsolutePath().normalize().toFile());
                return null;
            });
        } catch (@Nonnull final IOException e) {
            throw new WrappedInputOutputException(e);
        }
    }

    private void doZipFileCopy(@Nonnull final ZipFile in, @Nonnull final File out) throws IOException {
        try (@Nonnull final ZipOutputStream o = new ZipOutputStream(new FileOutputStream(out))) {
            final Enumeration<? extends ZipEntry> entries = in.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                if (entry.getName().startsWith("META-INF/services/")) continue;
                final ZipEntry outEntry = new ZipEntry(entry.getName());
                o.putNextEntry(outEntry);
                try (@Nonnull final BufferedInputStream bufIn = new BufferedInputStream(in.getInputStream(entry))) {
                    while (bufIn.available() > 0) {
                        o.write(bufIn.read());
                    }
                }
                o.closeEntry();
            }
            o.finish();
        }
    }

    @Nonnull
    private List<LaunchPlugin> discoverFromPaths() {
        final List<LaunchPlugin> launchPlugins = Lists.newArrayList();
        final List<Path> candidateFiles = this.discoverPaths();
        final LaunchPluginClassLoader loader = new LaunchPluginClassLoader(Java9ClassLoaderUtil.getSystemClassPathURLs(), this.getClass().getClassLoader());
        loader.addPaths(candidateFiles);
        candidateFiles.forEach(it -> this.loadLaunchPlugin(it, launchPlugins, loader));
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
            if (this.examineZipFileForFermionJar(file)) {
                LOGGER.i("Found Fermion JAR file '" + file + "'");
                this.fermionJar = file;
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

    @SuppressWarnings("SpellCheckingInspection")
    private boolean examineZipFileForFermionJar(@Nonnull final Path file) throws IOException {
        return this.doWithZipFile(file, jar -> {
            Objects.requireNonNull(jar);
            return this.getTargetEntry(jar, "META-INF/.fermionlocation") != null;
        });
    }

    @Nullable
    @SuppressWarnings("SameParameterValue")
    private ZipEntry getTargetEntry(@Nonnull final ZipFile file, @Nonnull final String name) {
        return file.getEntry(name);
    }

    private void loadLaunchPlugin(@Nonnull final Path file, @Nonnull final List<LaunchPlugin> plugins, @Nonnull final ClassLoader loader) {
        try {
            this.loadLaunchPluginFromZip(file, plugins, loader);
        } catch (@Nonnull final IOException e) {
            throw new WrappedInputOutputException(e);
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private void loadLaunchPluginFromZip(@Nonnull final Path file, @Nonnull final List<LaunchPlugin> plugins, @Nonnull final ClassLoader loader) throws IOException {
        this.doWithZipFile(file, jar -> {
            Objects.requireNonNull(jar);
            final ZipEntry entry = this.getTargetEntry(jar, "META-INF/services/net.thesilkminer.mc.fermion.asm.api.LaunchPlugin");
            Objects.requireNonNull(entry);
            try (final BufferedReader stream = new BufferedReader(new InputStreamReader(jar.getInputStream(entry)))) {
                final String line = stream.readLine();
                plugins.add(this.loadLaunchPlugin(line, loader));
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
    private LaunchPlugin loadLaunchPlugin(@Nonnull final String className, @Nonnull final ClassLoader loader) {
        try {
            return this.loadLaunchPluginWithReflection(className, loader);
        } catch (@Nonnull final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    private LaunchPlugin loadLaunchPluginWithReflection(@Nonnull final String className, @Nonnull final ClassLoader loader) throws ReflectiveOperationException {
        final Class<?> launchPluginClass = Class.forName(className, false, loader);
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
