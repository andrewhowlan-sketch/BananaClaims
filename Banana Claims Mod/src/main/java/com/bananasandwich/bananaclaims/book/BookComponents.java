package com.bananasandwich.bananaclaims.book;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class BookComponents {

    private BookComponents() {
    }

    public static MutableComponent page() {
        return Component.empty();
    }

    public static Component title(String text) {
        return Component.literal(text)
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
    }

    public static Component section(String text) {
        return Component.literal(text)
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
    }

    public static Component label(String text) {
        return Component.literal(text)
                .withStyle(ChatFormatting.DARK_GRAY);
    }

    public static Component value(String text) {
        return Component.literal(text == null ? "" : text)
                .withStyle(ChatFormatting.BLACK);
    }

    public static Component status(boolean enabled) {
        return Component.literal(enabled ? "ON" : "OFF")
                .withStyle(enabled ? ChatFormatting.DARK_GREEN : ChatFormatting.RED);
    }

    public static Component action(
            String label,
            String command,
            ChatFormatting color
    ) {
        return Component.literal(label)
                .withStyle(style -> style
                        .withColor(color)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent.RunCommand(command)));
    }

    public static Component suggest(
            String label,
            String command,
            ChatFormatting color
    ) {
        return Component.literal(label)
                .withStyle(style -> style
                        .withColor(color)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent.SuggestCommand(command)));
    }

    public static Component pageLink(
            String label,
            int page,
            ChatFormatting color
    ) {
        return Component.literal(label)
                .withStyle(style -> style
                        .withColor(color)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent.ChangePage(page)));
    }

    public static MutableComponent line(MutableComponent page, Component content) {
        return page.append(content).append("\n");
    }

    public static MutableComponent blank(MutableComponent page) {
        return page.append("\n");
    }
}
