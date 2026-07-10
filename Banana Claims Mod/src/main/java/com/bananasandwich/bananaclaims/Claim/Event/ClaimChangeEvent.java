package com.bananasandwich.bananaclaims.claim.event;

import com.bananasandwich.bananaclaims.claim.Claim;

public class ClaimChangeEvent {

    private final ClaimChangeType type;
    private final Claim claim;
    private final long revision;

    public ClaimChangeEvent(
            ClaimChangeType type,
            Claim claim,
            long revision
    ) {
        this.type = type;
        this.claim = claim;
        this.revision = revision;
    }

    public ClaimChangeType getType() {
        return type;
    }

    public Claim getClaim() {
        return claim;
    }

    public long getRevision() {
        return revision;
    }
}