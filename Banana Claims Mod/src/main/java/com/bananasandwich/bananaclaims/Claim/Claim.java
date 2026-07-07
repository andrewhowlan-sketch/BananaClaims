package com.bananasandwich.bananaclaims.claim;

import java.util.UUID;

public class Claim {
    private String name;
    private UUID ownerUuid;
    private String ownerName;
    private String dimension;
    private int chunkX;
    private int chunkZ;
    private String description;

    public Claim() {
    }

    public Claim(String name, UUID ownerUuid, String ownerName, String dimension, int chunkX, int chunkZ) {
        this.name = name;
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
        this.dimension = dimension;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.description = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public String getOwnerName() {
        return ownerName;
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

    public String getDescription() {
        return description == null ? "" : description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isOwner(UUID playerUuid) {
        return ownerUuid != null && ownerUuid.equals(playerUuid);
    }
}