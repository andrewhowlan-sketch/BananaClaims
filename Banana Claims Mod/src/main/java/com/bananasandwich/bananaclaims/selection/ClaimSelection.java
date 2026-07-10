package com.bananasandwich.bananaclaims.selection;

import net.minecraft.core.BlockPos;

public class ClaimSelection {

    private BlockPos pos1;
    private BlockPos pos2;

    private String pos1Dimension;
    private String pos2Dimension;

    public BlockPos getPos1() {
        return pos1;
    }

    public void setPos1(BlockPos pos1) {
        this.pos1 = pos1;
    }

    public BlockPos getPos2() {
        return pos2;
    }

    public void setPos2(BlockPos pos2) {
        this.pos2 = pos2;
    }

    public String getPos1Dimension() {
        return pos1Dimension;
    }

    public void setPos1Dimension(String pos1Dimension) {
        this.pos1Dimension = pos1Dimension;
    }

    public String getPos2Dimension() {
        return pos2Dimension;
    }

    public void setPos2Dimension(String pos2Dimension) {
        this.pos2Dimension = pos2Dimension;
    }

    public boolean hasBothPositions() {
        return pos1 != null
                && pos2 != null
                && pos1Dimension != null
                && pos2Dimension != null;
    }

    public boolean isSameDimension() {
        return hasBothPositions()
                && pos1Dimension.equals(pos2Dimension);
    }
}