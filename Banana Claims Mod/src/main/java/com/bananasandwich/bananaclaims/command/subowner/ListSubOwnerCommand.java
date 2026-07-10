package com.bananasandwich.bananaclaims.command.subowner;

import com.bananasandwich.bananaclaims.claim.Claim;
import com.bananasandwich.bananaclaims.claim.ClaimSubOwner;
import com.bananasandwich.bananaclaims.command.ClaimResolver;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;

public final class ListSubOwnerCommand {

    private ListSubOwnerCommand() {
    }

    public static int listCurrentClaim(
            CommandSourceStack source
    ) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<Claim> optionalClaim = ClaimResolver.findAtPlayer(player);

        if (optionalClaim.isEmpty()) {
            source.sendFailure(Component.literal("There is no claim here."));
            return 0;
        }

        return sendSubOwnerList(source, optionalClaim.get());
    }

    public static int listNamedClaim(
            CommandSourceStack source,
            String claimName
    ) {
        Optional<Claim> optionalClaim = ClaimResolver.findByName(claimName);

        if (optionalClaim.isEmpty()) {
            source.sendFailure(Component.literal(
                    "No claim found named \"" + claimName + "\"."
            ));
            return 0;
        }

        return sendSubOwnerList(source, optionalClaim.get());
    }

    private static int sendSubOwnerList(
            CommandSourceStack source,
            Claim claim
    ) {
        List<String> names = claim.getSubOwners()
                .stream()
                .map(ClaimSubOwner::getName)
                .filter(name -> name != null && !name.isBlank())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        String list = names.isEmpty()
                ? "None"
                : "\n- " + String.join("\n- ", names);

        source.sendSuccess(
                () -> Component.literal(
                        "Subowners of claim \""
                                + claim.getName()
                                + "\" ("
                                + names.size()
                                + "): "
                                + list
                ),
                false
        );

        return 1;
    }
}
