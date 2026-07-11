package com.bananasandwich.bananaclaims.command.admin;

import com.bananasandwich.bananaclaims.Bananaclaims;
import com.bananasandwich.bananaclaims.claim.Claim;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolves globally-addressed claims safely for administrative commands.
 *
 * <p>Accepted selectors are a claim UUID, a globally unique claim name, or
 * {@code name@owner} when multiple owners use the same claim name.</p>
 */
public final class AdminClaimSelector {

    private AdminClaimSelector() {
    }

    public static Resolution resolve(
            String selector
    ) {
        if (selector == null || selector.isBlank()) {
            return Resolution.notFound();
        }

        String trimmed = selector.trim();

        try {
            UUID claimId = UUID.fromString(trimmed);
            Optional<Claim> byId =
                    Bananaclaims.CLAIM_MANAGER.getClaimById(claimId);

            return byId
                    .map(Resolution::found)
                    .orElseGet(Resolution::notFound);
        } catch (IllegalArgumentException ignored) {
        }

        int ownerSeparator = trimmed.lastIndexOf('@');

        if (ownerSeparator > 0
                && ownerSeparator < trimmed.length() - 1) {
            String claimName = trimmed.substring(0, ownerSeparator);
            String ownerName = trimmed.substring(ownerSeparator + 1);

            List<Claim> matches = allClaims().stream()
                    .filter(claim ->
                            equalsIgnoreCase(
                                    claim.getName(),
                                    claimName
                            )
                                    && equalsIgnoreCase(
                                    claim.getOwnerName(),
                                    ownerName
                            )
                    )
                    .toList();

            return fromMatches(matches);
        }

        List<Claim> matches = allClaims().stream()
                .filter(claim ->
                        equalsIgnoreCase(
                                claim.getName(),
                                trimmed
                        )
                )
                .toList();

        return fromMatches(matches);
    }

    public static List<String> suggestions() {
        List<Claim> claims = allClaims();

        return claims.stream()
                .map(claim -> selectorFor(claim, claims))
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public static String selectorFor(
            Claim claim
    ) {
        return selectorFor(
                claim,
                allClaims()
        );
    }

    private static String selectorFor(
            Claim claim,
            List<Claim> claims
    ) {
        if (claim == null) {
            return "";
        }

        String name = safe(claim.getName());

        long sameNameCount = claims.stream()
                .filter(candidate ->
                        equalsIgnoreCase(
                                candidate.getName(),
                                name
                        )
                )
                .count();

        if (sameNameCount <= 1) {
            return name;
        }

        return name
                + "@"
                + safe(claim.getOwnerName());
    }

    private static Resolution fromMatches(
            List<Claim> matches
    ) {
        if (matches == null || matches.isEmpty()) {
            return Resolution.notFound();
        }

        if (matches.size() == 1) {
            return Resolution.found(matches.getFirst());
        }

        return Resolution.ambiguous(matches);
    }

    private static List<Claim> allClaims() {
        return Bananaclaims.CLAIM_MANAGER.getAllClaims();
    }

    private static boolean equalsIgnoreCase(
            String left,
            String right
    ) {
        return safe(left)
                .toLowerCase(Locale.ROOT)
                .equals(
                        safe(right)
                                .toLowerCase(Locale.ROOT)
                );
    }

    private static String safe(
            String value
    ) {
        return value == null ? "" : value;
    }

    public enum Status {
        FOUND,
        NOT_FOUND,
        AMBIGUOUS
    }

    public record Resolution(
            Status status,
            Claim claim,
            List<Claim> matches
    ) {
        private static Resolution found(
                Claim claim
        ) {
            return new Resolution(
                    Status.FOUND,
                    claim,
                    List.of(claim)
            );
        }

        private static Resolution notFound() {
            return new Resolution(
                    Status.NOT_FOUND,
                    null,
                    List.of()
            );
        }

        private static Resolution ambiguous(
                List<Claim> matches
        ) {
            return new Resolution(
                    Status.AMBIGUOUS,
                    null,
                    List.copyOf(matches)
            );
        }

        public boolean found() {
            return status == Status.FOUND
                    && claim != null;
        }
    }
}
