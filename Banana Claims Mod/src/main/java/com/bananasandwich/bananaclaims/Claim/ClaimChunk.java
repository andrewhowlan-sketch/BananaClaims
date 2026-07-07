package com.bananasandwich.bananaclaims.claim;

import java.util.Objects;

public class ClaimChunk {
    private String dimension;
    private int chunkX;
    private int chunkZ;

    public ClaimChunk() {
    }

    public ClaimChunk(String dimension, int chunkX, int chunkZ) {
        this.dimension = dimension;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public String getDimension() {
        return dimension;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ClaimChunk other)) return false;

        return chunkX == other.chunkX
                && chunkZ == other.chunkZ
                && Objects.equals(dimension, other.dimension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension, chunkX, chunkZ);
    }

    @Override
    public String toString() {
        return dimension + ":" + chunkX + "," + chunkZ;
    }
}