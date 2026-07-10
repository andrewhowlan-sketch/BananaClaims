package com.bananasandwich.bananaclaims.command;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.bananasandwich.bananaclaims.selection.ClaimSelection;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class PreviewV2Command {

    private PreviewV2Command() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("previewv2")
                .then(
                        Commands.literal("test")
                                .executes(context ->
                                        runDisplayTest(
                                                context.getSource()
                                        )
                                )
                )
                .then(
                        Commands.literal("selection")
                                .executes(context ->
                                        runSelectionPreview(
                                                context.getSource()
                                        )
                                )
                );
    }

    private static int runSelectionPreview(
            CommandSourceStack source
    ) throws CommandSyntaxException {
        ServerPlayer player =
                source.getPlayerOrException();

        ClaimSelection selection =
                Bananaclaims.SELECTION_MANAGER
                        .getSelection(
                                player.getUUID()
                        );

        if (selection == null
                || !selection.hasBothPositions()) {
            source.sendFailure(
                    Component.literal(
                            "Set both claim positions before previewing Renderer v2."
                    )
            );

            return 0;
        }

        if (!selection.isSameDimension()) {
            source.sendFailure(
                    Component.literal(
                            "Both claim positions must be in the same dimension."
                    )
            );

            return 0;
        }

        if (!player.level()
                .dimension()
                .toString()
                .equals(selection.getPos1Dimension())) {
            source.sendFailure(
                    Component.literal(
                            "Your selection is in another dimension."
                    )
            );

            return 0;
        }

        boolean created =
                Bananaclaims.DISPLAY_PREVIEW_V2_MANAGER
                        .showSelectionDisplay(
                                player,
                                selection
                        );

        if (!created) {
            source.sendFailure(
                    Component.literal(
                            "Unable to create the Renderer v2 selection preview."
                    )
            );

            return 0;
        }

        source.sendSuccess(
                () -> Component.literal(
                        "Showing the terrain-following Renderer v2 selection border for 10 seconds."
                ),
                false
        );

        return 1;
    }

    private static int runDisplayTest(
            CommandSourceStack source
    ) throws CommandSyntaxException {
        ServerPlayer player =
                source.getPlayerOrException();

        boolean created =
                Bananaclaims.DISPLAY_PREVIEW_V2_MANAGER
                        .showTestDisplay(player);

        if (!created) {
            source.sendFailure(
                    Component.literal(
                            "Unable to create the Preview Engine v2 test display."
                    )
            );

            return 0;
        }

        source.sendSuccess(
                () -> Component.literal(
                        "Preview Engine v2 test: showing one purple glass display for 10 seconds."
                ),
                false
        );

        return 1;
    }
}


