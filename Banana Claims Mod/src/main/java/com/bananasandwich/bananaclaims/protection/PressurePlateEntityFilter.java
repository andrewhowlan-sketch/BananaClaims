package com.bananasandwich.bananaclaims.protection;

import com.bananasandwich.bananaclaims.Bananaclaims;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Shared pressure-plate entity filtering used by both normal and weighted
 * pressure plates.
 */
public final class PressurePlateEntityFilter {

    private PressurePlateEntityFilter() {
    }

    public static int countAllowedEntities(
            Level level,
            AABB detectionBox,
            Class<? extends Entity> entityClass,
            BlockPos position
    ) {
        List<? extends Entity> entities =
                level.getEntitiesOfClass(
                        entityClass,
                        detectionBox,
                        entity ->
                                EntitySelector.NO_SPECTATORS.test(entity)
                                        && !entity.isIgnoringBlockTriggers()
                );

        if (!(level instanceof ServerLevel serverLevel)) {
            return entities.size();
        }

        int allowedCount = 0;

        for (Entity entity : entities) {
            if (entity instanceof ServerPlayer player
                    && Bananaclaims.CLAIM_PROTECTION_MANAGER.shouldPrevent(
                    serverLevel,
                    position,
                    player,
                    ProtectionAction.BLOCK_INTERACTION,
                    false
            )) {
                continue;
            }

            allowedCount++;
        }

        return allowedCount;
    }
}
