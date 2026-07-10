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

import java.util.Comparator;
import java.util.Optional;

public final class PreviewClaimCommand {

    private PreviewClaimCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("preview")
                .executes(context ->
                        previewCurrentClaim(
                                context.getSource()
                        )
                )
                .then(Commands.literal("stop")
                        .executes(context ->
                                stopPreview(
                                        context.getSource()
                                )
                        )
                )
                .then(Commands.literal("nearest")
                        .executes(context ->
                                previewNearestManagedClaim(
                                        context.getSource()
                                )
                        )
                )
                .then(Commands.argument(
                                        "claim",
                                        StringArgumentType.word()
                                )
                                .suggests(
                                        ClaimSuggestions.MANAGED_CLAIMS
                                )
                                .executes(context ->
                                        previewNamedClaim(
                                                context.getSource(),
                                                StringArgumentType.getString(
                                                        context,
                                                        "claim"
                                                )
                                        )
                                )
                );
    }

    private static int previewCurrentClaim(
            CommandSourceStack source
    ) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

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

        return showClaimPreview(
                source,
                player,
                optionalClaim.get()
        );
    }

    private static int previewNamedClaim(
            CommandSourceStack source,
            String claimName
    ) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

        Optional<Claim> optionalClaim =
                ClaimResolver.findManagedByName(
                        player.getUUID(),
                        claimName
                );

        if (optionalClaim.isEmpty()) {
            source.sendFailure(
                    Component.literal(
                            "You cannot manage a claim named \""
                                    + claimName
                                    + "\"."
                    )
            );

            return 0;
        }

        return showClaimPreview(
                source,
                player,
                optionalClaim.get()
        );
    }

    private static int previewNearestManagedClaim(
            CommandSourceStack source
    ) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        String dimension = player.level().dimension().toString();

        Optional<Claim> optionalClaim =
                Bananaclaims.CLAIM_MANAGER
                        .getAllClaims()
                        .stream()
                        .filter(claim ->
                                claim.canManage(player.getUUID())
                                        && dimension.equals(
                                        claim.getDimension()
                                )
                        )
                        .min(
                                Comparator.comparingDouble(
                                        claim -> distanceSquaredToClaim(
                                                player,
                                                claim
                                        )
                                )
                        );

        if (optionalClaim.isEmpty()) {
            source.sendFailure(
                    Component.literal(
                            "You do not manage any claims in this dimension."
                    )
            );

            return 0;
        }

        return showClaimPreview(
                source,
                player,
                optionalClaim.get()
        );
    }

    private static int stopPreview(
            CommandSourceStack source
    ) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

        boolean stoppedLegacy =
                Bananaclaims.BOUNDARY_PREVIEW_MANAGER.stop(
                        player.getUUID()
                );

        boolean stoppedV2 =
                Bananaclaims.DISPLAY_PREVIEW_V2_MANAGER.stop(
                        player.getUUID()
                );

        if (!stoppedLegacy && !stoppedV2) {
            source.sendFailure(
                    Component.literal(
                            "You do not have an active claim preview."
                    )
            );

            return 0;
        }

        source.sendSuccess(
                () -> Component.literal(
                        "Stopped your claim preview."
                ),
                false
        );

        return 1;
    }

    private static int showClaimPreview(
            CommandSourceStack source,
            ServerPlayer player,
            Claim claim
    ) {
        if (!player.level()
                .dimension()
                .toString()
                .equals(claim.getDimension())) {
            source.sendFailure(
                    Component.literal(
                            "That claim is in another dimension."
                    )
            );

            return 0;
        }

        Bananaclaims.BOUNDARY_PREVIEW_MANAGER.stop(
                player.getUUID()
        );

        boolean created =
                Bananaclaims.DISPLAY_PREVIEW_V2_MANAGER
                        .showClaimDisplay(
                                player,
                                claim
                        );

        if (!created) {
            source.sendFailure(
                    Component.literal(
                            "Unable to create a solid preview for that claim."
                    )
            );

            return 0;
        }

        source.sendSuccess(
                () -> Component.literal(
                        "Showing the solid terrain-following boundary for claim \""
                                + claim.getName()
                                + "\" for 10 seconds."
                ),
                false
        );

        return 1;
    }

    private static double distanceSquaredToClaim(
            ServerPlayer player,
            Claim claim
    ) {
        return claim.getChunks()
                .stream()
                .mapToDouble(chunk -> {
                    double centerX = chunk.getChunkX() * 16.0D + 8.0D;
                    double centerZ = chunk.getChunkZ() * 16.0D + 8.0D;
                    double deltaX = player.getX() - centerX;
                    double deltaZ = player.getZ() - centerZ;

                    return deltaX * deltaX + deltaZ * deltaZ;
                })
                .min()
                .orElse(Double.MAX_VALUE);
    }
}


