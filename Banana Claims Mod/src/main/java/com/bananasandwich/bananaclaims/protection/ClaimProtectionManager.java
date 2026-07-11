package com.bananasandwich.bananaclaims.protection;

import com.bananasandwich.bananaclaims.claim.Claim;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.BlockEvents;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.ArmorStandItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Registers all public Fabric protection hooks and exposes the same policy to
 * the small number of vanilla mixins required for pressure plates, explosions,
 * item frames, boats, and minecarts.
 */
public final class ClaimProtectionManager {

    private static final long DENIAL_MESSAGE_COOLDOWN_TICKS = 20L;

    private final ClaimProtectionService protectionService;

    private final Map<DenialKey, Long> lastDenialMessages =
            new HashMap<>();

    private boolean registered;

    public ClaimProtectionManager(
            ClaimProtectionService protectionService
    ) {
        this.protectionService =
                Objects.requireNonNull(
                        protectionService,
                        "protectionService"
                );
    }

    public void register() {
        if (registered) {
            return;
        }

        registered = true;

        PlayerBlockBreakEvents.BEFORE.register(
                this::allowBlockBreak
        );

        BlockEvents.USE_ITEM_ON.register(
                this::useItemOnBlock
        );

        BlockEvents.USE_WITHOUT_ITEM.register(
                this::useBlockWithoutItem
        );

        ItemEvents.USE_ON.register(
                this::useItemOn
        );

        UseEntityCallback.EVENT.register(
                this::useEntity
        );

        AttackEntityCallback.EVENT.register(
                this::attackEntity
        );

        ServerLivingEntityEvents.ALLOW_DAMAGE.register(
                this::allowLivingEntityDamage
        );
    }

    public ClaimProtectionService getProtectionService() {
        return protectionService;
    }

    /**
     * Shared entry point for mixins and event callbacks.
     *
     * @return true when the action must be cancelled
     */
    public boolean shouldPrevent(
            ServerLevel level,
            BlockPos position,
            ServerPlayer responsiblePlayer,
            ProtectionAction action,
            boolean notifyPlayer
    ) {
        Optional<Claim> blockingClaim =
                protectionService.findBlockingClaim(
                        level,
                        position,
                        responsiblePlayer,
                        action
                );

        if (blockingClaim.isEmpty()) {
            return false;
        }

        if (notifyPlayer
                && responsiblePlayer != null) {
            sendDenialMessage(
                    responsiblePlayer,
                    action
            );
        }

        return true;
    }

    private boolean allowBlockBreak(
            Level level,
            Player player,
            BlockPos position,
            BlockState state,
            BlockEntity blockEntity
    ) {
        ServerPlayer serverPlayer =
                asServerPlayer(player);

        ServerLevel serverLevel =
                asServerLevel(level);

        if (serverPlayer == null
                || serverLevel == null) {
            return true;
        }

        return !shouldPrevent(
                serverLevel,
                position,
                serverPlayer,
                ProtectionAction.BREAK_BLOCKS,
                true
        );
    }

    private InteractionResult useItemOnBlock(
            ItemStack itemStack,
            BlockState blockState,
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        ServerPlayer serverPlayer =
                asServerPlayer(player);

        ServerLevel serverLevel =
                asServerLevel(level);

        if (serverPlayer == null
                || serverLevel == null) {
            return null;
        }

        boolean blockItem =
                itemStack.getItem()
                        instanceof BlockItem;

        boolean placingAgainstBlock =
                blockItem
                        && serverPlayer.isSecondaryUseActive();

        if (placingAgainstBlock) {
            return null;
        }

        if (isContainerBlock(
                serverLevel,
                position,
                blockState
        )) {
            return denyResult(
                    serverLevel,
                    position,
                    serverPlayer,
                    ProtectionAction.CONTAINER_INTERACTION
            );
        }

        boolean intendsBlockInteraction =
                !blockItem
                        || isExplicitInteractiveBlock(
                        serverLevel,
                        position,
                        blockState
                );

        if (!intendsBlockInteraction) {
            return null;
        }

        return denyResult(
                serverLevel,
                position,
                serverPlayer,
                ProtectionAction.BLOCK_INTERACTION
        );
    }

    private InteractionResult useBlockWithoutItem(
            BlockState blockState,
            Level level,
            BlockPos position,
            Player player,
            BlockHitResult hitResult
    ) {
        ServerPlayer serverPlayer =
                asServerPlayer(player);

        ServerLevel serverLevel =
                asServerLevel(level);

        if (serverPlayer == null
                || serverLevel == null) {
            return null;
        }

        if (isContainerBlock(
                serverLevel,
                position,
                blockState
        )) {
            return denyResult(
                    serverLevel,
                    position,
                    serverPlayer,
                    ProtectionAction.CONTAINER_INTERACTION
            );
        }

        if (!isExplicitInteractiveBlock(
                serverLevel,
                position,
                blockState
        )) {
            return null;
        }

        return denyResult(
                serverLevel,
                position,
                serverPlayer,
                ProtectionAction.BLOCK_INTERACTION
        );
    }

