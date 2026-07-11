package com.bananasandwich.bananaclaims.protection;

import com.bananasandwich.bananaclaims.claim.ClaimFlags;

/**
 * A concrete protected action performed inside a claim.
 *
 * <p>Several actions intentionally share one persisted claim flag. Keeping
 * the actions separate gives callers precise denial messages while preserving
 * the existing compact flag model and JSON compatibility.</p>
 */
public enum ProtectionAction {

    BREAK_BLOCKS(
            "You cannot break blocks in this claim."
    ),

    PLACE_BLOCKS(
            "You cannot place blocks in this claim."
    ),

    BLOCK_INTERACTION(
            "You cannot use that block in this claim."
    ),

    CONTAINER_INTERACTION(
            "You cannot open containers in this claim."
    ),

    ENTITY_INTERACTION(
            "You cannot interact with entities in this claim."
    ),

    ENTITY_PLACEMENT(
            "You cannot place entities in this claim."
    ),

    ENTITY_DAMAGE(
            "You cannot damage entities in this claim."
    ),

    PVP(
            "PvP is disabled in this claim."
    ),

    EXPLOSION(
            "Explosions are disabled in this claim."
    );

    private final String denialMessage;

    ProtectionAction(
            String denialMessage
    ) {
        this.denialMessage = denialMessage;
    }

    public String getDenialMessage() {
        return denialMessage;
    }

    public boolean isEnabled(
            ClaimFlags flags
    ) {
        if (flags == null) {
            return false;
        }

        return switch (this) {
            case BREAK_BLOCKS -> flags.isBreakBlocks();
            case PLACE_BLOCKS -> flags.isPlaceBlocks();
            case BLOCK_INTERACTION -> flags.isInteract();
            case CONTAINER_INTERACTION -> flags.isContainers();
            case ENTITY_INTERACTION,
                 ENTITY_PLACEMENT,
                 ENTITY_DAMAGE -> flags.isEntities();
            case PVP -> flags.isPvp();
            case EXPLOSION -> flags.isExplosions();
        };
    }
}
