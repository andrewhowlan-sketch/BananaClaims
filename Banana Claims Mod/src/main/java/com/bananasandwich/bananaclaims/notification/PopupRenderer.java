package com.bananasandwich.bananaclaims.notification;

import com.bananasandwich.bananaclaims.claim.Claim;
import com.bananasandwich.bananaclaims.claim.ClaimPopupSettings;
import com.bananasandwich.bananaclaims.claim.PopupDisplayMode;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;

public class PopupRenderer {

    public static void showEnter(ServerPlayer player, Claim claim) {
        ClaimPopupSettings settings = claim.getPopupSettings();

        String title = settings.getEnterTitle();
        String subtitle = settings.getEnterSubtitle();

        if (title == null || title.isBlank()) {
            title = claim.getName();
        }

        if (subtitle == null || subtitle.isBlank()) {
            subtitle = "Now Entering";
        }

        render(player, settings.getDisplayMode(), title, subtitle);
    }

    public static void showLeave(ServerPlayer player, Claim claim) {
        ClaimPopupSettings settings = claim.getPopupSettings();

        String title = settings.getLeaveTitle();
        String subtitle = settings.getLeaveSubtitle();

        if (title == null || title.isBlank()) {
            title = claim.getName();
        }

        if (subtitle == null || subtitle.isBlank()) {
            subtitle = "Now Leaving";
        }

        render(player, settings.getDisplayMode(), title, subtitle);
    }

    private static void render(ServerPlayer player, PopupDisplayMode displayMode, String title, String subtitle) {
        PopupDisplayMode mode = displayMode == null ? PopupDisplayMode.ACTIONBAR : displayMode;

        switch (mode) {
            case TITLE -> renderTitle(player, title, subtitle);
            case CHAT -> renderChat(player, title, subtitle);
            case ACTIONBAR -> renderActionBar(player, title, subtitle);
        }
    }

    private static void renderActionBar(ServerPlayer player, String title, String subtitle) {
        player.sendSystemMessage(
                Component.literal(subtitle + ": " + title),
                true
        );
    }

    private static void renderChat(ServerPlayer player, String title, String subtitle) {
        player.sendSystemMessage(
                Component.literal(subtitle + ": " + title)
        );
    }

    private static void renderTitle(ServerPlayer player, String title, String subtitle) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 50, 10));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(subtitle)));
    }
}