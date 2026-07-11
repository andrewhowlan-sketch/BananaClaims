package com.bananasandwich.bananaclaims.mixin;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.bananasandwich.bananaclaims.protection.ProtectionAction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Covers non-living protected entities that are not reached by Fabric's
 * living-entity damage event: item frames, boats, and minecarts.
 */
@Mixin({
        ItemFrame.class,
        VehicleEntity.class
})
public abstract class ProtectedEntityDamageMixin {

    @Inject(
            method = "hurtServer",
            at = @At("HEAD"),
            cancellable = true
    )
    private void bananaclaims$protectNonLivingEntityDamage(
            ServerLevel level,
            DamageSource source,
            float amount,
            CallbackInfoReturnable<Boolean> callbackInfo
    ) {
        Entity entity =
                (Entity) (Object) this;

        ServerPlayer responsiblePlayer =
                Bananaclaims.CLAIM_PROTECTION_SERVICE
                        .resolveResponsiblePlayer(source);

        ProtectionAction action;

        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            action = ProtectionAction.EXPLOSION;
        } else {
            if (responsiblePlayer == null) {
                return;
            }

            action = ProtectionAction.ENTITY_DAMAGE;
        }

        if (Bananaclaims.CLAIM_PROTECTION_MANAGER.shouldPrevent(
                level,
                entity.blockPosition(),
                responsiblePlayer,
                action,
                true
        )) {
            callbackInfo.setReturnValue(false);
        }
    }
}
