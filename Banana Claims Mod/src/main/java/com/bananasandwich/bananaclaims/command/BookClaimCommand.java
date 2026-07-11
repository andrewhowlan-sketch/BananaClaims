package com.bananasandwich.bananaclaims.command;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.bananasandwich.bananaclaims.claim.Claim;
import com.bananasandwich.bananaclaims.localization.BananaClaimsMessages;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class BookClaimCommand {

    private BookClaimCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("book")
                .executes(context -> open(context.getSource()))
                .then(Commands.literal("action")
                        .then(Commands.argument("token", StringArgumentType.word())
                                .then(Commands.argument("action", StringArgumentType.word())
                                        .then(Commands.argument("value", StringArgumentType.word())
                                                .executes(context -> action(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "token"),
                                                        StringArgumentType.getString(context, "action"),
                                                        StringArgumentType.getString(context, "value")
                                                ))))))
                .then(Commands.argument("claim", StringArgumentType.word())
                        .suggests(ClaimSuggestions.PARTICIPATING_CLAIMS)
                        .executes(context -> openNamed(
                                context.getSource(),
                                StringArgumentType.getString(context, "claim")
                        )));
    }

    public static int open(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        return Bananaclaims.BOOK_MANAGER.open(player) ? 1 : 0;
    }

    private static int openNamed(CommandSourceStack source, String claimName)
            throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<Claim> claim = ClaimResolver.findParticipatingByName(
                player.getUUID(),
                claimName
        );
        if (claim.isEmpty()) {
            source.sendFailure(BananaClaimsMessages.text(
                    "command.bananaclaims.book.no_access",
                    claimName
            ));
            return 0;
        }
        return Bananaclaims.BOOK_MANAGER.open(player, claim.get()) ? 1 : 0;
    }

    private static int action(
            CommandSourceStack source,
            String token,
            String action,
            String value
    ) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        return Bananaclaims.BOOK_MANAGER.handleAction(
                player,
                token,
                action,
                value
        );
    }
}
