package com.bananasandwich.bananaclaims.command;

import com.bananasandwich.bananaclaims.Bananaclaims;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public class DeleteClaimCommand {

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("delete")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ChunkPos chunkPos = player.chunkPosition();
                    String dimension = player.level().dimension().toString();

                    boolean removed = Bananaclaims.CLAIM_MANAGER.removeClaim(
                            dimension,
                            chunkPos.x(),
                            chunkPos.z()
                    );

                    if (!removed) {
                        context.getSource().sendFailure(
                                Component.literal("There is no claim here to delete.")
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