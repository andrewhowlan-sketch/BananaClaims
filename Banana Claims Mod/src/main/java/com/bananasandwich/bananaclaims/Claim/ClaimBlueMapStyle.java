package com.bananasandwich.bananaclaims.claim;

import java.util.Locale;

/**
 * Per-claim BlueMap presentation settings.
 *
 * <p>Values are stored with the claim so commands and the Book GUI share the
 * same model. Missing or invalid values are repaired to release-safe defaults
 * when claims are loaded.</p>
 */
public final class ClaimBlueMapStyle {

    public static final String DEFAULT_LINE_COLOR = "#2378FF";
    public static final String DEFAULT_FILL_COLOR = "#2378FF";
    public static final float DEFAULT_LINE_OPACITY = 1.0F;
    public static final float DEFAULT_FILL_OPACITY = 0.22F;
    public static final int DEFAULT_LINE_WIDTH = 2;

    private String lineColor = DEFAULT_LINE_COLOR;
    private String fillColor = DEFAULT_FILL_COLOR;
    private float lineOpacity = DEFAULT_LINE_OPACITY;
    private float fillOpacity = DEFAULT_FILL_OPACITY;
    private int lineWidth = DEFAULT_LINE_WIDTH;

    public ClaimBlueMapStyle() {
    }

    public ClaimBlueMapStyle copy() {
        ClaimBlueMapStyle copy = new ClaimBlueMapStyle();
        copy.lineColor = lineColor;
        copy.fillColor = fillColor;
        copy.lineOpacity = lineOpacity;
        copy.fillOpacity = fillOpacity;
        copy.lineWidth = lineWidth;
        return copy;
    }

    public boolean sanitize() {
        boolean changed = false;

        String normalizedLine = normalizeHex(lineColor, DEFAULT_LINE_COLOR);
        changed |= !normalizedLine.equals(lineColor);
        lineColor = normalizedLine;

        String normalizedFill = normalizeHex(fillColor, DEFAULT_FILL_COLOR);
        changed |= !normalizedFill.equals(fillColor);
        fillColor = normalizedFill;

        float normalizedLineOpacity = clampOpacity(lineOpacity, DEFAULT_LINE_OPACITY);
        changed |= Float.compare(normalizedLineOpacity, lineOpacity) != 0;
        lineOpacity = normalizedLineOpacity;

        float normalizedFillOpacity = clampOpacity(fillOpacity, DEFAULT_FILL_OPACITY);
        changed |= Float.compare(normalizedFillOpacity, fillOpacity) != 0;
        fillOpacity = normalizedFillOpacity;

        int normalizedWidth = Math.max(1, Math.min(10, lineWidth));
        changed |= normalizedWidth != lineWidth;
        lineWidth = normalizedWidth;

        return changed;
    }

    public void reset() {
        lineColor = DEFAULT_LINE_COLOR;
        fillColor = DEFAULT_FILL_COLOR;
        lineOpacity = DEFAULT_LINE_OPACITY;
        fillOpacity = DEFAULT_FILL_OPACITY;
        lineWidth = DEFAULT_LINE_WIDTH;
    }

    public String getLineColor() {
        return lineColor;
    }

    public boolean setLineColor(String lineColor) {
        String normalized = normalizeHex(lineColor, null);
        if (normalized == null) {
            return false;
        }
        this.lineColor = normalized;
        return true;
    }

    public String getFillColor() {
        return fillColor;
    }

    public boolean setFillColor(String fillColor) {
        String normalized = normalizeHex(fillColor, null);
        if (normalized == null) {
            return false;
        }
        this.fillColor = normalized;
        return true;
    }

    public float getLineOpacity() {
        return lineOpacity;
    }

    public boolean setLineOpacity(float lineOpacity) {
        if (!Float.isFinite(lineOpacity) || lineOpacity < 0.0F || lineOpacity > 1.0F) {
            return false;
        }
        this.lineOpacity = lineOpacity;
        return true;
    }

    public float getFillOpacity() {
        return fillOpacity;
    }

    public boolean setFillOpacity(float fillOpacity) {
        if (!Float.isFinite(fillOpacity) || fillOpacity < 0.0F || fillOpacity > 1.0F) {
            return false;
        }
        this.fillOpacity = fillOpacity;
        return true;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public boolean setLineWidth(int lineWidth) {
        if (lineWidth < 1 || lineWidth > 10) {
            return false;
        }
        this.lineWidth = lineWidth;
        return true;
    }

    public int getLineRgb() {
        return Integer.parseInt(lineColor.substring(1), 16);
    }

    public int getFillRgb() {
        return Integer.parseInt(fillColor.substring(1), 16);
    }

    public static boolean isValidHex(String value) {
        return normalizeHex(value, null) != null;
    }

    private static float clampOpacity(float value, float fallback) {
        if (!Float.isFinite(value)) {
            return fallback;
        }
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static String normalizeHex(String value, String fallback) {
        if (value == null) {
            return fallback;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        } else if (normalized.startsWith("0X")) {
            normalized = normalized.substring(2);
        }

        if (normalized.length() != 6) {
            return fallback;
        }

        for (int index = 0; index < normalized.length(); index++) {
            char character = normalized.charAt(index);
            boolean hexadecimal = character >= '0' && character <= '9'
                    || character >= 'A' && character <= 'F';
            if (!hexadecimal) {
                return fallback;
            }
        }

        return "#" + normalized;
    }
}
