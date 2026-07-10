package com.bananasandwich.bananaclaims.preview;

public record BoundaryLine(
        double startX,
        double startY,
        double startZ,
        double endX,
        double endY,
        double endZ
) {
    public double length() {
        double deltaX = endX - startX;
        double deltaY = endY - startY;
        double deltaZ = endZ - startZ;

        return Math.sqrt(
                deltaX * deltaX
                        + deltaY * deltaY
                        + deltaZ * deltaZ
        );
    }
}
