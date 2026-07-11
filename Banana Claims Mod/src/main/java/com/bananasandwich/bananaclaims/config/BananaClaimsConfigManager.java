package com.bananasandwich.bananaclaims.config;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.bananasandwich.bananaclaims.permission.ClaimPermission;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Loads, validates, persists, and atomically reloads the main server config.
 */
public final class BananaClaimsConfigManager {

    public static final String CONFIG_FILE_NAME =
            "bananaclaims.json";

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create();

    private final Path configPath;

    private volatile BananaClaimsConfig config =
            BananaClaimsConfig.defaults();

    public BananaClaimsConfigManager() {
        this(
                FabricLoader.getInstance()
                        .getConfigDir()
                        .resolve(CONFIG_FILE_NAME)
        );
    }

    BananaClaimsConfigManager(
            Path configPath
    ) {
        this.configPath = configPath;
    }

    public synchronized void load() {
        BananaClaimsConfig loadedConfig;
        boolean shouldSave = false;

        try {
            Files.createDirectories(
                    configPath.getParent()
            );

            if (!Files.exists(configPath)) {
                loadedConfig =
                        BananaClaimsConfig.defaults();
                shouldSave = true;
            } else {
                loadedConfig = readConfig();
            }
        } catch (IOException | JsonParseException exception) {
            backupInvalidConfig();

            Bananaclaims.LOGGER.error(
                    "Failed to load Banana Claims config '{}'. Defaults will be used.",
                    configPath,
                    exception
            );

            loadedConfig =
                    BananaClaimsConfig.defaults();
            shouldSave = true;
        }

        shouldSave |= loadedConfig.sanitize();

        if (shouldSave) {
            writeConfigSafely(loadedConfig);
        }

        config = loadedConfig;

        Bananaclaims.LOGGER.info(
                "Banana Claims config loaded from '{}'.",
                configPath
        );
    }

    public synchronized boolean reload() {
        try {
            BananaClaimsConfig loadedConfig =
                    readConfig();

            boolean changed =
                    loadedConfig.sanitize();

            if (changed) {
                writeConfig(loadedConfig);
            }

            config = loadedConfig;

            Bananaclaims.LOGGER.info(
                    "Banana Claims config reloaded from '{}'.",
                    configPath
            );

            return true;
        } catch (IOException | JsonParseException exception) {
            Bananaclaims.LOGGER.error(
                    "Failed to reload Banana Claims config '{}'. The previous settings remain active.",
                    configPath,
                    exception
            );

            return false;
        }
    }

    public BananaClaimsConfig getConfig() {
        return config.copy();
    }

    public Path getConfigPath() {
        return configPath;
    }

    public boolean isFabricPermissionsApiEnabled() {
        return config.getPermissions()
                .isFabricPermissionsApiEnabled();
    }

    public int getFallbackLevel(
            ClaimPermission permission
    ) {
        return config.getPermissions()
                .resolveFallbackLevel(permission);
    }

    public boolean areProtectionDenialMessagesEnabled() {
        return config.getProtection()
                .isDenialMessagesEnabled();
    }

    public int getProtectionDenialCooldownTicks() {
        return config.getProtection()
                .getDenialMessageCooldownTicks();
    }

    public boolean areInvitationsEnabled() {
        return config.getInvitations().isEnabled();
    }

    public int getInvitationExpirationSeconds() {
        return config.getInvitations().getExpirationSeconds();
    }

    public int getMaxPendingInvitationsPerClaim() {
        return config.getInvitations().getMaxPendingPerClaim();
    }

    public boolean shouldNotifyInvitationExpiration() {
        return config.getInvitations().isNotifyOnExpiration();
    }

    private BananaClaimsConfig readConfig()
            throws IOException {
        if (!Files.exists(configPath)) {
            throw new IOException(
                    "Config file does not exist: "
                            + configPath
            );
        }

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             configPath,
                             StandardCharsets.UTF_8
                     )) {
            BananaClaimsConfig loadedConfig =
                    GSON.fromJson(
                            reader,
                            BananaClaimsConfig.class
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
            BananaClaimsConfig configToWrite
    ) {
        try {
            writeConfig(configToWrite);
        } catch (IOException exception) {
            Bananaclaims.LOGGER.error(
                    "Failed to write Banana Claims config '{}'. Settings remain active in memory.",
                    configPath,
                    exception
            );
        }
    }

    private void writeConfig(
            BananaClaimsConfig configToWrite
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
            GSON.toJson(configToWrite, writer);
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
                    "Invalid Banana Claims config moved to '{}'.",
                    backupPath
            );
        } catch (IOException exception) {
            Bananaclaims.LOGGER.error(
                    "Failed to back up invalid Banana Claims config '{}'.",
                    configPath,
                    exception
            );
        }
    }
}
