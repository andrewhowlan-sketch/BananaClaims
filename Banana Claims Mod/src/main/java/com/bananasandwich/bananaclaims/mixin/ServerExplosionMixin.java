package com.bananasandwich.bananaclaims.mixin;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.bananasandwich.bananaclaims.protection.ProtectionAction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies the explosions flag per affected claim instead of cancelling the
 * whole explosion globally. Protected blocks and entities are removed from
 * the vanilla explosion work lists while unclaimed areas remain unaffected.
 */
@Mixin(ServerExplosion.class)
public abstract class ServerExplosionMixin {

    @Shadow
    @Final
    private ServerLevel level;

    @Shadow
    @Final
    private Entity source;

    @Shadow
    @Final
    private DamageSource damageSource;

    @Inject(
            method = "calculateExplodedPositions",
            at = @At("RETURN"),
            cancellable = true
    )
    private void bananaclaims$filterProtectedExplosionBlocks(
            CallbackInfoReturnable<List<BlockPos>> callbackInfo
    ) {
        List<BlockPos> originalPositions =
                callbackInfo.getReturnValue();

        if (originalPositions == null
                || originalPositions.isEmpty()) {
            return;
        }

        ServerPlayer responsiblePlayer =
                resolveResponsiblePlayer();

        List<BlockPos> allowedPositions =
                new ArrayList<>(
                        originalPositions.size()
                );

        for (BlockPos position : originalPositions) {
            boolean protectedPosition =
                    Bananaclaims.CLAIM_PROTECTION_MANAGER
                            .shouldPrevent(
                                    level,
                                    position,
                                    responsiblePlayer,
                                    ProtectionAction.EXPLOSION,
                                    true
                            );

            if (!protectedPosition) {
                allowedPositions.add(position);
            }
        }

        if (allowedPositions.size()
                != originalPositions.size()) {
            callbackInfo.setReturnValue(
                    allowedPositions
            );
        }
    }

    @Redirect(
            method = "hurtEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"
            )
    )
    private List<Entity> bananaclaims$filterProtectedExplosionEntities(
            ServerLevel serverLevel,
            Entity excludedEntity,
            AABB bounds
    ) {
        List<Entity> originalEntities =
                serverLevel.getEntities(
                        excludedEntity,
                        bounds
                );

        if (originalEntities.isEmpty()) {
            return originalEntities;
        }

        ServerPlayer responsiblePlayer =
                resolveResponsiblePlayer();

        List<Entity> allowedEntities =
                new ArrayList<>(
                        originalEntities.size()
                );

        for (Entity entity : originalEntities) {
            boolean protectedEntity =
                    Bananaclaims.CLAIM_PROTECTION_MANAGER
                            .shouldPrevent(
                                    level,
                                    entity.blockPosition(),
                                    responsiblePlayer,
                                    ProtectionAction.EXPLOSION,
                                    true
                            );

            if (!protectedEntity) {
                allowedEntities.add(entity);
            }
        }

        return allowedEntities;
    }
    private ServerPlayer resolveResponsiblePlayer() {
        ServerPlayer responsiblePlayer =
                Bananaclaims.CLAIM_PROTECTION_SERVICE
                        .resolveResponsiblePlayer(
                                damageSource
                        );

        if (responsiblePlayer != null) {
            return responsiblePlayer;
        }

        return Bananaclaims.CLAIM_PROTECTION_SERVICE
                .resolveResponsiblePlayer(source);
    }

}
