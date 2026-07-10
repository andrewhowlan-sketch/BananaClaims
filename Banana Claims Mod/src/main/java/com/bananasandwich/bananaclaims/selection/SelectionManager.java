package com.bananasandwich.bananaclaims.selection;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionManager {

    private final Map<UUID, ClaimSelection> selections = new HashMap<>();

    public void setPos1(UUID playerUuid, String dimension, BlockPos position) {
        ClaimSelection selection = getOrCreateSelection(playerUuid);

        selection.setPos1(position);
        selection.setPos1Dimension(dimension);
    }

    public void setPos2(UUID playerUuid, String dimension, BlockPos position) {
        ClaimSelection selection = getOrCreateSelection(playerUuid);

        selection.setPos2(position);
        selection.setPos2Dimension(dimension);
    }

    public ClaimSelection getSelection(UUID playerUuid) {
        return selections.get(playerUuid);
    }

    public void clearSelection(UUID playerUuid) {
        selections.remove(playerUuid);
    }

    private ClaimSelection getOrCreateSelection(UUID playerUuid) {
        return selections.computeIfAbsent(
                playerUuid,
                uuid -> new ClaimSelection()
        );
    }
}