package com.bananasandwich.bananaclaims.invitation;

import java.util.UUID;

public record ClaimInvitation(
        UUID invitationId,
        UUID claimId,
        String claimName,
        UUID inviterUuid,
        String inviterName,
        UUID inviteeUuid,
        String inviteeName,
        long createdAtMillis,
        long expiresAtMillis
) {
    public boolean isExpired(long nowMillis) {
        return nowMillis >= expiresAtMillis;
    }

    public String selector() {
        return claimName + "@" + inviterName;
    }

    public boolean matchesSelector(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return selector().equalsIgnoreCase(value)
                || claimName.equalsIgnoreCase(value)
                || claimId.toString().equalsIgnoreCase(value)
                || invitationId.toString().equalsIgnoreCase(value);
    }
}
