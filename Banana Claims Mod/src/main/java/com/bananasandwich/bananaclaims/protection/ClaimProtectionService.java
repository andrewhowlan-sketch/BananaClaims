package com.bananasandwich.bananaclaims.protection;

import com.bananasandwich.bananaclaims.claim.Claim;
import com.bananasandwich.bananaclaims.claim.ClaimManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.ChunkPos;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Central claim-protection policy service.
 *
 * <p>Every event and mixin routes through this class so role bypasses, flag
 * meaning, claim lookup, and damage attribution remain consistent.</p>
 */
public final class ClaimProtectionService {

    private static final int MAX_OWNER_RESOLUTION_DEPTH = 8;

    private final ClaimManager claimManager;

    public ClaimProtectionService(
            ClaimManager claimManager
    ) {
        this.claimManager =
                Objects.requireNonNull(
                        claimManager,
                        "claimManager"
                );
    }

    public Optional<Claim> findClaimAt(
            ServerLevel level,
            BlockPos position
    ) {
        if (level == null
                || position == null) {
            return Optional.empty();
        }

        ChunkPos chunkPosition =
                ChunkPos.containing(position);

        return claimManager.getClaimAt(
                level.dimension().toString(),
                chunkPosition.x(),
                chunkPosition.z()
        );
    }

    public Optional<Claim> findBlockingClaim(
            ServerLevel level,
            BlockPos position,
            ServerPlayer responsiblePlayer,
            ProtectionAction action
    ) {
        UUID responsibleUuid =
                responsiblePlayer == null
                        ? null
                        : responsiblePlayer.getUUID();

        return findBlockingClaim(
                level,
                position,
                responsibleUuid,
                action
        );
    }

    public Optional<Claim> findBlockingClaim(
            ServerLevel level,
            BlockPos position,
            UUID responsibleUuid,
            ProtectionAction action
    ) {
        if (action == null) {
            return Optional.empty();
        }

        Optional<Claim> optionalClaim =
                findClaimAt(level, position);

        if (optionalClaim.isEmpty()) {
            return Optional.empty();
        }

        Claim claim = optionalClaim.get();

        if (!action.isEnabled(claim.getFlags())) {
            return Optional.empty();
        }

        if (responsibleUuid != null
                && claim.hasAccess(responsibleUuid)) {
            return Optional.empty();
        }

        return Optional.of(claim);
    }

    public boolean isDenied(
            ServerLevel level,
            BlockPos position,
            ServerPlayer responsiblePlayer,
            ProtectionAction action
    ) {
        return findBlockingClaim(
                level,
                position,
                responsiblePlayer,
                action
        ).isPresent();
    }

    public ServerPlayer resolveResponsiblePlayer(
            DamageSource source
    ) {
        if (source == null) {
            return null;
        }

        ServerPlayer responsiblePlayer =
                resolveResponsiblePlayer(
                        source.getEntity()
                );

        if (responsiblePlayer != null) {
            return responsiblePlayer;
        }

        return resolveResponsiblePlayer(
                source.getDirectEntity()
        );
    }

    public ServerPlayer resolveResponsiblePlayer(
            Entity entity
    ) {
        Set<Entity> visited =
                Collections.newSetFromMap(
                        new IdentityHashMap<>()
                );

        return resolveResponsiblePlayer(
                entity,
                visited,
                0
        );
    }

    private ServerPlayer resolveResponsiblePlayer(
            Entity entity,
            Set<Entity> visited,
            int depth
    ) {
        if (entity == null
                || depth >= MAX_OWNER_RESOLUTION_DEPTH
                || !visited.add(entity)) {
            return null;
        }

        if (entity instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }

        if (entity instanceof PrimedTnt primedTnt) {
            ServerPlayer owner =
                    resolveResponsiblePlayer(
                            primedTnt.getOwner(),
                            visited,
                            depth + 1
                    );

            if (owner != null) {
                return owner;
            }
        }

        if (entity instanceof TraceableEntity traceableEntity) {
            ServerPlayer owner =
                    resolveResponsiblePlayer(
                            traceableEntity.getOwner(),
                            visited,
                            depth + 1
                    );

            if (owner != null) {
                return owner;
            }
        }

        if (entity instanceof OwnableEntity ownableEntity) {
            return resolveResponsiblePlayer(
                    ownableEntity.getRootOwner(),
                    visited,
                    depth + 1
            );
        }

        return null;
    }
}
