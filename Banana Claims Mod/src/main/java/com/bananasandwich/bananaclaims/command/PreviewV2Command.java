package com.bananasandwich.bananaclaims.command;

import com.bananasandwich.bananaclaims.Bananaclaims;
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
                );
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

