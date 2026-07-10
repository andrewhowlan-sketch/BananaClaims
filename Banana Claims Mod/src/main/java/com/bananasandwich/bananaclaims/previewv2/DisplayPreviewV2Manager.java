package com.bananasandwich.bananaclaims.previewv2;

import com.bananasandwich.bananaclaims.claim.Claim;
import com.bananasandwich.bananaclaims.claim.ClaimChunk;
import com.bananasandwich.bananaclaims.selection.ClaimSelection;
import com.mojang.math.Transformation;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class DisplayPreviewV2Manager {

    private static final int TEST_DURATION_TICKS =
            10 * 20;

    private static final int RECTANGLE_SIZE =
            8;

    private static final float BORDER_HEIGHT =
            0.35F;

    private static final float BORDER_THICKNESS =
            0.35F;

    private static final double TERRAIN_OFFSET =
            0.18D;

    private static final float CORNER_MARKER_SIZE =
            1.15F;

    private static final float CORNER_MARKER_HEIGHT =
            1.15F;

    private static final float CORNER_COLUMN_THICKNESS =
            0.28F;

    private static final double CORNER_COLUMN_GAP =
            0.10D;

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

    public boolean stop(
            UUID playerUuid
    ) {
        if (playerUuid == null) {
            return false;
        }

        TestDisplaySession existing =
                sessions.remove(playerUuid);

        if (existing == null) {
            return false;
        }

        discardAll(
                existing.displays()
        );

        return true;
    }

    public boolean showClaimDisplay(
            ServerPlayer player,
            Claim claim
    ) {
        if (player == null
                || claim == null
                || claim.getChunks().isEmpty()) {
            return false;
        }

        ServerLevel level =
                player.level();

        if (!level.dimension()
                .toString()
                .equals(claim.getDimension())) {
            return false;
        }

        MinecraftServer server =
                level.getServer();

        if (server == null) {
            return false;
        }

        removeExistingSession(
                player.getUUID()
        );

        List<PerimeterEdge> perimeterEdges =
                createClaimPerimeterEdges(claim);

        if (perimeterEdges.isEmpty()) {
            return false;
        }

        List<Display.BlockDisplay> displays =
                new ArrayList<>();

        for (PerimeterEdge edge : perimeterEdges) {
            if (edge.startZ() == edge.endZ()) {
                appendContouredXEdge(
                        level,
                        displays,
                        edge.startX(),
                        edge.endX(),
                        edge.startZ()
                );
            } else {
                appendContouredZEdge(
                        level,
                        displays,
                        edge.startZ(),
                        edge.endZ(),
                        edge.startX()
                );
            }
        }

        appendCornerAnchors(
                level,
                displays,
                findTrueCorners(perimeterEdges)
        );

        if (displays.isEmpty()) {
            return false;
        }

        long currentTick =
                server.getTickCount();

        sessions.put(
                player.getUUID(),
                new TestDisplaySession(
                        List.copyOf(displays),
                        currentTick
                                + TEST_DURATION_TICKS
                )
        );

        return true;
    }

    public boolean showSelectionDisplay(
            ServerPlayer player,
            ClaimSelection selection
    ) {
        if (player == null
                || selection == null
                || !selection.hasBothPositions()
                || !selection.isSameDimension()) {
            return false;
        }

        ServerLevel level =
                player.level();

        String currentDimension =
                level.dimension()
                        .toString();

        if (!currentDimension.equals(
                selection.getPos1Dimension()
        )) {
            return false;
        }

        MinecraftServer server =
                level.getServer();

        if (server == null) {
            return false;
        }

        removeExistingSession(
                player.getUUID()
        );

        int minX =
                Math.min(
                        selection.getPos1().getX(),
                        selection.getPos2().getX()
                );

        int maxX =
                Math.max(
                        selection.getPos1().getX(),
                        selection.getPos2().getX()
                ) + 1;

        int minZ =
                Math.min(
                        selection.getPos1().getZ(),
                        selection.getPos2().getZ()
                );

        int maxZ =
                Math.max(
                        selection.getPos1().getZ(),
                        selection.getPos2().getZ()
                ) + 1;

        List<Display.BlockDisplay> displays =
                new ArrayList<>();

        appendContouredXEdge(
                level,
                displays,
                minX,
                maxX,
                minZ
        );

        appendContouredXEdge(
                level,
                displays,
                minX,
                maxX,
                maxZ
        );

        appendContouredZEdge(
                level,
                displays,
                minZ,
                maxZ,
                minX
        );

        appendContouredZEdge(
                level,
                displays,
                minZ,
                maxZ,
                maxX
        );

        appendCornerAnchors(
                level,
                displays,
                Set.of(
                        new PerimeterPoint(minX, minZ),
                        new PerimeterPoint(maxX, minZ),
                        new PerimeterPoint(maxX, maxZ),
                        new PerimeterPoint(minX, maxZ)
                )
        );

        if (displays.isEmpty()) {
            return false;
        }

        long currentTick =
                server.getTickCount();

        sessions.put(
                player.getUUID(),
                new TestDisplaySession(
                        List.copyOf(displays),
                        currentTick
                                + TEST_DURATION_TICKS
                )
        );

        return true;
    }

    public boolean showTestDisplay(
            ServerPlayer player
    ) {
        if (player == null) {
            return false;
        }

        ServerLevel level =
                player.level();

        MinecraftServer server =
                level.getServer();

        if (server == null) {
            return false;
        }

        removeExistingSession(
                player.getUUID()
        );

        Vec3 horizontalLook =
                new Vec3(
                        player.getLookAngle().x,
                        0.0D,
                        player.getLookAngle().z
                );

        if (horizontalLook.lengthSqr() < 0.0001D) {
            horizontalLook =
                    new Vec3(
                            0.0D,
                            0.0D,
                            1.0D
                    );
        } else {
            horizontalLook =
                    horizontalLook.normalize();
        }

        int centerX =
                (int) Math.floor(
                        player.getX()
                                + horizontalLook.x * 7.0D
                );

        int centerZ =
                (int) Math.floor(
                        player.getZ()
                                + horizontalLook.z * 7.0D
                );

        int halfSize =
                RECTANGLE_SIZE / 2;

        int minX =
                centerX - halfSize;

        int maxX =
                minX + RECTANGLE_SIZE;

        int minZ =
                centerZ - halfSize;

        int maxZ =
                minZ + RECTANGLE_SIZE;

        List<Display.BlockDisplay> displays =
                new ArrayList<>();

        appendContouredXEdge(
                level,
                displays,
                minX,
                maxX,
                minZ
        );

        appendContouredXEdge(
                level,
                displays,
                minX,
                maxX,
                maxZ
        );

        appendContouredZEdge(
                level,
                displays,
                minZ,
                maxZ,
                minX
        );

        appendContouredZEdge(
                level,
                displays,
                minZ,
                maxZ,
                maxX
        );

        appendCornerAnchors(
                level,
                displays,
                Set.of(
                        new PerimeterPoint(minX, minZ),
                        new PerimeterPoint(maxX, minZ),
                        new PerimeterPoint(maxX, maxZ),
                        new PerimeterPoint(minX, maxZ)
                )
        );

        if (displays.isEmpty()) {
            return false;
        }

        long currentTick =
                server.getTickCount();

        sessions.put(
                player.getUUID(),
                new TestDisplaySession(
                        List.copyOf(displays),
                        currentTick
                                + TEST_DURATION_TICKS
                )
        );

        return true;
    }

    private static void appendContouredXEdge(
            ServerLevel level,
            List<Display.BlockDisplay> displays,
            int minX,
            int maxX,
            int z
    ) {
        int runStartX =
                minX;

        int runHeight =
                terrainHeight(
                        level,
                        minX,
                        z
                );

        for (int x = minX + 1;
             x <= maxX;
             x++) {
            int height =
                    terrainHeight(
                            level,
                            x,
                            z
                    );

            boolean reachedEnd =
                    x == maxX;

            if (height == runHeight
                    && !reachedEnd) {
                continue;
            }

            int runEndX =
                    reachedEnd
                            && height == runHeight
                            ? maxX
                            : x;

            addDisplay(
                    level,
                    displays,
                    runStartX,
                    runHeight
                            + TERRAIN_OFFSET,
                    z
                            - BORDER_THICKNESS / 2.0D,
                    runEndX - runStartX,
                    BORDER_HEIGHT,
                    BORDER_THICKNESS,
                    Blocks.AMETHYST_BLOCK
                            .defaultBlockState()
            );

            runStartX = x;
            runHeight = height;
        }
    }

    private static void appendContouredZEdge(
            ServerLevel level,
            List<Display.BlockDisplay> displays,
            int minZ,
            int maxZ,
            int x
    ) {
        int runStartZ =
                minZ;

        int runHeight =
                terrainHeight(
                        level,
                        x,
                        minZ
                );

        for (int z = minZ + 1;
             z <= maxZ;
             z++) {
            int height =
                    terrainHeight(
                            level,
                            x,
                            z
                    );

            boolean reachedEnd =
                    z == maxZ;

            if (height == runHeight
                    && !reachedEnd) {
                continue;
            }

            int runEndZ =
                    reachedEnd
                            && height == runHeight
                            ? maxZ
                            : z;

            addDisplay(
                    level,
                    displays,
                    x
                            - BORDER_THICKNESS / 2.0D,
                    runHeight
                            + TERRAIN_OFFSET,
                    runStartZ,
                    BORDER_THICKNESS,
                    BORDER_HEIGHT,
                    runEndZ - runStartZ,
                    Blocks.AMETHYST_BLOCK
                            .defaultBlockState()
            );

            runStartZ = z;
            runHeight = height;
        }
    }

    private static int terrainHeight(
            ServerLevel level,
            int x,
            int z
    ) {
        int topY =
                level.getHeight(
                        Heightmap.Types.MOTION_BLOCKING,
                        x,
                        z
                ) - 1;

        int minimumY =
                level.getMinY();

        BlockPos.MutableBlockPos position =
                new BlockPos.MutableBlockPos(
                        x,
                        topY,
                        z
                );

        while (topY >= minimumY) {
            position.set(
                    x,
                    topY,
                    z
            );

            var blockState =
                    level.getBlockState(position);

            if (!blockState.is(BlockTags.LOGS)
                    && !blockState.is(BlockTags.LEAVES)) {
                return topY + 1;
            }

            topY--;
        }

        return minimumY;
    }

    private static void addDisplay(
            ServerLevel level,
            List<Display.BlockDisplay> displays,
            double x,
            double y,
            double z,
            float scaleX,
            float scaleY,
            float scaleZ,
            BlockState blockState
    ) {
        if (scaleX <= 0.0F
                || scaleY <= 0.0F
                || scaleZ <= 0.0F) {
            return;
        }

        Display.BlockDisplay display =
                new Display.BlockDisplay(
                        EntityTypes.BLOCK_DISPLAY,
                        level
                );

        display.setPos(
                x,
                y,
                z
        );

        display.setBlockState(blockState);

        display.setTransformation(
                new Transformation(
                        new Vector3f(),
                        new Quaternionf(),
                        new Vector3f(
                                scaleX,
                                scaleY,
                                scaleZ
                        ),
                        new Quaternionf()
                )
        );

        display.setViewRange(4.0F);
        display.setShadowRadius(0.0F);
        display.setShadowStrength(0.0F);
        display.setWidth(
                Math.max(
                        scaleX,
                        scaleZ
                )
        );
        display.setHeight(scaleY);
        display.setGlowingTag(true);
        display.setGlowColorOverride(0xA855F7);
        display.setNoGravity(true);

        boolean added =
                level.addFreshEntity(display);

        if (!added) {
            display.discard();
            return;
        }

        displays.add(display);
    }

    private static void appendCornerAnchors(
            ServerLevel level,
            List<Display.BlockDisplay> displays,
            Set<PerimeterPoint> corners
    ) {
        for (PerimeterPoint corner : corners) {
            int surfaceY =
                    terrainHeight(
                            level,
                            corner.x(),
                            corner.z()
                    );

            addDisplay(
                    level,
                    displays,
                    corner.x()
                            - CORNER_MARKER_SIZE / 2.0D,
                    surfaceY
                            + TERRAIN_OFFSET,
                    corner.z()
                            - CORNER_MARKER_SIZE / 2.0D,
                    CORNER_MARKER_SIZE,
                    CORNER_MARKER_HEIGHT,
                    CORNER_MARKER_SIZE,
                    Blocks.LAPIS_BLOCK
                            .defaultBlockState()
            );

            double upperColumnY =
                    surfaceY
                            + TERRAIN_OFFSET
                            + CORNER_MARKER_HEIGHT
                            + CORNER_COLUMN_GAP;

            float upperColumnHeight =
                    (float) Math.max(
                            1.0D,
                            level.getMaxY() - upperColumnY
                    );

            addDisplay(
                    level,
                    displays,
                    corner.x()
                            - CORNER_COLUMN_THICKNESS / 2.0D,
                    upperColumnY,
                    corner.z()
                            - CORNER_COLUMN_THICKNESS / 2.0D,
                    CORNER_COLUMN_THICKNESS,
                    upperColumnHeight,
                    CORNER_COLUMN_THICKNESS,
                    Blocks.LAPIS_BLOCK
                            .defaultBlockState()
            );

            double lowerColumnY =
                    level.getMinY();

            float lowerColumnHeight =
                    (float) Math.max(
                            1.0D,
                            surfaceY
                                    + TERRAIN_OFFSET
                                    - lowerColumnY
                    );

            addDisplay(
                    level,
                    displays,
                    corner.x()
                            - CORNER_COLUMN_THICKNESS / 2.0D,
                    lowerColumnY,
                    corner.z()
                            - CORNER_COLUMN_THICKNESS / 2.0D,
                    CORNER_COLUMN_THICKNESS,
                    lowerColumnHeight,
                    CORNER_COLUMN_THICKNESS,
                    Blocks.LAPIS_BLOCK
                            .defaultBlockState()
            );
        }
    }

    private static Set<PerimeterPoint> findTrueCorners(
            List<PerimeterEdge> edges
    ) {
        Map<PerimeterPoint, Integer> directionMasks =
                new HashMap<>();

        for (PerimeterEdge edge : edges) {
            int directionMask =
                    edge.startZ() == edge.endZ()
                            ? 1
                            : 2;

            directionMasks.merge(
                    new PerimeterPoint(
                            edge.startX(),
                            edge.startZ()
                    ),
                    directionMask,
                    (left, right) -> left | right
            );

            directionMasks.merge(
                    new PerimeterPoint(
                            edge.endX(),
                            edge.endZ()
                    ),
                    directionMask,
                    (left, right) -> left | right
            );
        }

        Set<PerimeterPoint> corners =
                new LinkedHashSet<>();

        for (Map.Entry<PerimeterPoint, Integer> entry
                : directionMasks.entrySet()) {
            if (entry.getValue() == 3) {
                corners.add(entry.getKey());
            }
        }

        return corners;
    }

    private static List<PerimeterEdge> createClaimPerimeterEdges(
            Claim claim
    ) {
        Set<PerimeterEdge> edges =
                new HashSet<>();

        for (ClaimChunk chunk : claim.getChunks()) {
            int minX =
                    chunk.getChunkX() * 16;

            int minZ =
                    chunk.getChunkZ() * 16;

            int maxX =
                    minX + 16;

            int maxZ =
                    minZ + 16;

            toggleEdge(
                    edges,
                    new PerimeterEdge(
                            minX,
                            minZ,
                            maxX,
                            minZ
                    )
            );

            toggleEdge(
                    edges,
                    new PerimeterEdge(
                            maxX,
                            minZ,
                            maxX,
                            maxZ
                    )
            );

            toggleEdge(
                    edges,
                    new PerimeterEdge(
                            minX,
                            maxZ,
                            maxX,
                            maxZ
                    )
            );

            toggleEdge(
                    edges,
                    new PerimeterEdge(
                            minX,
                            minZ,
                            minX,
                            maxZ
                    )
            );
        }

        return new ArrayList<>(
                new LinkedHashSet<>(edges)
        );
    }

    private static void toggleEdge(
            Set<PerimeterEdge> edges,
            PerimeterEdge edge
    ) {
        PerimeterEdge normalized =
                edge.normalized();

        if (!edges.remove(normalized)) {
            edges.add(normalized);
        }
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

            discardAll(
                    session.displays()
            );

            iterator.remove();
        }
    }

    private void removeExistingSession(
            UUID playerUuid
    ) {
        TestDisplaySession existing =
                sessions.remove(playerUuid);

        if (existing == null) {
            return;
        }

        discardAll(
                existing.displays()
        );
    }

    private static void discardAll(
            List<Display.BlockDisplay> displays
    ) {
        if (displays == null) {
            return;
        }

        for (Display.BlockDisplay display : displays) {
            if (display != null
                    && !display.isRemoved()) {
                display.discard();
            }
        }
    }

    private record PerimeterPoint(
            int x,
            int z
    ) {
    }

    private record PerimeterEdge(
            int startX,
            int startZ,
            int endX,
            int endZ
    ) {
        private PerimeterEdge normalized() {
            if (startX < endX
                    || (
                    startX == endX
                            && startZ <= endZ
            )) {
                return this;
            }

            return new PerimeterEdge(
                    endX,
                    endZ,
                    startX,
                    startZ
            );
        }
    }

    private record TestDisplaySession(
            List<Display.BlockDisplay> displays,
            long expiryTick
    ) {
        private TestDisplaySession {
            displays = List.copyOf(displays);
        }
    }
}




