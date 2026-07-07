package com.bananasandwich.bananaclaims.claim;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClaimManager {
    private final List<Claim> claims = new ArrayList<>();

    public boolean addClaim(Claim claim) {
        if (isChunkClaimed(claim.getDimension(), claim.getChunkX(), claim.getChunkZ())) {
            return false;
        }

        claims.add(claim);
        return true;
    }

    public void loadClaims(List<Claim> loadedClaims) {
        claims.clear();

        if (loadedClaims != null) {
            claims.addAll(loadedClaims);
        }
    }

    public boolean removeClaim(String dimension, int chunkX, int chunkZ) {
        return claims.removeIf(claim ->
                claim.getDimension().equals(dimension)
                        && claim.getChunkX() == chunkX
                        && claim.getChunkZ() == chunkZ
        );
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
}