package com.bananasandwich.bananaclaims.command;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.bananasandwich.bananaclaims.claim.Claim;
import com.bananasandwich.bananaclaims.claim.PopupDisplayMode;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class PopupClaimCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("popup")
                .then(Commands.argument("claim", StringArgumentType.word())
                        .then(Commands.literal("set")
                                .then(Commands.literal("mode")
                                        .then(Commands.argument("mode", StringArgumentType.word())
                                                .executes(context -> {
                                                    String claimName = StringArgumentType.getString(context, "claim");
                                                    String modeText = StringArgumentType.getString(context, "mode");

                                                    Optional<Claim> optionalClaim = findClaim(claimName);

                                                    if (optionalClaim.isEmpty()) {
                                                        context.getSource().sendFailure(Component.literal("No claim found named \"" + claimName + "\"."));
                                                        return 0;
                                                    }

                                                    PopupDisplayMode mode;

                                                    try {
                                                        mode = PopupDisplayMode.valueOf(modeText.toUpperCase());
                                                    } catch (IllegalArgumentException exception) {
                                                        context.getSource().sendFailure(Component.literal("Invalid popup mode. Use ACTIONBAR, TITLE, or CHAT."));
                                                        return 0;
                                                    }

                                                    Claim claim = optionalClaim.get();
                                                    claim.getPopupSettings().setDisplayMode(mode);
                                                    Bananaclaims.CLAIM_MANAGER.saveClaims();

                                                    context.getSource().sendSuccess(
                                                            () -> Component.literal("Set popup mode for \"" + claim.getName() + "\" to " + mode + "."),
                                                            false
                                                    );

                                                    return 1;
                                                })
                                        )
                                )

                                .then(Commands.literal("enterTitle")
                                        .then(Commands.argument("text", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    String claimName = StringArgumentType.getString(context, "claim");
                                                    String text = StringArgumentType.getString(context, "text");

                                                    Optional<Claim> optionalClaim = findClaim(claimName);

                                                    if (optionalClaim.isEmpty()) {
                                                        context.getSource().sendFailure(Component.literal("No claim found named \"" + claimName + "\"."));
                                                        return 0;
                                                    }

                                                    Claim claim = optionalClaim.get();
                                                    claim.getPopupSettings().setEnterTitle(text);
                                                    Bananaclaims.CLAIM_MANAGER.saveClaims();

                                                    context.getSource().sendSuccess(
                                                            () -> Component.literal("Set enter title for \"" + claim.getName() + "\"."),
                                                            false
                                                    );

                                                    return 1;
                                                })
                                        )
                                )

                                .then(Commands.literal("enterSubtitle")
                                        .then(Commands.argument("text", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    String claimName = StringArgumentType.getString(context, "claim");
                                                    String text = StringArgumentType.getString(context, "text");

                                                    Optional<Claim> optionalClaim = findClaim(claimName);

                                                    if (optionalClaim.isEmpty()) {
                                                        context.getSource().sendFailure(Component.literal("No claim found named \"" + claimName + "\"."));
                                                        return 0;
                                                    }

                                                    Claim claim = optionalClaim.get();
                                                    claim.getPopupSettings().setEnterSubtitle(text);
                                                    Bananaclaims.CLAIM_MANAGER.saveClaims();

                                                    context.getSource().sendSuccess(
                                                            () -> Component.literal("Set enter subtitle for \"" + claim.getName() + "\"."),
                                                            false
                                                    );

                                                    return 1;
                                                })
                                        )
                                )
                        )
                );
    }

    private static Optional<Claim> findClaim(String claimName) {
        return Bananaclaims.CLAIM_MANAGER.getAllClaims().stream()
                .filter(claim -> claim.getName().equalsIgnoreCase(claimName))
                .findFirst();
    }
}