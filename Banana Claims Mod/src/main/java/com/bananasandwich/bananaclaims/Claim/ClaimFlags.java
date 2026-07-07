package com.bananasandwich.bananaclaims.claim;

public class ClaimFlags {
    private boolean breakBlocks = false;
    private boolean placeBlocks = false;
    private boolean interact = false;
    private boolean containers = false;
    private boolean entities = false;
    private boolean pvp = false;
    private boolean explosions = false;
    private boolean fireSpread = false;
    private boolean mobGriefing = false;

    public boolean isBreakBlocks() {
        return breakBlocks;
    }

    public void setBreakBlocks(boolean breakBlocks) {
        this.breakBlocks = breakBlocks;
    }

    public boolean isPlaceBlocks() {
        return placeBlocks;
    }

    public void setPlaceBlocks(boolean placeBlocks) {
        this.placeBlocks = placeBlocks;
    }

    public boolean isInteract() {
        return interact;
    }

    public void setInteract(boolean interact) {
        this.interact = interact;
    }

    public boolean isContainers() {
        return containers;
    }

    public void setContainers(boolean containers) {
        this.containers = containers;
    }

    public boolean isEntities() {
        return entities;
    }

    public void setEntities(boolean entities) {
        this.entities = entities;
    }

    public boolean isPvp() {
        return pvp;
    }

    public void setPvp(boolean pvp) {
        this.pvp = pvp;
    }

    public boolean isExplosions() {
        return explosions;
    }

    public void setExplosions(boolean explosions) {
        this.explosions = explosions;
    }

    public boolean isFireSpread() {
        return fireSpread;
    }

    public void setFireSpread(boolean fireSpread) {
        this.fireSpread = fireSpread;
    }

    public boolean isMobGriefing() {
        return mobGriefing;
    }

    public void setMobGriefing(boolean mobGriefing) {
        this.mobGriefing = mobGriefing;
    }
}