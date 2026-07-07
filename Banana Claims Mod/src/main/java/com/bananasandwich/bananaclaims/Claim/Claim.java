package com.bananasandwich.bananaclaims.claim;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Claim {
    private String name;
    private UUID ownerUuid;
    private String ownerName;
    private String dimension;
    private int chunkX;
    private int chunkZ;
    private String description;
    private Set<ClaimChunk> chunks = new HashSet<>();

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
        this.chunks = new HashSet<>();
        this.chunks.add(new ClaimChunk(dimension, chunkX, chunkZ));
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
        ensureChunks();
        return chunks.iterator().next().getChunkX();
    }

    public int getChunkZ() {
        ensureChunks();
        return chunks.iterator().next().getChunkZ();
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<ClaimChunk> getChunks() {
        ensureChunks();
        return Set.copyOf(chunks);
    }

    public void addChunk(String dimension, int chunkX, int chunkZ) {
        ensureChunks();
        chunks.add(new ClaimChunk(dimension, chunkX, chunkZ));
    }

    public boolean removeChunk(String dimension, int chunkX, int chunkZ) {
        ensureChunks();
        return chunks.remove(new ClaimChunk(dimension, chunkX, chunkZ));
    }

    public boolean containsChunk(String dimension, int chunkX, int chunkZ) {
        ensureChunks();
        return chunks.contains(new ClaimChunk(dimension, chunkX, chunkZ));
    }

    public boolean isOwner(UUID playerUuid) {
        return ownerUuid != null && ownerUuid.equals(playerUuid);
    }

    private void ensureChunks() {
        if (chunks == null) {
            chunks = new HashSet<>();
        }

        if (chunks.isEmpty() && dimension != null) {
            chunks.add(new ClaimChunk(dimension, chunkX, chunkZ));
        }
    }
}