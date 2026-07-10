package com.bananasandwich.bananaclaims.preview;

public final class PreviewSettings {

    public static final int COLOR_RGB = 0xA855F7;

    public static final float PARTICLE_SCALE = 1.70F;
    public static final float CORNER_PARTICLE_SCALE = 2.55F;
    public static final float SHADE_PARTICLE_SCALE = 0.90F;
    public static final float GUIDE_PARTICLE_SCALE = 1.35F;

    public static final int DURATION_TICKS = 20 * 20;

    /*
     * Rendering twice per second is enough because dust particles linger.
     * This cuts repeated particle packets in half compared with 5 ticks.
     */
    public static final int REFRESH_INTERVAL_TICKS = 10;

    public static final int LINE_PARTICLE_COUNT = 2;
    public static final double LINE_PARTICLE_SPREAD = 0.045D;
    public static final double HORIZONTAL_LINE_SPACING = 0.40D;
    public static final double VERTICAL_LINE_SPACING = 1.25D;

    public static final int CORNER_PARTICLE_COUNT = 6;
    public static final double CORNER_PARTICLE_SPREAD = 0.11D;

    /*
     * Bright horizontal guide bands communicate that the boundary continues
     * through the full world height without filling every wall with particles.
     */
    public static final int GUIDE_PARTICLE_COUNT = 2;
    public static final double GUIDE_PARTICLE_SPREAD = 0.055D;
    public static final double GUIDE_LINE_SPACING = 0.65D;
    public static final int GUIDE_BAND_INTERVAL = 16;

    /*
     * Dense wall shading follows the player vertically. The complete corner
     * pillars and guide bands remain full height.
     */
    public static final int LOCAL_SHADE_BELOW_PLAYER = 24;
    public static final int LOCAL_SHADE_ABOVE_PLAYER = 48;
    public static final int LOCAL_SHADE_PARTICLE_COUNT = 1;
    public static final double LOCAL_SHADE_PARTICLE_SPREAD = 0.075D;
    public static final double LOCAL_SHADE_SPACING = 1.15D;

    /*
     * Particle packets outside this horizontal radius are skipped. The forced
     * particle flag is still used, so particles within this range remain
     * visible farther than ordinary particles.
     */
    public static final double RENDER_RADIUS = 128.0D;
    public static final double RENDER_RADIUS_SQUARED =
            RENDER_RADIUS * RENDER_RADIUS;

    /*
     * Shading is the first visual layer reduced when a very large claim would
     * exceed the client-friendly budget. Edges and guide bands are preserved.
     */
    public static final int MAX_LOCAL_SHADE_POINTS_PER_RENDER = 1800;

    private PreviewSettings() {
    }

    public static double calculateLineSpacing(
            BoundaryLine line
    ) {
        return isVertical(line)
                ? VERTICAL_LINE_SPACING
                : HORIZONTAL_LINE_SPACING;
    }

    public static boolean isVertical(
            BoundaryLine line
    ) {
        return Math.abs(
                line.endY() - line.startY()
        ) > 0.0001D
                && Math.abs(
                line.endX() - line.startX()
        ) < 0.0001D
                && Math.abs(
                line.endZ() - line.startZ()
        ) < 0.0001D;
    }
}

