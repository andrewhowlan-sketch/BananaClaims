package com.bananasandwich.bananaclaims.command;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.bananasandwich.bananaclaims.claim.Claim;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.List;

public class ExpandClaimCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {

        return Commands.literal("expand")
                .executes(context -> {

                    ServerPlayer player = context.getSource().getPlayerOrException();

                    ChunkPos pos = player.chunkPosition();
                    String dimension = player.level().dimension().toString();

                    if (Bananaclaims.CLAIM_MANAGER.isChunkClaimed(dimension, pos.x(), pos.z())) {
                        context.getSource().sendFailure(Component.literal("This chunk is already claimed."));
                        return 0;
                    }

                    List<Claim> claims = Bananaclaims.CLAIM_MANAGER.getClaimsForOwner(player.getUUID());

                    if (claims.isEmpty()) {
                        context.getSource().sendFailure(Component.literal("You do not own any claims."));
                        return 0;
                    }

                    if (claims.size() > 1) {
                        context.getSource().sendFailure(Component.literal("You own multiple claims. Support for selecting which claim to expand is coming next."));
                        return 0;
                    }

                    Claim claim = claims.getFirst();

                    claim.addChunk(dimension, pos.x(), pos.z());

                    Bananaclaims.CLAIM_MANAGER.saveClaims();

                    context.getSource().sendSuccess(
                            () -> Component.literal("Added chunk to \"" + claim.getName() + "\"."),
                            false
                    );

                    return 1;
                });

    }

}