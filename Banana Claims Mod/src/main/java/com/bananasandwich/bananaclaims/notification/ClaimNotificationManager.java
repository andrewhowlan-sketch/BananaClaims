package com.bananasandwich.bananaclaims.notification;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.bananasandwich.bananaclaims.claim.Claim;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ClaimNotificationManager {
    private static final Map<UUID, String> LAST_KNOWN_CLAIMS = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                checkPlayerClaimLocation(player);
            }
        });
    }

    private static void checkPlayerClaimLocation(ServerPlayer player) {
        UUID playerUuid = player.getUUID();

        String dimension = player.level().dimension().toString();
        ChunkPos chunkPos = player.chunkPosition();

        Optional<Claim> currentClaim = Bananaclaims.CLAIM_MANAGER.getClaimAt(
                dimension,
                chunkPos.x(),
                chunkPos.z()
        );

        String currentClaimKey = currentClaim
                .map(ClaimNotificationManager::getClaimKey)
                .orElse(null);

        String previousClaimKey = LAST_KNOWN_CLAIMS.get(playerUuid);

        if (sameClaim(previousClaimKey, currentClaimKey)) {
            return;
        }

        if (currentClaim.isPresent()) {
            Claim claim = currentClaim.get();
            player.sendSystemMessage(
                    Component.literal("Entering claim: " + claim.getName()),
                    true
            );
        } else if (previousClaimKey != null) {
            player.sendSystemMessage(
                    Component.literal("Leaving claim"),
                    true
            );
        }

        if (currentClaimKey == null) {
            LAST_KNOWN_CLAIMS.remove(playerUuid);
        } else {
            LAST_KNOWN_CLAIMS.put(playerUuid, currentClaimKey);
        }
    }

    private static String getClaimKey(Claim claim) {
        return claim.getOwnerUuid() + "|" + claim.getName();
    }

    private static boolean sameClaim(String first, String second) {
        if (first == null && second == null) {
            return true;
        }

        if (first == null || second == null) {
            return false;
        }

        return first.equals(second);
    }
}