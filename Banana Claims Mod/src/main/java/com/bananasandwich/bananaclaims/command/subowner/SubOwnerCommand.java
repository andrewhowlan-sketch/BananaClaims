package com.bananasandwich.bananaclaims.command.subowner;

import com.bananasandwich.bananaclaims.command.ClaimSuggestions;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class SubOwnerCommand {

    private SubOwnerCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("subowner")
                .then(Commands.literal("add")
                        .then(AddSubOwnerCommand.localPlayerArgument())
                )
                .then(Commands.literal("remove")
                        .then(RemoveSubOwnerCommand.localPlayerArgument())
                )
                .then(Commands.literal("list")
                        .executes(context ->
                                ListSubOwnerCommand.listCurrentClaim(
                                        context.getSource()
                                )
                        )
                )
                .then(Commands.argument(
                                        "claim",
                                        StringArgumentType.word()
                                )
                                .suggests(ClaimSuggestions.OWNED_CLAIMS)
                                .then(Commands.literal("add")
                                        .then(AddSubOwnerCommand.remotePlayerArgument())
                                )
                                .then(Commands.literal("remove")
                                        .then(RemoveSubOwnerCommand.remotePlayerArgument())
                                )
                                .then(Commands.literal("list")
                                        .executes(context ->
                                                ListSubOwnerCommand.listNamedClaim(
                                                        context.getSource(),
                                                        StringArgumentType.getString(
                                                                context,
                                                                "claim"
                                                        )
                                                )
                                        )
                                )
                );
    }
}
