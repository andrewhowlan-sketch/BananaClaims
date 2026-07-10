package com.bananasandwich.bananaclaims.command;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.bananasandwich.bananaclaims.claim.Claim;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

public final class LeaveClaimCommand {

    private LeaveClaimCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("leave")
                .executes(context ->
                        leaveCurrentClaim(
                                context.getSource()
                        )
                )
                .then(
                        Commands.argument(
                                        "claim",
                                        StringArgumentType.word()
                                )
                                .suggests(
                                        ClaimSuggestions.LEAVABLE_CLAIMS
                                )
                                .executes(context ->
                                        leaveNamedClaim(
                                                context.getSource(),
                                                StringArgumentType.getString(
                                                        context,
                                                        "claim"
                                                )
                                        )
                                )
                );
    }

    private static int leaveCurrentClaim(
            CommandSourceStack source
    ) throws CommandSyntaxException {
        ServerPlayer player =
                source.getPlayerOrException();

        Optional<Claim> optionalClaim =
                ClaimResolver.findAtPlayer(player);

        if (optionalClaim.isEmpty()) {
            source.sendFailure(
                    Component.literal(
                            "There is no claim here."
                    )
            );

            return 0;
        }

        return leaveClaim(
                source,
                player,
                optionalClaim.get()
        );
    }

    private static int leaveNamedClaim(
            CommandSourceStack source,
            String claimName
    ) throws CommandSyntaxException {
        ServerPlayer player =
                source.getPlayerOrException();

        Optional<Claim> optionalClaim =
                Bananaclaims.CLAIM_MANAGER
                        .getAllClaims()
                        .stream()
                        .filter(claim ->
                                claim.getName() != null
                                        && claim.getName()
                                        .equalsIgnoreCase(claimName)
                                        && (
                                        claim.isOwner(player.getUUID())
                                                || claim.canLeave(
                                                player.getUUID()
                                        )
                                )
                        )
                        .findFirst();

        if (optionalClaim.isEmpty()) {
            source.sendFailure(
                    Component.literal(
                            "You are not a member or subowner of a claim named \""
                                    + claimName
                                    + "\"."
                    )
            );

            return 0;
        }

        return leaveClaim(
                source,
                player,
                optionalClaim.get()
        );
    }

    private static int leaveClaim(
            CommandSourceStack source,
            ServerPlayer player,
            Claim claim
    ) {
        UUID playerUuid = player.getUUID();

        if (claim.isOwner(playerUuid)) {
            source.sendFailure(
                    Component.literal(
                            "You own claim \""
                                    + claim.getName()
                                    + "\". Transfer ownership or delete the claim before leaving."
                    )
            );

            return 0;
        }

        if (claim.isSubOwner(playerUuid)) {
            boolean demoted =
                    claim.demoteSubOwnerToMember(
                            playerUuid
                    );

            if (!demoted) {
                source.sendFailure(
                        Component.literal(
                                "Unable to step down from claim \""
                                        + claim.getName()
                                        + "\"."
                        )
                );

                return 0;
            }

            Bananaclaims.CLAIM_MANAGER.saveClaims();

            source.sendSuccess(
                    () -> Component.literal(
                            "You stepped down as a subowner of claim \""
                                    + claim.getName()
                                    + "\" and remain a regular member."
                    ),
                    false
            );

            notifyOwner(
                    source,
                    claim,
                    player,
                    player.getName().getString()
                            + " stepped down as a subowner of claim \""
                            + claim.getName()
                            + "\" and remains a member."
            );

            return 1;
        }

        if (claim.isMember(playerUuid)) {
            boolean removed =
                    claim.removeMember(playerUuid);

            if (!removed) {
                source.sendFailure(
                        Component.literal(
                                "Unable to leave claim \""
                                        + claim.getName()
                                        + "\"."
                        )
                );

                return 0;
            }

            Bananaclaims.CLAIM_MANAGER.saveClaims();

            source.sendSuccess(
                    () -> Component.literal(
                            "You have left claim \""
                                    + claim.getName()
                                    + "\"."
                    ),
                    false
            );

            notifyOwner(
                    source,
                    claim,
                    player,
                    player.getName().getString()
                            + " left claim \""
                            + claim.getName()
                            + "\"."
            );

            return 1;
        }

        source.sendFailure(
                Component.literal(
                        "You are not a member or subowner of claim \""
                                + claim.getName()
                                + "\"."
                )
        );

        return 0;
    }

    private static void notifyOwner(
            CommandSourceStack source,
            Claim claim,
            ServerPlayer leavingPlayer,
            String message
    ) {
        UUID ownerUuid = claim.getOwnerUuid();

        if (ownerUuid == null
                || ownerUuid.equals(leavingPlayer.getUUID())) {
            return;
        }

        source.getServer()
                .getPlayerList()
                .getPlayers()
                .stream()
                .filter(player ->
                        ownerUuid.equals(player.getUUID())
                )
                .findFirst()
                .ifPresent(owner ->
                        owner.sendSystemMessage(
                                Component.literal(message)
                        )
                );
    }
}
