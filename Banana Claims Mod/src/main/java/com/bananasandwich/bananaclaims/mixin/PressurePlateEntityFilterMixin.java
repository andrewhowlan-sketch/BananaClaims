package com.bananasandwich.bananaclaims.mixin;

import com.bananasandwich.bananaclaims.protection.PressurePlateEntityFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Filters unauthorized players out of normal pressure-plate entity counts.
 */
@Mixin(PressurePlateBlock.class)
public abstract class PressurePlateEntityFilterMixin {

    @Redirect(
            method = "getSignalStrength",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/PressurePlateBlock;getEntityCount(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/AABB;Ljava/lang/Class;)I"
            )
    )
    private int bananaclaims$countAllowedPressurePlateEntities(
            Level level,
            AABB detectionBox,
            Class<? extends Entity> entityClass,
            Level methodLevel,
            BlockPos position
    ) {
        return PressurePlateEntityFilter.countAllowedEntities(
                level,
                detectionBox,
                entityClass,
                position
        );
    }
}
