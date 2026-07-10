package com.bananasandwich.bananaclaims.claim;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class Claim {

    private UUID claimId;

    private String name;
    private UUID ownerUuid;
    private String ownerName;
    private String dimension;
    private int chunkX;
    private int chunkZ;
    private String description;

    private Set<ClaimChunk> chunks = new HashSet<>();
    private Set<ClaimMember> members = new HashSet<>();
    private Set<ClaimSubOwner> subOwners = new HashSet<>();

    private ClaimFlags flags = new ClaimFlags();
    private ClaimPopupSettings popupSettings = new ClaimPopupSettings();

    public Claim() {
    }

    public Claim(
            String name,
            UUID ownerUuid,
            String ownerName,
            String dimension,
            int chunkX,
            int chunkZ
    ) {
        this.claimId = UUID.randomUUID();

        this.name = name;
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
        this.dimension = dimension;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.description = "";

        this.chunks = new HashSet<>();
        this.chunks.add(
                new ClaimChunk(
                        dimension,
                        chunkX,
                        chunkZ
                )
        );

        this.members = new HashSet<>();
        this.subOwners = new HashSet<>();
        this.flags = new ClaimFlags();
        this.popupSettings = new ClaimPopupSettings();
    }

    public UUID getClaimId() {
        ensureClaimId();
        return claimId;
    }

    public boolean ensureClaimId() {
        if (claimId != null) {
            return false;
        }

        claimId = UUID.randomUUID();
        return true;
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
        return ownerName == null ? "" : ownerName;
    }

    public boolean transferOwnership(
            UUID newOwnerUuid,
            String newOwnerName
    ) {
        if (newOwnerUuid == null || newOwnerUuid.equals(ownerUuid)) {
            return false;
        }

        UUID previousOwnerUuid = ownerUuid;
        String previousOwnerName = getOwnerName();

        removeMember(newOwnerUuid);
        removeSubOwner(newOwnerUuid);

        ownerUuid = newOwnerUuid;
        ownerName = newOwnerName == null ? "" : newOwnerName;

        if (previousOwnerUuid != null) {
            addMember(
                    previousOwnerUuid,
                    previousOwnerName
            );
        }

        return true;
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

    public void addChunk(
            String dimension,
            int chunkX,
            int chunkZ
    ) {
        ensureChunks();

        chunks.add(
                new ClaimChunk(
                        dimension,
                        chunkX,
                        chunkZ
                )
        );
    }

    public boolean removeChunk(
            String dimension,
            int chunkX,
            int chunkZ
    ) {
        ensureChunks();

        return chunks.remove(
                new ClaimChunk(
                        dimension,
                        chunkX,
                        chunkZ
                )
        );
    }

    public boolean containsChunk(
            String dimension,
            int chunkX,
            int chunkZ
    ) {
        ensureChunks();

        return chunks.contains(
                new ClaimChunk(
                        dimension,
                        chunkX,
                        chunkZ
                )
        );
    }

    public Set<ClaimMember> getMembers() {
        ensureMembers();
        return Set.copyOf(members);
    }

    public Optional<ClaimMember> getMember(UUID playerUuid) {
        if (playerUuid == null) {
            return Optional.empty();
        }

        ensureMembers();

        return members.stream()
                .filter(member ->
                        playerUuid.equals(member.getUuid())
                )
                .findFirst();
    }

    public boolean addMember(UUID playerUuid, String playerName) {
        if (playerUuid == null || isOwner(playerUuid) || isSubOwner(playerUuid)) {
            return false;
        }

        ensureMembers();

        Optional<ClaimMember> existingMember =
                getMember(playerUuid);

        if (existingMember.isPresent()) {
            existingMember.get().setName(playerName);
            return false;
        }

        return members.add(
                new ClaimMember(
                        playerUuid,
                        playerName
                )
        );
    }

    public boolean removeMember(UUID playerUuid) {
        if (playerUuid == null) {
            return false;
        }

        ensureMembers();

        return members.removeIf(member ->
                playerUuid.equals(member.getUuid())
        );
    }

    public boolean isMember(UUID playerUuid) {
        return getMember(playerUuid).isPresent();
    }


    public Set<ClaimSubOwner> getSubOwners() {
        ensureSubOwners();
        return Set.copyOf(subOwners);
    }

    public Optional<ClaimSubOwner> getSubOwner(UUID playerUuid) {
        if (playerUuid == null) {
            return Optional.empty();
        }

        ensureSubOwners();

        return subOwners.stream()
                .filter(subOwner ->
                        playerUuid.equals(subOwner.getUuid())
                )
                .findFirst();
    }

    public boolean addSubOwner(UUID playerUuid, String playerName) {
        if (playerUuid == null || isOwner(playerUuid)) {
            return false;
        }

        ensureSubOwners();

        Optional<ClaimSubOwner> existingSubOwner =
                getSubOwner(playerUuid);

        if (existingSubOwner.isPresent()) {
            existingSubOwner.get().setName(playerName);
            return false;
        }

        removeMember(playerUuid);

        return subOwners.add(
                new ClaimSubOwner(
                        playerUuid,
                        playerName
                )
        );
    }

    public boolean removeSubOwner(UUID playerUuid) {
        if (playerUuid == null) {
            return false;
        }

        ensureSubOwners();

        return subOwners.removeIf(subOwner ->
                playerUuid.equals(subOwner.getUuid())
        );
    }

    public boolean demoteSubOwnerToMember(UUID playerUuid) {
        Optional<ClaimSubOwner> optionalSubOwner = getSubOwner(playerUuid);

        if (optionalSubOwner.isEmpty()) {
            return false;
        }

        ClaimSubOwner subOwner = optionalSubOwner.get();

        if (!removeSubOwner(playerUuid)) {
            return false;
        }

        addMember(
                subOwner.getUuid(),
                subOwner.getName()
        );

        return true;
    }

    public boolean isSubOwner(UUID playerUuid) {
        return getSubOwner(playerUuid).isPresent();
    }

    public boolean canAccess(UUID playerUuid) {
        return isOwner(playerUuid)
                || isSubOwner(playerUuid)
                || isMember(playerUuid);
    }

    public boolean hasAccess(UUID playerUuid) {
        return canAccess(playerUuid);
    }

    public boolean canManage(UUID playerUuid) {
        return isOwner(playerUuid) || isSubOwner(playerUuid);
    }

    public boolean canResize(UUID playerUuid) {
        return canManage(playerUuid);
    }

    public boolean canEditMembers(UUID playerUuid) {
        return canManage(playerUuid);
    }

    public boolean canEditFlags(UUID playerUuid) {
        return canManage(playerUuid);
    }

    public boolean canEditPopup(UUID playerUuid) {
        return canManage(playerUuid);
    }

    public boolean canTransfer(UUID playerUuid) {
        return isOwner(playerUuid);
    }

    public boolean canDelete(UUID playerUuid) {
        return isOwner(playerUuid);
    }

    public ClaimFlags getFlags() {
        ensureFlags();
        return flags;
    }

    public ClaimPopupSettings getPopupSettings() {
        ensurePopupSettings();
        return popupSettings;
    }

    public boolean isOwner(UUID playerUuid) {
        return ownerUuid != null
                && ownerUuid.equals(playerUuid);
    }

    private void ensureChunks() {
        if (chunks == null) {
            chunks = new HashSet<>();
        }

        if (chunks.isEmpty() && dimension != null) {
            chunks.add(
                    new ClaimChunk(
                            dimension,
                            chunkX,
                            chunkZ
                    )
            );
        }
    }

    private void ensureMembers() {
        if (members == null) {
            members = new HashSet<>();
        }

        members.removeIf(member ->
                member == null || member.getUuid() == null
        );
    }

    private void ensureSubOwners() {
        if (subOwners == null) {
            subOwners = new HashSet<>();
        }

        subOwners.removeIf(subOwner ->
                subOwner == null || subOwner.getUuid() == null
        );
    }

    private void ensureFlags() {
        if (flags == null) {
            flags = new ClaimFlags();
        }
    }

    private void ensurePopupSettings() {
        if (popupSettings == null) {
            popupSettings = new ClaimPopupSettings();
        }
    }
}
