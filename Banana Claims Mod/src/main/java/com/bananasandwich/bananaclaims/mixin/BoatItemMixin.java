package com.bananasandwich.bananaclaims.mixin;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.bananasandwich.bananaclaims.protection.ProtectionAction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Boat placement uses Item#use rather than Item#useOn, so it requires a small
 * targeted hook separate from the shared item-use event.
 */
@Mixin(BoatItem.class)
public abstract class BoatItemMixin {

    @Inject(
            method = "getBoat",
            at = @At("HEAD"),
            cancellable = true
    )
    private void bananaclaims$protectBoatPlacement(
            Level level,
            HitResult hitResult,
            ItemStack itemStack,
            Player player,
            CallbackInfoReturnable<AbstractBoat> callbackInfo
    ) {
        if (!(level instanceof ServerLevel serverLevel)
                || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        BlockPos position =
                BlockPos.containing(
                        hitResult.getLocation()
                );

        if (Bananaclaims.CLAIM_PROTECTION_MANAGER.shouldPrevent(
                serverLevel,
                position,
                serverPlayer,
                ProtectionAction.ENTITY_PLACEMENT,
                true
        )) {
            callbackInfo.setReturnValue(null);
        }
    }
}
