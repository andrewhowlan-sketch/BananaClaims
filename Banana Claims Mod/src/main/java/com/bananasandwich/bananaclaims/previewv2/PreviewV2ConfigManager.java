package com.bananasandwich.bananaclaims.previewv2;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Owns loading, validation, material resolution, and persistence for Renderer
 * V2 configuration.
 */
public final class PreviewV2ConfigManager {

    public static final String CONFIG_FILE_NAME =
            "bananaclaims-preview.json";

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create();

    private final Path configPath;

    private volatile Snapshot snapshot =
            createSnapshot(
                    PreviewV2Config.defaults()
            );

    public PreviewV2ConfigManager() {
        this(
                FabricLoader.getInstance()
                        .getConfigDir()
                        .resolve(CONFIG_FILE_NAME)
        );
    }

    PreviewV2ConfigManager(
            Path configPath
    ) {
        this.configPath = configPath;
    }

    public synchronized void load() {
        PreviewV2Config loadedConfig;
        boolean shouldSave = false;

        try {
            Files.createDirectories(
                    configPath.getParent()
            );

            if (!Files.exists(configPath)) {
                loadedConfig =
                        PreviewV2Config.defaults();
                shouldSave = true;
            } else {
                loadedConfig =
                        readConfig();
            }
        } catch (IOException | JsonParseException exception) {
            backupInvalidConfig();

            Bananaclaims.LOGGER.error(
                    "Failed to load Renderer V2 config '{}'. "
                            + "Defaults will be used.",
                    configPath,
                    exception
            );

            loadedConfig =
                    PreviewV2Config.defaults();
            shouldSave = true;
        }

        shouldSave |= loadedConfig.sanitize();

        Snapshot preparedSnapshot =
                createSnapshot(loadedConfig);

        shouldSave |= applyResolvedMaterialIdentifiers(
                loadedConfig,
                preparedSnapshot.materials()
        );

        if (shouldSave) {
            writeConfigSafely(loadedConfig);
        }

        snapshot =
                new Snapshot(
                        loadedConfig,
                        preparedSnapshot.materials()
                );

        Bananaclaims.LOGGER.info(
                "Renderer V2 config loaded from '{}'.",
                configPath
        );
    }

    public synchronized boolean reload() {
        try {
            PreviewV2Config loadedConfig =
                    readConfig();

            boolean changed =
                    loadedConfig.sanitize();

            Snapshot preparedSnapshot =
                    createSnapshot(loadedConfig);

            changed |= applyResolvedMaterialIdentifiers(
                    loadedConfig,
                    preparedSnapshot.materials()
            );

            if (changed) {
                writeConfig(loadedConfig);
            }

            snapshot =
                    new Snapshot(
                            loadedConfig,
                            preparedSnapshot.materials()
                    );

            Bananaclaims.LOGGER.info(
                    "Renderer V2 config reloaded from '{}'.",
                    configPath
            );

            return true;
        } catch (IOException | JsonParseException exception) {
            Bananaclaims.LOGGER.error(
                    "Failed to reload Renderer V2 config '{}'. "
                            + "The previously loaded settings remain active.",
                    configPath,
                    exception
            );

            return false;
        }
    }

    public synchronized boolean save(
            PreviewV2Config config
    ) {
        PreviewV2Config configToSave =
                config == null
                        ? PreviewV2Config.defaults()
                        : config.copy();

        configToSave.sanitize();

        Snapshot preparedSnapshot =
                createSnapshot(configToSave);

        applyResolvedMaterialIdentifiers(
                configToSave,
                preparedSnapshot.materials()
        );

        try {
            writeConfig(configToSave);

            snapshot =
                    new Snapshot(
                            configToSave,
                            preparedSnapshot.materials()
                    );

            return true;
        } catch (IOException exception) {
            Bananaclaims.LOGGER.error(
                    "Failed to save Renderer V2 config '{}'.",
                    configPath,
                    exception
            );

            return false;
        }
    }

