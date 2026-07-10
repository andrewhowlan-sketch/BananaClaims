package com.bananasandwich.bananaclaims.previewv2;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class DisplayPreviewV2Manager {

    private static final int TEST_DURATION_TICKS =
            10 * 20;

    private final Map<UUID, TestDisplaySession> sessions =
            new HashMap<>();

    private boolean registered;

    public void register() {
        if (registered) {
            return;
        }

        registered = true;

        ServerTickEvents.END_SERVER_TICK.register(
                this::tick
        );
    }

    public boolean showTestDisplay(
            ServerPlayer player
    ) {
        if (player == null) {
            return false;
        }

        MinecraftServer server =
                player.level().getServer();

        if (server == null) {
            return false;
        }

        removeExistingSession(
                server,
                player.getUUID()
        );

        Vec3 lookDirection =
                player.getLookAngle()
                        .normalize();

        double x =
                player.getX()
                        + lookDirection.x * 3.0D;

        double y =
                player.getEyeY()
                        - 0.5D
                        + lookDirection.y * 3.0D;

        double z =
                player.getZ()
                        + lookDirection.z * 3.0D;

        String tag =
                "bananaclaims_previewv2_"
                        + player.getUUID()
                        .toString()
                        .replace("-", "");

        String summonCommand =
                String.format(
                        Locale.ROOT,
                        "summon minecraft:block_display %.4f %.4f %.4f "
                                + "{Tags:[\"%s\"],"
                                + "block_state:{Name:\"minecraft:purple_stained_glass\"},"
                                + "transformation:{"
                                + "translation:[0.0f,0.0f,0.0f],"
                                + "left_rotation:[0.0f,0.0f,0.0f,1.0f],"
                                + "scale:[1.0f,1.0f,1.0f],"
                                + "right_rotation:[0.0f,0.0f,0.0f,1.0f]"
                                + "},"
                                + "glowing:1b,"
                                + "glow_color_override:11032055,"
                                + "view_range:4.0f,"
                                + "shadow_radius:0.0f,"
                                + "shadow_strength:0.0f}",
                        x,
                        y,
                        z,
                        tag
                );

        server.getCommands()
                .performPrefixedCommand(
                        server.createCommandSourceStack(),
                        summonCommand
                );

        long currentTick =
                server.getTickCount();

        sessions.put(
                player.getUUID(),
                new TestDisplaySession(
                        tag,
                        currentTick
                                + TEST_DURATION_TICKS
                )
        );

        return true;
    }

    private void tick(
            MinecraftServer server
    ) {
        if (sessions.isEmpty()) {
            return;
        }

        long currentTick =
                server.getTickCount();

        Iterator<Map.Entry<UUID, TestDisplaySession>> iterator =
                sessions.entrySet()
                        .iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, TestDisplaySession> entry =
                    iterator.next();

            TestDisplaySession session =
                    entry.getValue();

            if (currentTick
                    < session.expiryTick()) {
                continue;
            }

            removeTaggedDisplay(
                    server,
                    session.tag()
            );

            iterator.remove();
        }
    }

    private void removeExistingSession(
            MinecraftServer server,
            UUID playerUuid
    ) {
        TestDisplaySession existing =
                sessions.remove(playerUuid);

        if (existing == null) {
            return;
        }

        removeTaggedDisplay(
                server,
                existing.tag()
        );
    }

    private static void removeTaggedDisplay(
            MinecraftServer server,
            String tag
    ) {
        server.getCommands()
                .performPrefixedCommand(
                        server.createCommandSourceStack(),
                        "kill @e[type=minecraft:block_display,tag="
                                + tag
                                + "]"
                );
    }

    private record TestDisplaySession(
            String tag,
            long expiryTick
    ) {
    }
}
