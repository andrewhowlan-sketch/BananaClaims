package com.bananasandwich.bananaclaims.book;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.List;

/** Opens a server-generated written book without changing server inventory. */
public final class BookPacketSender {

    private BookPacketSender() {
    }

    public static void open(
            ServerPlayer player,
            String title,
            List<Component> pages
    ) {
        if (player == null || pages == null || pages.isEmpty()) {
            return;
        }

        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        List<Filterable<Component>> filteredPages = pages.stream()
                .map(Filterable::passThrough)
                .toList();

        book.set(
                DataComponents.WRITTEN_BOOK_CONTENT,
                new WrittenBookContent(
                        Filterable.passThrough(truncateTitle(title)),
                        "Banana Claims",
                        0,
                        filteredPages,
                        true
                )
        );

        int selectedSlot = player.getInventory().getSelectedSlot();
        ItemStack original = player.getInventory().getSelectedItem().copy();

        // Packet ordering ensures the client sees a written book in the hand
        // when the open packet is handled, then immediately restores its real
        // inventory view. The server inventory is never mutated.
        player.connection.send(
                new ClientboundSetPlayerInventoryPacket(
                        selectedSlot,
                        book
                )
        );
        player.connection.send(
                new ClientboundOpenBookPacket(
                        InteractionHand.MAIN_HAND
                )
        );
        player.connection.send(
                new ClientboundSetPlayerInventoryPacket(
                        selectedSlot,
                        original
                )
        );
    }

    private static String truncateTitle(String title) {
        String normalized = title == null ? "Banana Claims" : title;
        return normalized.length() <= 32
                ? normalized
                : normalized.substring(0, 32);
    }
}
