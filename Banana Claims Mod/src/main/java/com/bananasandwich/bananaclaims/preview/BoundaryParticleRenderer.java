package com.bananasandwich.bananaclaims.preview;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;

public final class BoundaryParticleRenderer {

    private static final DustParticleOptions LINE_PARTICLE =
            new DustParticleOptions(
                    PreviewSettings.COLOR_RGB,
                    PreviewSettings.PARTICLE_SCALE
            );

    private static final DustParticleOptions CORNER_PARTICLE =
            new DustParticleOptions(
                    PreviewSettings.COLOR_RGB,
                    PreviewSettings.CORNER_PARTICLE_SCALE
            );

    private static final DustParticleOptions GUIDE_PARTICLE =
            new DustParticleOptions(
                    PreviewSettings.COLOR_RGB,
                    PreviewSettings.GUIDE_PARTICLE_SCALE
            );

    private static final DustParticleOptions SHADE_PARTICLE =
            new DustParticleOptions(
                    PreviewSettings.COLOR_RGB,
                    PreviewSettings.SHADE_PARTICLE_SCALE
            );

    private BoundaryParticleRenderer() {
    }

    public static void render(
            ServerPlayer player,
            BoundaryPreview preview
    ) {
        if (player == null || preview == null) {
            return;
        }

        ServerLevel level = player.level();
        String currentDimension =
                level.dimension().toString();

        if (!currentDimension.equals(preview.dimension())) {
            return;
        }

        /*
         * Draw the cheapest and most important layers first so the boundary
         * stays obvious even when a large preview reaches its shading budget.
         */
        renderGuideBands(
                level,
                player,
                preview
        );

        renderLocalWallShading(
                level,
                player,
                preview
        );

        Set<PreviewPoint> corners = new HashSet<>();

        for (BoundaryLine line : preview.lines()) {
            renderLine(
                    level,
                    player,
                    line,
                    PreviewSettings.calculateLineSpacing(line),
                    LINE_PARTICLE,
                    PreviewSettings.LINE_PARTICLE_COUNT,
                    PreviewSettings.LINE_PARTICLE_SPREAD
            );

            corners.add(
                    new PreviewPoint(
                            line.startX(),
                            line.startY(),
                            line.startZ()
                    )
            );

            corners.add(
                    new PreviewPoint(
                            line.endX(),
                            line.endY(),
                            line.endZ()
                    )
            );
        }

        for (PreviewPoint corner : corners) {
            renderCorner(
                    level,
                    player,
                    corner
            );
        }
    }

    private static void renderGuideBands(
            ServerLevel level,
            ServerPlayer player,
            BoundaryPreview preview
    ) {
        for (BoundarySurface surface : preview.surfaces()) {
            if (!surface.isVertical()) {
                continue;
            }

            double minY = surface.originY();
            double maxY =
                    surface.originY()
                            + surface.axisVY();

            if (maxY < minY) {
                double temporary = minY;
                minY = maxY;
                maxY = temporary;
            }

            int firstBand =
                    (int) Math.ceil(
                            minY
                                    / PreviewSettings.GUIDE_BAND_INTERVAL
                    ) * PreviewSettings.GUIDE_BAND_INTERVAL;

            for (double y = firstBand;
                 y <= maxY;
                 y += PreviewSettings.GUIDE_BAND_INTERVAL) {
                BoundaryLine guideLine =
                        horizontalLineAtY(
                                surface,
                                y
                        );

                renderLine(
                        level,
                        player,
                        guideLine,
                        PreviewSettings.GUIDE_LINE_SPACING,
                        GUIDE_PARTICLE,
                        PreviewSettings.GUIDE_PARTICLE_COUNT,
                        PreviewSettings.GUIDE_PARTICLE_SPREAD
                );
            }
        }
    }

    private static void renderLocalWallShading(
            ServerLevel level,
            ServerPlayer player,
            BoundaryPreview preview
    ) {
        int remainingBudget =
                PreviewSettings.MAX_LOCAL_SHADE_POINTS_PER_RENDER;

        double playerMinY =
                player.getY()
                        - PreviewSettings.LOCAL_SHADE_BELOW_PLAYER;

        double playerMaxY =
                player.getY()
                        + PreviewSettings.LOCAL_SHADE_ABOVE_PLAYER;

        for (BoundarySurface surface : preview.surfaces()) {
            if (!surface.isVertical()
                    || remainingBudget <= 0) {
                continue;
            }

            remainingBudget = renderLocalSurface(
                    level,
                    player,
                    surface,
                    playerMinY,
                    playerMaxY,
                    remainingBudget
            );
        }
    }

