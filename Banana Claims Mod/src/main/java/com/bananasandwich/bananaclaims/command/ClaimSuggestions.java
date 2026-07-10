package com.bananasandwich.bananaclaims.command;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.bananasandwich.bananaclaims.claim.Claim;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;

public final class ClaimSuggestions {

    public static final SuggestionProvider<CommandSourceStack> ALL_CLAIMS =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    Bananaclaims.CLAIM_MANAGER
                            .getAllClaims()
                            .stream()
                            .map(Claim::getName)
                            .filter(name -> name != null && !name.isBlank())
                            .sorted(String.CASE_INSENSITIVE_ORDER),
                    builder
            );

    public static final SuggestionProvider<CommandSourceStack> OWNED_CLAIMS =
            (context, builder) -> {
                try {
                    List<String> claimNames =
                            Bananaclaims.CLAIM_MANAGER
                                    .getClaimsForOwner(
                                            context.getSource()
                                                    .getPlayerOrException()
                                                    .getUUID()
                                    )
                                    .stream()
                                    .map(Claim::getName)
                                    .filter(name ->
                                            name != null
                                                    && !name.isBlank()
                                    )
                                    .sorted(String.CASE_INSENSITIVE_ORDER)
                                    .toList();

                    return SharedSuggestionProvider.suggest(
                            claimNames,
                            builder
                    );
                } catch (Exception exception) {
                    return builder.buildFuture();
                }
            };

    public static final SuggestionProvider<CommandSourceStack> POPUP_MODES =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    List.of(
                            "ACTIONBAR",
                            "TITLE",
                            "CHAT"
                    ),
                    builder
            );

    public static final SuggestionProvider<CommandSourceStack> SOUNDS =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    BuiltInRegistries.SOUND_EVENT
                            .keySet()
                            .stream()
                            .map(Object::toString)
                            .sorted(),
                    builder
            );

    private ClaimSuggestions() {
    }
}