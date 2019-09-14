package net.thesilkminer.mc.fermion.asm.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import net.minecraftforge.fml.loading.FileUtils;
import net.thesilkminer.mc.fermion.asm.api.LaunchPlugin;
import net.thesilkminer.mc.fermion.asm.common.utility.LaunchBlackboard;
import net.thesilkminer.mc.fermion.asm.common.utility.Log;

import javax.annotation.Nonnull;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Consumer;

public final class FermionTransformationService implements ITransformationService {

    private static final Log LOGGER = Log.of("Transformation Service");

    private final LaunchBlackboard blackboard;
    private final Map<String, Boolean> environmentConfiguration;

    public FermionTransformationService() {
        this.blackboard = new LaunchBlackboard();
        this.environmentConfiguration = Maps.newHashMap();
    }

    @Nonnull
    @Override
    public final String name() {
        return "fermion.asm.service";
    }

    @Override
    public final void initialize(@Nonnull final IEnvironment environment) {
        LOGGER.d("Initializing");
        LOGGER.i("Loading configuration files for Fermion environment");
        LOGGER.d("Attempting to find configuration directory");
        final Path gameDirectory = environment.getProperty(IEnvironment.Keys.GAMEDIR.get())
                .orElseThrow(() -> new IllegalStateException("No game directory was found. This is a serious error"));
        final Path configDirectory = gameDirectory.resolve("config/FermionEnv").toAbsolutePath().normalize();
        FileUtils.getOrCreateDirectory(configDirectory, "FermionEnv");
        FileUtils.getOrCreateDirectory(configDirectory.resolve("fermion.asm.service/dump").toAbsolutePath().normalize(), "dumps");
        LOGGER.d("Loading environment configurations");
        this.loadEnvironmentConfiguration(configDirectory);
        this.blackboard.acceptDumpDir(configDirectory.resolve("fermion.asm.service/dump").toAbsolutePath().normalize());
        LOGGER.d("Loading configuration files plugin per plugin");
        this.blackboard.loadConfig(configDirectory);
        LOGGER.i("Configuration loaded");
    }

    @Override
    public final void beginScanning(@Nonnull final IEnvironment environment) {
        LOGGER.d("Begin scanning");
    }

    @Override
    public final void onLoad(@Nonnull final IEnvironment env, @Nonnull final Set<String> otherServices) throws IncompatibleEnvironmentException {
        LOGGER.i("Fermion Transformer Service is being loaded");
        LOGGER.i("Attempting to discover Fermion Launch Plugins");
        final ServiceLoader<LaunchPlugin> launchPluginLoader = ServiceLoader.load(LaunchPlugin.class);
        this.blackboard.accept(launchPluginLoader, env);
        LOGGER.i("Fermion Launch Plugins discovery completed");
    }

    @Nonnull
    @Override
    @SuppressWarnings("rawtypes") // Fuck you: List<ITransformer<?>> was hard, wasn't it
    public final List<ITransformer> transformers() {
        LOGGER.i("Registered Fermion transformer");
        return ImmutableList.of(new FermionTransformer(this.blackboard, this.environmentConfiguration));
    }

    private void loadEnvironmentConfiguration(@Nonnull final Path root) {
        final Path configPath = root.resolve("./environment.json").toAbsolutePath().normalize();
        LOGGER.d("Attempting to read and/or create file " + configPath);
        if (Files.notExists(configPath)) {
            try (final BufferedWriter writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
                writer.write("{}");
                writer.flush();
            } catch (@Nonnull final IOException e) {
                if (e instanceof FileAlreadyExistsException) {
                    LOGGER.w("Weird. Environment configuration file was deemed non-existent, but now it is there. Whatever...");
                } else {
                    throw new RuntimeException("Unable to create environment configuration file", e);
                }
            }
        }

        final Gson gson = new GsonBuilder()
                .serializeNulls()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();

        final JsonObject transformersConfigArray;

        try (final Reader reader = Files.newBufferedReader(configPath)) {

            transformersConfigArray = gson
                    .getAdapter(TypeToken.get(JsonObject.class))
                    .read(new JsonReader(reader));

            this.loadConfigFromJson(transformersConfigArray);
        } catch (@Nonnull final IOException e) {
            throw new RuntimeException("An error has occurred while attempting to read the environment configuration file", e);
        }


        try (final BufferedWriter writer = Files.newBufferedWriter(configPath)) {
            gson.toJson(transformersConfigArray, writer);
        } catch (@Nonnull final IOException e) {
            throw new RuntimeException("An error has occurred while attempting to write the environment configuration file", e);
        }
    }

    private void loadConfigFromJson(@Nonnull final JsonObject object) {
        if (!object.has("dump")) {
            object.add("dump", this.getJsonObject(it -> {
                it.add("__comment", new JsonPrimitive("Whether classes transformed by this class transformer should be dumped to disk"));
                it.add("enabled", new JsonPrimitive(false));
            }));
        }
        if (!object.has("emergency_mode")) {
            object.add("emergency_mode", this.getJsonObject(it -> {
                it.add("__comment", new JsonPrimitive("If emergency mode is enabled, no transformers will load. Can be used for debugging, but DO NOT TURN IT ON!"));
                it.add("enabled", new JsonPrimitive(false));
            }));
        }

        object.entrySet().forEach(it -> this.environmentConfiguration.put(it.getKey(), it.getValue().getAsJsonObject().get("enabled").getAsJsonPrimitive().getAsBoolean()));
    }

    @Nonnull
    private JsonObject getJsonObject(@Nonnull final Consumer<JsonObject> object) {
        final JsonObject obj = new JsonObject();
        object.accept(obj);
        return obj;
    }
}
