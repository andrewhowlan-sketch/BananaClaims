package com.bananasandwich.bananaclaims.storage;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.bananasandwich.bananaclaims.claim.Claim;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClaimStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path claimsFile;

    public ClaimStorage() {
        this.claimsFile = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("bananaclaims")
                .resolve("claims.json");
    }

    public List<Claim> loadClaims() {
        return tryLoadClaims()
                .orElseGet(ArrayList::new);
    }

    /**
     * Attempts to load claim data without destroying the caller's current
     * in-memory state when the file cannot be read or parsed.
     */
    public Optional<List<Claim>> tryLoadClaims() {
        try {
            if (!Files.exists(claimsFile)) {
                return Optional.of(new ArrayList<>());
            }

            String json = Files.readString(claimsFile);
            ClaimData data = GSON.fromJson(json, ClaimData.class);

            if (data == null || data.claims == null) {
                return Optional.of(new ArrayList<>());
            }

            return Optional.of(new ArrayList<>(data.claims));
        } catch (IOException | JsonParseException exception) {
            Bananaclaims.LOGGER.error(
                    "Failed to load Banana Claims data.",
                    exception
            );

            return Optional.empty();
        }
    }

    public Path getClaimsFile() {
        return claimsFile;
    }

    public void saveClaims(List<Claim> claims) {
        try {
            Files.createDirectories(claimsFile.getParent());

            ClaimData data = new ClaimData();
            data.claims = claims;

            String json = GSON.toJson(data);
            Files.writeString(claimsFile, json);
        } catch (IOException exception) {
            Bananaclaims.LOGGER.error("Failed to save Banana Claims data.", exception);
        }
    }

    private static class ClaimData {
        private List<Claim> claims = new ArrayList<>();
    }
}