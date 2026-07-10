package com.bananasandwich.bananaclaims.notification;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.bananasandwich.bananaclaims.claim.Claim;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ClaimNotificationManager {

    private static final Map<UUID, PlayerLocationState> PLAYER_STATES =
            new HashMap<>();

    private static int tickCounter = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;

            if (tickCounter < 10) {
                return;
            }

            tickCounter = 0;

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                checkPlayerClaimLocation(player);
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                PLAYER_STATES.remove(handler.player.getUUID())
        );
    }

    private static void checkPlayerClaimLocation(ServerPlayer player) {
        UUID playerUuid = player.getUUID();

        String dimension = player.level().dimension().toString();
        ChunkPos chunkPos = player.chunkPosition();
        long claimRevision = Bananaclaims.CLAIM_MANAGER.getRevision();

        PlayerLocationState previousState = PLAYER_STATES.get(playerUuid);

        if (previousState != null
                && previousState.matchesLocation(
                dimension,
                chunkPos.x(),
                chunkPos.z()
        )
                && previousState.claimRevision == claimRevision) {
            return;
        }

        Optional<Claim> currentClaim =
                Bananaclaims.CLAIM_MANAGER.getClaimAt(
                        dimension,
                        chunkPos.x(),
                        chunkPos.z()
                );

        LastKnownClaim currentKnownClaim = currentClaim
                .map(ClaimNotificationManager::toLastKnownClaim)
                .orElse(null);

        LastKnownClaim previousKnownClaim = previousState == null
                ? null
                : previousState.claim;

        if (!sameClaim(previousKnownClaim, currentKnownClaim)) {
            if (previousKnownClaim != null) {
                PopupRenderer.showLeave(
                        player,
                        previousKnownClaim.claim
                );
            }

            if (currentKnownClaim != null) {
                PopupRenderer.showEnter(
                        player,
                        currentKnownClaim.claim
                );
            }
        }

        PLAYER_STATES.put(
                playerUuid,
                new PlayerLocationState(
                        dimension,
                        chunkPos.x(),
                        chunkPos.z(),
                        claimRevision,
                        currentKnownClaim
                )
        );
    }

    private static LastKnownClaim toLastKnownClaim(Claim claim) {
        return new LastKnownClaim(
                claim.getOwnerUuid() + "|" + claim.getName(),
                claim
        );
    }

    private static boolean sameClaim(
            LastKnownClaim first,
            LastKnownClaim second
    ) {
        if (first == null && second == null) {
            return true;
        }

        if (first == null || second == null) {
            return false;
        }

        return first.key.equals(second.key);
    }

    private static class PlayerLocationState {

        private final String dimension;
        private final int chunkX;
        private final int chunkZ;
        private final long claimRevision;
        private final LastKnownClaim claim;

        private PlayerLocationState(
                String dimension,
                int chunkX,
                int chunkZ,
                long claimRevision,
                LastKnownClaim claim
        ) {
            this.dimension = dimension;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.claimRevision = claimRevision;
            this.claim = claim;
        }

        private boolean matchesLocation(
                String dimension,
                int chunkX,
                int chunkZ
        ) {
            return this.dimension.equals(dimension)
                    && this.chunkX == chunkX
                    && this.chunkZ == chunkZ;
        }
    }

    private static class LastKnownClaim {

        private final String key;
        private final Claim claim;

        private LastKnownClaim(String key, Claim claim) {
            this.key = key;
            this.claim = claim;
        }
    }
}