    private static int renderLocalSurface(
            ServerLevel level,
            ServerPlayer player,
            BoundarySurface surface,
            double playerMinY,
            double playerMaxY,
            int remainingBudget
    ) {
        double surfaceStartY =
                surface.originY();

        double surfaceEndY =
                surface.originY()
                        + surface.axisVY();

        double surfaceMinY =
                Math.min(
                        surfaceStartY,
                        surfaceEndY
                );

        double surfaceMaxY =
                Math.max(
                        surfaceStartY,
                        surfaceEndY
                );

        double minY = Math.max(
                surfaceMinY,
                playerMinY
        );

        double maxY = Math.min(
                surfaceMaxY,
                playerMaxY
        );

        if (maxY <= minY) {
            return remainingBudget;
        }

        double width = surface.width();
        double visibleHeight = maxY - minY;

        int stepsU = Math.max(
                1,
                (int) Math.ceil(
                        width
                                / PreviewSettings.LOCAL_SHADE_SPACING
                )
        );

        int stepsY = Math.max(
                1,
                (int) Math.ceil(
                        visibleHeight
                                / PreviewSettings.LOCAL_SHADE_SPACING
                )
        );

        for (int u = 1;
             u < stepsU && remainingBudget > 0;
             u++) {
            double progressU =
                    (double) u / stepsU;

            double x =
                    surface.originX()
                            + surface.axisUX() * progressU;

            double z =
                    surface.originZ()
                            + surface.axisUZ() * progressU;

            for (int yStep = 1;
                 yStep < stepsY && remainingBudget > 0;
                 yStep++) {
                double progressY =
                        (double) yStep / stepsY;

                double y =
                        minY
                                + visibleHeight * progressY;

                if (!isWithinRenderRadius(
                        player,
                        x,
                        z
                )) {
                    continue;
                }

                sendLongDistanceParticles(
                        level,
                        player,
                        SHADE_PARTICLE,
                        x,
                        y,
                        z,
                        PreviewSettings.LOCAL_SHADE_PARTICLE_COUNT,
                        PreviewSettings.LOCAL_SHADE_PARTICLE_SPREAD
                );

                remainingBudget--;
            }
        }

        return remainingBudget;
    }

    private static BoundaryLine horizontalLineAtY(
            BoundarySurface surface,
            double y
    ) {
        return new BoundaryLine(
                surface.originX(),
                y,
                surface.originZ(),
                surface.originX()
                        + surface.axisUX(),
                y,
                surface.originZ()
                        + surface.axisUZ()
        );
    }

    private static void renderLine(
            ServerLevel level,
            ServerPlayer player,
            BoundaryLine line,
            double spacing,
            DustParticleOptions particle,
            int particleCount,
            double particleSpread
    ) {
        double length = line.length();

        int steps = Math.max(
                1,
                (int) Math.ceil(length / spacing)
        );

        for (int step = 0;
             step <= steps;
             step++) {
            double progress =
                    (double) step / steps;

            double x = lerp(
                    line.startX(),
                    line.endX(),
                    progress
            );

            double y = lerp(
                    line.startY(),
                    line.endY(),
                    progress
            );

            double z = lerp(
                    line.startZ(),
                    line.endZ(),
                    progress
            );

            if (!isWithinRenderRadius(
                    player,
                    x,
                    z
            )) {
                continue;
            }

            sendLongDistanceParticles(
                    level,
                    player,
                    particle,
                    x,
                    y,
                    z,
                    particleCount,
                    particleSpread
            );
        }
    }

    private static void renderCorner(
            ServerLevel level,
            ServerPlayer player,
            PreviewPoint corner
    ) {
        if (!isWithinRenderRadius(
                player,
                corner.x(),
                corner.z()
        )) {
            return;
        }

        sendLongDistanceParticles(
                level,
                player,
                CORNER_PARTICLE,
                corner.x(),
                corner.y(),
                corner.z(),
                PreviewSettings.CORNER_PARTICLE_COUNT,
                PreviewSettings.CORNER_PARTICLE_SPREAD
        );
    }

    private static boolean isWithinRenderRadius(
            ServerPlayer player,
            double x,
            double z
    ) {
        double deltaX =
                x - player.getX();

        double deltaZ =
                z - player.getZ();

        return deltaX * deltaX
                + deltaZ * deltaZ
                <= PreviewSettings.RENDER_RADIUS_SQUARED;
    }

    private static void sendLongDistanceParticles(
            ServerLevel level,
            ServerPlayer player,
            DustParticleOptions particle,
            double x,
            double y,
            double z,
            int count,
            double spread
    ) {
        level.sendParticles(
                player,
                particle,
                true,
                true,
                x,
                y,
                z,
                count,
                spread,
                spread,
                spread,
                0.0D
        );
    }

    private static double lerp(
            double start,
            double end,
            double progress
    ) {
        return start
                + (end - start) * progress;
    }

    private record PreviewPoint(
            double x,
            double y,
            double z
    ) {
    }
}



