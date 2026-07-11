package com.bananasandwich.bananaclaims.protection;

import com.bananasandwich.bananaclaims.claim.ClaimFlags;
import net.minecraft.network.chat.Component;

/**
 * A concrete protected action performed inside a claim.
 *
 * <p>Several actions intentionally share one persisted claim flag. Keeping
 * the actions separate gives callers precise denial messages while preserving
 * the existing compact flag model and JSON compatibility.</p>
 */
public enum ProtectionAction {

    BREAK_BLOCKS(
            "protection.bananaclaims.break_blocks",
            "You cannot break blocks in this claim."
    ),

    PLACE_BLOCKS(
            "protection.bananaclaims.place_blocks",
            "You cannot place blocks in this claim."
    ),

    BLOCK_INTERACTION(
            "protection.bananaclaims.block_interaction",
            "You cannot use that block in this claim."
    ),

    CONTAINER_INTERACTION(
            "protection.bananaclaims.container_interaction",
            "You cannot open containers in this claim."
    ),

    ENTITY_INTERACTION(
            "protection.bananaclaims.entity_interaction",
            "You cannot interact with entities in this claim."
    ),

    ENTITY_PLACEMENT(
            "protection.bananaclaims.entity_placement",
            "You cannot place entities in this claim."
    ),

    ENTITY_DAMAGE(
            "protection.bananaclaims.entity_damage",
            "You cannot damage entities in this claim."
    ),

    PVP(
            "protection.bananaclaims.pvp",
            "PvP is disabled in this claim."
    ),

    EXPLOSION(
            "protection.bananaclaims.explosion",
            "Explosions are disabled in this claim."
    );

    private final String translationKey;
    private final String fallbackMessage;

    ProtectionAction(
            String translationKey,
            String fallbackMessage
    ) {
        this.translationKey = translationKey;
        this.fallbackMessage = fallbackMessage;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public String getFallbackMessage() {
        return fallbackMessage;
    }

    public Component getDenialComponent() {
        return Component.translatableWithFallback(
                translationKey,
                fallbackMessage
        );
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
