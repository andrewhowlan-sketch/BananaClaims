package com.bananasandwich.bananaclaims.command.admin;

import com.bananasandwich.bananaclaims.claim.Claim;
import com.bananasandwich.bananaclaims.claim.ClaimChunk;
import com.bananasandwich.bananaclaims.claim.ClaimMember;
import com.bananasandwich.bananaclaims.claim.ClaimSubOwner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Read-only integrity diagnostics for claims loaded in memory.
 */
public final class ClaimDiagnostics {

    private ClaimDiagnostics() {
    }

    public static GlobalReport analyze(
            List<Claim> claims
    ) {
        List<Claim> safeClaims = claims == null
                ? List.of()
                : claims.stream()
                .filter(claim -> claim != null)
                .toList();

        Set<UUID> ownerUuids = new HashSet<>();
        Set<UUID> claimIds = new HashSet<>();
        Map<String, Integer> dimensionCounts = new LinkedHashMap<>();
        Map<ChunkKey, Integer> chunkUseCounts = new HashMap<>();
        List<String> warnings = new ArrayList<>();

        int totalChunks = 0;

        for (Claim claim : safeClaims) {
            UUID ownerUuid = claim.getOwnerUuid();
            UUID claimId = claim.getClaimId();
            Set<ClaimChunk> chunks = claim.getChunks();

            if (ownerUuid == null) {
                warnings.add(
                        label(claim)
                                + " has no owner UUID."
                );
            } else {
                ownerUuids.add(ownerUuid);
            }

            if (!claimIds.add(claimId)) {
                warnings.add(
                        label(claim)
                                + " duplicates claim ID "
                                + claimId
                                + "."
                );
            }

            if (claim.getName() == null
                    || claim.getName().isBlank()) {
                warnings.add(
                        label(claim)
                                + " has a blank name."
                );
            }

            if (chunks.isEmpty()) {
                warnings.add(
                        label(claim)
                                + " has no chunks."
                );
            }

            Set<String> claimDimensions = new HashSet<>();

            for (ClaimChunk chunk : chunks) {
                totalChunks++;
                claimDimensions.add(chunk.getDimension());
                dimensionCounts.merge(
                        chunk.getDimension(),
                        1,
                        Integer::sum
                );
                chunkUseCounts.merge(
                        new ChunkKey(
                                chunk.getDimension(),
                                chunk.getChunkX(),
                                chunk.getChunkZ()
                        ),
                        1,
                        Integer::sum
                );
            }

            if (claimDimensions.size() > 1) {
                warnings.add(
                        label(claim)
                                + " contains chunks from multiple dimensions."
                );
            }

            appendRoleWarnings(
                    claim,
                    warnings
            );
        }

        long overlappingChunks = chunkUseCounts.values()
                .stream()
                .filter(count -> count > 1)
                .count();

        if (overlappingChunks > 0) {
            warnings.add(
                    overlappingChunks
                            + " chunk position(s) are assigned to multiple claims."
            );
        }

        return new GlobalReport(
                safeClaims.size(),
                totalChunks,
                chunkUseCounts.size(),
                ownerUuids.size(),
                Map.copyOf(dimensionCounts),
                List.copyOf(warnings)
        );
    }

    public static List<String> analyzeClaim(
            Claim claim
    ) {
        if (claim == null) {
            return List.of("Claim is null.");
        }

        List<String> warnings = new ArrayList<>();

        if (claim.getOwnerUuid() == null) {
            warnings.add("Owner UUID is missing.");
        }

        if (claim.getName() == null
                || claim.getName().isBlank()) {
            warnings.add("Claim name is blank.");
        }

        if (claim.getChunks().isEmpty()) {
            warnings.add("Claim has no chunks.");
        }

        long dimensionCount = claim.getChunks()
                .stream()
                .map(ClaimChunk::getDimension)
                .distinct()
                .count();

        if (dimensionCount > 1) {
            warnings.add("Claim contains chunks from multiple dimensions.");
        }

        appendRoleWarnings(
                claim,
                warnings
        );

        return List.copyOf(warnings);
    }

    private static void appendRoleWarnings(
            Claim claim,
            List<String> warnings
    ) {
        UUID ownerUuid = claim.getOwnerUuid();
        Set<UUID> memberUuids = claim.getMembers()
                .stream()
                .map(ClaimMember::getUuid)
                .collect(HashSet::new, Set::add, Set::addAll);
        Set<UUID> subOwnerUuids = claim.getSubOwners()
                .stream()
                .map(ClaimSubOwner::getUuid)
                .collect(HashSet::new, Set::add, Set::addAll);

        if (ownerUuid != null
                && memberUuids.contains(ownerUuid)) {
            warnings.add("Owner also appears in the member list.");
        }

        if (ownerUuid != null
                && subOwnerUuids.contains(ownerUuid)) {
            warnings.add("Owner also appears in the subowner list.");
        }

        Set<UUID> overlap = new HashSet<>(memberUuids);
        overlap.retainAll(subOwnerUuids);

        if (!overlap.isEmpty()) {
            warnings.add(
                    overlap.size()
                            + " player(s) appear as both member and subowner."
            );
        }
    }

    private static String label(
            Claim claim
    ) {
        String name = claim.getName();

        if (name == null || name.isBlank()) {
            name = "<unnamed>";
        }

        return "Claim \""
                + name
                + "\" ["
                + claim.getClaimId()
                + "]";
    }

    private record ChunkKey(
            String dimension,
            int chunkX,
            int chunkZ
    ) {
    }

    public record GlobalReport(
            int claimCount,
            int totalChunkAssignments,
            int uniqueChunkCount,
            int uniqueOwnerCount,
            Map<String, Integer> chunksByDimension,
            List<String> warnings
    ) {
        public boolean healthy() {
            return warnings.isEmpty();
        }
    }
}
