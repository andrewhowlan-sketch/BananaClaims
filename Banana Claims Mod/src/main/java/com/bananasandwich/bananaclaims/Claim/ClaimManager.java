package com.bananasandwich.bananaclaims.claim;

import com.bananasandwich.bananaclaims.storage.ClaimStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClaimManager {
    private final List<Claim> claims = new ArrayList<>();
    private ClaimStorage storage;

    public void setStorage(ClaimStorage storage) {
        this.storage = storage;
    }

    public boolean addClaim(Claim claim) {
        if (isChunkClaimed(claim.getDimension(), claim.getChunkX(), claim.getChunkZ())) {
            return false;
        }

        claims.add(claim);
        save();
        return true;
    }

    public void loadClaims(List<Claim> loadedClaims) {
        claims.clear();

        if (loadedClaims != null) {
            claims.addAll(loadedClaims);
        }
    }

    public boolean removeClaim(String dimension, int chunkX, int chunkZ) {
        boolean removed = claims.removeIf(claim ->
                claim.getDimension().equals(dimension)
                        && claim.getChunkX() == chunkX
                        && claim.getChunkZ() == chunkZ
        );

        if (removed) {
            save();
        }

        return removed;
    }

    public Optional<Claim> getClaimAt(String dimension, int chunkX, int chunkZ) {
        return claims.stream()
                .filter(claim ->
                        claim.getDimension().equals(dimension)
                                && claim.getChunkX() == chunkX
                                && claim.getChunkZ() == chunkZ
                )
                .findFirst();
    }

    public List<Claim> getClaimsForOwner(UUID ownerUuid) {
        return claims.stream()
                .filter(claim -> claim.getOwnerUuid().equals(ownerUuid))
                .toList();
    }

    public List<Claim> getAllClaims() {
        return List.copyOf(claims);
    }

    public boolean isChunkClaimed(String dimension, int chunkX, int chunkZ) {
        return getClaimAt(dimension, chunkX, chunkZ).isPresent();
    }

    private void save() {
        if (storage != null) {
            storage.saveClaims(getAllClaims());
        }
    }
}