package com.bananasandwich.bananaclaims.command;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.bananasandwich.bananaclaims.claim.Claim;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.Optional;

public class RenameClaimCommand {

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("rename")
                .then(Commands.argument("newName", StringArgumentType.word())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ChunkPos chunkPos = player.chunkPosition();
                            String dimension = player.level().dimension().toString();
                            String newName = StringArgumentType.getString(context, "newName");

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

                            String oldName = claim.getName();
                            claim.setName(newName);
                            Bananaclaims.CLAIM_MANAGER.saveClaims();

                            context.getSource().sendSuccess(
                                    () -> Component.literal("Claim renamed from \"" + oldName + "\" to \"" + newName + "\"."),
                                    false
                            );

                            return 1;
                        })
                );
    }
}