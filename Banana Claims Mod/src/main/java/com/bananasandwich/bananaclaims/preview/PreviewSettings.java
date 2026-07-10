package com.bananasandwich.bananaclaims.preview;

public final class PreviewSettings {

    public static final int COLOR_RGB = 0xA855F7;

    public static final float PARTICLE_SCALE = 1.70F;
    public static final float CORNER_PARTICLE_SCALE = 2.55F;
    public static final float SHADE_PARTICLE_SCALE = 0.85F;

    public static final int DURATION_TICKS = 20 * 20;
    public static final int REFRESH_INTERVAL_TICKS = 5;

    public static final int LINE_PARTICLE_COUNT = 3;
    public static final double LINE_PARTICLE_SPREAD = 0.045D;
    public static final double LINE_SPACING = 0.40D;

    public static final int CORNER_PARTICLE_COUNT = 7;
    public static final double CORNER_PARTICLE_SPREAD = 0.11D;

    public static final int SHADE_PARTICLE_COUNT = 1;
    public static final double SHADE_PARTICLE_SPREAD = 0.025D;
    public static final double SHADE_SPACING = 2.25D;

    /*
     * Keeps shaded previews visible without allowing very large claims to
     * generate an excessive number of particles per refresh.
     */
    public static final int MAX_SHADE_POINTS_PER_RENDER = 1400;

    public static final double DEFAULT_SELECTION_HEIGHT = 24.0D;

    private PreviewSettings() {
    }

    public static double calculateLineSpacing(
            double totalLineLength
    ) {
        return LINE_SPACING;
    }

    public static double calculateShadeSpacing(
            double totalSurfaceArea
    ) {
        if (totalSurfaceArea <= 0.0D) {
            return SHADE_SPACING;
        }

        double estimatedPoints =
                totalSurfaceArea
                        / (SHADE_SPACING * SHADE_SPACING);

        if (estimatedPoints <= MAX_SHADE_POINTS_PER_RENDER) {
            return SHADE_SPACING;
        }

        return Math.sqrt(
                totalSurfaceArea
                        / MAX_SHADE_POINTS_PER_RENDER
        );
    }
}