    public PreviewV2Config getConfig() {
        return snapshot.config()
                .copy();
    }

    public Path getConfigPath() {
        return configPath;
    }

    public String getDurationDescription() {
        BigDecimal seconds =
                BigDecimal.valueOf(
                                getConfig().getDurationTicks()
                        )
                        .divide(
                                BigDecimal.valueOf(20L)
                        )
                        .stripTrailingZeros();

        String unit =
                seconds.compareTo(BigDecimal.ONE) == 0
                        ? " second"
                        : " seconds";

        return seconds.toPlainString() + unit;
    }

    Snapshot snapshot() {
        return snapshot;
    }

    private PreviewV2Config readConfig()
            throws IOException {
        try (BufferedReader reader =
                     Files.newBufferedReader(
                             configPath,
                             StandardCharsets.UTF_8
                     )) {
            PreviewV2Config loadedConfig =
                    GSON.fromJson(
                            reader,
                            PreviewV2Config.class
                    );

            if (loadedConfig == null) {
                throw new JsonParseException(
                        "Config file did not contain a JSON object."
                );
            }

            return loadedConfig;
        }
    }

    private void writeConfigSafely(
            PreviewV2Config config
    ) {
        try {
            writeConfig(config);
        } catch (IOException exception) {
            Bananaclaims.LOGGER.error(
                    "Failed to write Renderer V2 config '{}'. "
                            + "The settings remain active in memory.",
                    configPath,
                    exception
            );
        }
    }

    private void writeConfig(
            PreviewV2Config config
    ) throws IOException {
        Files.createDirectories(
                configPath.getParent()
        );

        Path temporaryPath =
                configPath.resolveSibling(
                        CONFIG_FILE_NAME + ".tmp"
                );

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             temporaryPath,
                             StandardCharsets.UTF_8
                     )) {
            GSON.toJson(config, writer);
        }

        try {
            Files.move(
                    temporaryPath,
                    configPath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (IOException atomicMoveFailure) {
            Files.move(
                    temporaryPath,
                    configPath,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private void backupInvalidConfig() {
        if (!Files.exists(configPath)) {
            return;
        }

        Path backupPath =
                configPath.resolveSibling(
                        CONFIG_FILE_NAME
                                + ".invalid-"
                                + System.currentTimeMillis()
                );

        try {
            Files.move(
                    configPath,
                    backupPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            Bananaclaims.LOGGER.warn(
                    "Invalid Renderer V2 config moved to '{}'.",
                    backupPath
            );
        } catch (IOException exception) {
            Bananaclaims.LOGGER.error(
                    "Failed to back up invalid Renderer V2 config '{}'.",
                    configPath,
                    exception
            );
        }
    }

    private static Snapshot createSnapshot(
            PreviewV2Config config
    ) {
        return new Snapshot(
                config,
                new PreviewV2Materials(config)
        );
    }

    private static boolean applyResolvedMaterialIdentifiers(
            PreviewV2Config config,
            PreviewV2Materials materials
    ) {
        boolean changed = false;

        if (!config.getBorder()
                .getMaterial()
                .equals(materials.getBorderIdentifier())) {
            config.getBorder()
                    .setMaterial(
                            materials.getBorderIdentifier()
                    );
            changed = true;
        }

        if (!config.getCorners()
                .getMaterial()
                .equals(materials.getCornerIdentifier())) {
            config.getCorners()
                    .setMaterial(
                            materials.getCornerIdentifier()
                    );
            changed = true;
        }

        if (!config.getGuides()
                .getMaterial()
                .equals(materials.getGuideIdentifier())) {
            config.getGuides()
                    .setMaterial(
                            materials.getGuideIdentifier()
                    );
            changed = true;
        }

        return changed;
    }

    record Snapshot(
            PreviewV2Config config,
            PreviewV2Materials materials
    ) {
    }
}


