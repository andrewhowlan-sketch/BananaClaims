package com.bananasandwich.bananaclaims.notification;

import com.bananasandwich.bananaclaims.claim.Claim;
import com.bananasandwich.bananaclaims.claim.ClaimPopupSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class PopupRenderer {

    public static void showEnter(ServerPlayer player, Claim claim) {
        ClaimPopupSettings settings = claim.getPopupSettings();

        String message = settings.getEnterTitle();

        if (message == null || message.isBlank()) {
            message = "Entering " + claim.getName();
        }

        player.sendSystemMessage(Component.literal(message), true);
    }

    public static void showLeave(ServerPlayer player, Claim claim) {
        ClaimPopupSettings settings = claim.getPopupSettings();

        String message = settings.getLeaveTitle();

        if (message == null || message.isBlank()) {
            message = "Leaving " + claim.getName();
        }

        player.sendSystemMessage(Component.literal(message), true);
    }
}