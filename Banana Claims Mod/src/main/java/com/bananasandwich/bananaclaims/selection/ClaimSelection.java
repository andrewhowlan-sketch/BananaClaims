package com.bananasandwich.bananaclaims.selection;

import net.minecraft.core.BlockPos;

public class ClaimSelection {

    private BlockPos pos1;
    private BlockPos pos2;
    private String dimension;

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

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public boolean hasBothPositions() {
        return pos1 != null && pos2 != null;
    }
}