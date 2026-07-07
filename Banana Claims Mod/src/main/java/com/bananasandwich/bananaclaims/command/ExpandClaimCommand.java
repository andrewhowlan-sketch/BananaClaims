package com.bananasandwich.bananaclaims.command;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.bananasandwich.bananaclaims.claim.Claim;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.List;
import java.util.Optional;

public class ExpandClaimCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("expand")
                .then(Commands.argument("claim", StringArgumentType.word())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            String claimName = StringArgumentType.getString(context, "claim");

                            ChunkPos pos = player.chunkPosition();
                            String dimension = player.level().dimension().toString();

                            if (Bananaclaims.CLAIM_MANAGER.isChunkClaimed(dimension, pos.x(), pos.z())) {
                                context.getSource().sendFailure(Component.literal("This chunk is already claimed."));
                                return 0;
                            }

                            List<Claim> ownedClaims = Bananaclaims.CLAIM_MANAGER.getClaimsForOwner(player.getUUID());

                            Optional<Claim> optionalClaim = ownedClaims.stream()
                                    .filter(claim -> claim.getName().equalsIgnoreCase(claimName))
                                    .findFirst();

                            if (optionalClaim.isEmpty()) {
                                context.getSource().sendFailure(Component.literal("You do not own a claim named \"" + claimName + "\"."));
                                return 0;
                            }

                            Claim claim = optionalClaim.get();

                            claim.addChunk(dimension, pos.x(), pos.z());
                            Bananaclaims.CLAIM_MANAGER.saveClaims();

                            context.getSource().sendSuccess(
                                    () -> Component.literal("Added this chunk to claim \"" + claim.getName() + "\"."),
                                    false
                            );

                            return 1;
                        })
                );
    }
}