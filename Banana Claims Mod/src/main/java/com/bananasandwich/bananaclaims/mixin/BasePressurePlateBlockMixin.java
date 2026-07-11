package com.bananasandwich.bananaclaims.mixin;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.bananasandwich.bananaclaims.protection.ProtectionAction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents an unauthorized player from initiating a pressure-plate update.
 * The companion entity-filter mixin also keeps unauthorized players from
 * holding an already-powered plate down during scheduled checks.
 */
@Mixin(BasePressurePlateBlock.class)
public abstract class BasePressurePlateBlockMixin {

    @Inject(
            method = "entityInside",
            at = @At("HEAD"),
            cancellable = true
    )
    private void bananaclaims$protectPressurePlateEntry(
            BlockState state,
            Level level,
            BlockPos position,
            Entity entity,
            InsideBlockEffectApplier effectApplier,
            boolean isPrecise,
            CallbackInfo callbackInfo
    ) {
        if (!(level instanceof ServerLevel serverLevel)
                || !(entity instanceof ServerPlayer player)) {
            return;
        }

        if (Bananaclaims.CLAIM_PROTECTION_MANAGER.shouldPrevent(
                serverLevel,
                position,
                player,
                ProtectionAction.BLOCK_INTERACTION,
                true
        )) {
            callbackInfo.cancel();
        }
    }
}
