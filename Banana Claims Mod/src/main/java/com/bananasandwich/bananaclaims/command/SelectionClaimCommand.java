package com.bananasandwich.bananaclaims.command;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SelectionClaimCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> registerPos1() {
        return Commands.literal("pos1")
                .executes(context -> setPos1(context.getSource()));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> registerPos2() {
        return Commands.literal("pos2")
                .executes(context -> setPos2(context.getSource()));
    }

    private static int setPos1(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        BlockPos position = player.blockPosition();
        String dimension = player.level().dimension().toString();

        Bananaclaims.SELECTION_MANAGER.setPos1(
                player.getUUID(),
                dimension,
                position
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Claim position 1 set to "
                                + position.getX() + ", "
                                + position.getY() + ", "
                                + position.getZ() + "."
                ),
                false
        );

        return 1;
    }

    private static int setPos2(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        BlockPos position = player.blockPosition();
        String dimension = player.level().dimension().toString();

        Bananaclaims.SELECTION_MANAGER.setPos2(
                player.getUUID(),
                dimension,
                position
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Claim position 2 set to "
                                + position.getX() + ", "
                                + position.getY() + ", "
                                + position.getZ() + "."
                ),
                false
        );

        return 1;
    }
}