    private InteractionResult useItemOn(
            UseOnContext context
    ) {
        Player player =
                context.getPlayer();

        ServerPlayer serverPlayer =
                asServerPlayer(player);

        ServerLevel serverLevel =
                asServerLevel(
                        context.getLevel()
                );

        if (serverPlayer == null
                || serverLevel == null) {
            return null;
        }

        Item item =
                context.getItemInHand()
                        .getItem();

        if (item instanceof BlockItem) {
            BlockPlaceContext placementContext =
                    new BlockPlaceContext(context);

            return denyResult(
                    serverLevel,
                    placementContext.getClickedPos(),
                    serverPlayer,
                    ProtectionAction.PLACE_BLOCKS
            );
        }

        if (!isProtectedEntityPlacementItem(item)) {
            return null;
        }

        BlockPos entityPosition;

        if (item instanceof HangingEntityItem) {
            entityPosition =
                    context.getClickedPos()
                            .relative(
                                    context.getClickedFace()
                            );
        } else if (item instanceof ArmorStandItem) {
            entityPosition =
                    new BlockPlaceContext(context)
                            .getClickedPos();
        } else {
            entityPosition =
                    context.getClickedPos();
        }

        return denyResult(
                serverLevel,
                entityPosition,
                serverPlayer,
                ProtectionAction.ENTITY_PLACEMENT
        );
    }

    private InteractionResult useEntity(
            Player player,
            Level level,
            InteractionHand hand,
            Entity entity,
            EntityHitResult hitResult
    ) {
        ServerPlayer serverPlayer =
                asServerPlayer(player);

        ServerLevel serverLevel =
                asServerLevel(level);

        if (serverPlayer == null
                || serverLevel == null
                || entity instanceof Player) {
            return InteractionResult.PASS;
        }

        ProtectionAction action =
                entity instanceof ContainerEntity
                        ? ProtectionAction.CONTAINER_INTERACTION
                        : ProtectionAction.ENTITY_INTERACTION;

        return shouldPrevent(
                serverLevel,
                entity.blockPosition(),
                serverPlayer,
                action,
                true
        )
                ? InteractionResult.FAIL
                : InteractionResult.PASS;
    }

    private InteractionResult attackEntity(
            Player player,
            Level level,
            InteractionHand hand,
            Entity entity,
            EntityHitResult hitResult
    ) {
        ServerPlayer serverPlayer =
                asServerPlayer(player);

        ServerLevel serverLevel =
                asServerLevel(level);

        if (serverPlayer == null
                || serverLevel == null
                || entity == serverPlayer) {
            return InteractionResult.PASS;
        }

        ProtectionAction action =
                entity instanceof Player
                        ? ProtectionAction.PVP
                        : ProtectionAction.ENTITY_DAMAGE;

        return shouldPrevent(
                serverLevel,
                entity.blockPosition(),
                serverPlayer,
                action,
                true
        )
                ? InteractionResult.FAIL
                : InteractionResult.PASS;
    }

    private boolean allowLivingEntityDamage(
            LivingEntity entity,
            DamageSource source,
            float amount
    ) {
        ServerLevel serverLevel =
                asServerLevel(
                        entity.level()
                );

        if (serverLevel == null) {
            return true;
        }

        ServerPlayer responsiblePlayer =
                protectionService.resolveResponsiblePlayer(
                        source
                );

        ProtectionAction action;

        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            action = ProtectionAction.EXPLOSION;
        } else if (entity instanceof Player) {
            if (responsiblePlayer == null
                    || responsiblePlayer == entity) {
                return true;
            }

            action = ProtectionAction.PVP;
        } else {
            if (responsiblePlayer == null) {
                return true;
            }

            action = ProtectionAction.ENTITY_DAMAGE;
        }

        return !shouldPrevent(
                serverLevel,
                entity.blockPosition(),
                responsiblePlayer,
                action,
                true
        );
    }

    private InteractionResult denyResult(
            ServerLevel level,
            BlockPos position,
            ServerPlayer player,
            ProtectionAction action
    ) {
        return shouldPrevent(
                level,
                position,
                player,
                action,
                true
        )
                ? InteractionResult.FAIL
                : null;
    }

    private void sendDenialMessage(
            ServerPlayer player,
            ProtectionAction action
    ) {
        MinecraftServer server =
                player.level()
                        .getServer();

        if (server == null) {
            return;
        }

        long currentTick =
                server.getTickCount();

        DenialKey denialKey =
                new DenialKey(
                        player.getUUID(),
                        action
                );

        Long previousTick =
                lastDenialMessages.get(denialKey);

        if (previousTick != null
                && currentTick - previousTick
                < DENIAL_MESSAGE_COOLDOWN_TICKS) {
            return;
        }

        lastDenialMessages.put(
                denialKey,
                currentTick
        );

        player.sendSystemMessage(
                Component.literal(
                        action.getDenialMessage()
                )
        );
    }

    private static boolean isProtectedEntityPlacementItem(
            Item item
    ) {
        return item instanceof ArmorStandItem
                || item instanceof HangingEntityItem
                || item instanceof MinecartItem;
    }

    private static boolean isContainerBlock(
            ServerLevel level,
            BlockPos position,
            BlockState blockState
    ) {
        if (blockState.getBlock()
                instanceof EnderChestBlock) {
            return true;
        }

        BlockEntity blockEntity =
                level.getBlockEntity(position);

        return blockEntity instanceof Container;
    }

    private static boolean isExplicitInteractiveBlock(
            ServerLevel level,
            BlockPos position,
            BlockState blockState
    ) {
        Object block =
                blockState.getBlock();

        return block instanceof DoorBlock
                || block instanceof TrapDoorBlock
                || block instanceof FenceGateBlock
                || block instanceof ButtonBlock
                || block instanceof LeverBlock
                || block instanceof BasePressurePlateBlock
                || blockState.getMenuProvider(
                level,
                position
        ) != null;
    }

    private static ServerPlayer asServerPlayer(
            Player player
    ) {
        return player instanceof ServerPlayer serverPlayer
                ? serverPlayer
                : null;
    }

    private static ServerLevel asServerLevel(
            Level level
    ) {
        return level instanceof ServerLevel serverLevel
                ? serverLevel
                : null;
    }

    private record DenialKey(
            UUID playerUuid,
            ProtectionAction action
    ) {
    }
}
