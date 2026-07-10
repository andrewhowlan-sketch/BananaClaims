package com.bananasandwich.bananaclaims.claim;

import com.bananasandwich.bananaclaims.storage.ClaimStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ClaimManager {

    private final List<Claim> claims = new ArrayList<>();
    private final Map<ClaimChunkKey, Claim> chunkLookup = new HashMap<>();

    private ClaimStorage storage;
    private long revision = 0;

    public void setStorage(ClaimStorage storage) {
        this.storage = storage;
    }

    public boolean addClaim(Claim claim) {
        for (ClaimChunk chunk : claim.getChunks()) {
            if (isChunkClaimed(
                    chunk.getDimension(),
                    chunk.getChunkX(),
                    chunk.getChunkZ()
            )) {
                return false;
            }
        }

        claims.add(claim);
        rebuildChunkLookup();
        save();

        return true;
    }

    public void loadClaims(List<Claim> loadedClaims) {
        claims.clear();

        if (loadedClaims != null) {
            claims.addAll(loadedClaims);
        }

        rebuildChunkLookup();
    }

    public boolean removeClaim(String dimension, int chunkX, int chunkZ) {
        Claim claim = chunkLookup.get(
                new ClaimChunkKey(dimension, chunkX, chunkZ)
        );

        if (claim == null) {
            return false;
        }

        boolean removed = claims.remove(claim);

        if (removed) {
            rebuildChunkLookup();
            save();
        }

        return removed;
    }

    public void saveClaims() {
        rebuildChunkLookup();
        save();
    }

    public Optional<Claim> getClaimAt(
            String dimension,
            int chunkX,
            int chunkZ
    ) {
        return Optional.ofNullable(
                chunkLookup.get(
                        new ClaimChunkKey(dimension, chunkX, chunkZ)
                )
        );
    }

    public List<Claim> getClaimsForOwner(UUID ownerUuid) {
        return claims.stream()
                .filter(claim -> claim.getOwnerUuid().equals(ownerUuid))
                .toList();
    }

    public List<Claim> getAllClaims() {
        return List.copyOf(claims);
    }

    public boolean isChunkClaimed(
            String dimension,
            int chunkX,
            int chunkZ
    ) {
        return chunkLookup.containsKey(
                new ClaimChunkKey(dimension, chunkX, chunkZ)
        );
    }

    public long getRevision() {
        return revision;
    }

    private void rebuildChunkLookup() {
        chunkLookup.clear();

        for (Claim claim : claims) {
            for (ClaimChunk chunk : claim.getChunks()) {
                ClaimChunkKey key = new ClaimChunkKey(
                        chunk.getDimension(),
                        chunk.getChunkX(),
                        chunk.getChunkZ()
                );

                chunkLookup.put(key, claim);
            }
        }

        revision++;
    }

    private void save() {
        if (storage != null) {
            storage.saveClaims(getAllClaims());
        }
    }
}