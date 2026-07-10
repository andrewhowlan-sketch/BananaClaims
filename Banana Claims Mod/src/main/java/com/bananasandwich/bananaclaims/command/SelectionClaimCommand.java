package com.bananasandwich.bananaclaims.command;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.bananasandwich.bananaclaims.selection.ClaimSelection;
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
                .executes(context ->
                        setPos1(
                                context.getSource()
                        )
                );
    }

    public static LiteralArgumentBuilder<CommandSourceStack> registerPos2() {
        return Commands.literal("pos2")
                .executes(context ->
                        setPos2(
                                context.getSource()
                        )
                );
    }

    private static int setPos1(
            CommandSourceStack source
    ) throws CommandSyntaxException {
        ServerPlayer player =
                source.getPlayerOrException();

        BlockPos position =
                player.blockPosition();

        String dimension =
                player.level()
                        .dimension()
                        .toString();

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

        showSelectionPreviewIfReady(
                source,
                player
        );

        return 1;
    }

    private static int setPos2(
            CommandSourceStack source
    ) throws CommandSyntaxException {
        ServerPlayer player =
                source.getPlayerOrException();

        BlockPos position =
                player.blockPosition();

        String dimension =
                player.level()
                        .dimension()
                        .toString();

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

        showSelectionPreviewIfReady(
                source,
                player
        );

        return 1;
    }

    private static void showSelectionPreviewIfReady(
            CommandSourceStack source,
            ServerPlayer player
    ) {
        ClaimSelection selection =
                Bananaclaims.SELECTION_MANAGER
                        .getSelection(
                                player.getUUID()
                        );

        if (selection == null
                || !selection.hasBothPositions()) {
            return;
        }

        if (!selection.isSameDimension()) {
            source.sendFailure(
                    Component.literal(
                            "Both claim positions must be in the same dimension to preview the selection."
                    )
            );

            return;
        }

        if (!player.level()
                .dimension()
                .toString()
                .equals(selection.getPos1Dimension())) {
            source.sendFailure(
                    Component.literal(
                            "Your claim selection is in another dimension."
                    )
            );

            return;
        }

        /*
         * Stop the legacy particle preview if one is still active for this
         * player, then show the solid terrain-following Renderer v2 border.
         */
        Bananaclaims.BOUNDARY_PREVIEW_MANAGER.stop(
                player.getUUID()
        );

        boolean created =
                Bananaclaims.DISPLAY_PREVIEW_V2_MANAGER
                        .showSelectionDisplay(
                                player,
                                selection
                        );

        if (!created) {
            source.sendFailure(
                    Component.literal(
                            "Unable to create the solid selection preview."
                    )
            );

            return;
        }

        source.sendSuccess(
                () -> Component.literal(
                        "Showing the solid terrain-following selection boundary for 10 seconds."
                ),
                false
        );
    }
}


