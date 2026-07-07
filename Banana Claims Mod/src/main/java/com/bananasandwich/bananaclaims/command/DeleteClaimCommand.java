package com.bananasandwich.bananaclaims.command;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.bananasandwich.bananaclaims.claim.Claim;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.Optional;

public class DeleteClaimCommand {

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("delete")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ChunkPos chunkPos = player.chunkPosition();
                    String dimension = player.level().dimension().toString();

                    Optional<Claim> optionalClaim = Bananaclaims.CLAIM_MANAGER.getClaimAt(
                            dimension,
                            chunkPos.x(),
                            chunkPos.z()
                    );

                    if (optionalClaim.isEmpty()) {
                        context.getSource().sendFailure(
                                Component.literal("There is no claim here.")
                        );
                        return 0;
                    }

                    Claim claim = optionalClaim.get();

                    if (!claim.isOwner(player.getUUID())) {
                        context.getSource().sendFailure(
                                Component.literal("You do not own this claim.")
                        );
                        return 0;
                    }

                    boolean removed = Bananaclaims.CLAIM_MANAGER.removeClaim(
                            dimension,
                            chunkPos.x(),
                            chunkPos.z()
                    );

                    if (!removed) {
                        context.getSource().sendFailure(
                                Component.literal("Unable to delete claim.")
                        );
                        return 0;
                    }

                    context.getSource().sendSuccess(
                            () -> Component.literal("Claim deleted successfully."),
                            false
                    );

                    return 1;
                });
    }
}