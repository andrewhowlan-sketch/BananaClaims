package com.bananasandwich.bananaclaims.config;

import com.bananasandwich.bananaclaims.permission.ClaimPermission;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Main Banana Claims server configuration.
 *
 * <p>This deliberately excludes Renderer V2 settings, which remain in their
 * dedicated preview configuration. Keeping the two models separate avoids
 * turning unrelated reloads into renderer changes and gives the future Book
 * GUI a stable preview-specific settings model.</p>
 */
public final class BananaClaimsConfig {

    public static final int CURRENT_CONFIG_VERSION = 2;

    private int configVersion;

    private PermissionSettings permissions =
            new PermissionSettings();

    private ProtectionSettings protection =
            new ProtectionSettings();

    private InvitationSettings invitations =
            new InvitationSettings();

    public static BananaClaimsConfig defaults() {
        return new BananaClaimsConfig();
    }

    public BananaClaimsConfig copy() {
        BananaClaimsConfig copy =
                new BananaClaimsConfig();

        copy.configVersion = configVersion;
        copy.permissions = permissions == null
                ? null
                : permissions.copy();
        copy.protection = protection == null
                ? null
                : protection.copy();
        copy.invitations = invitations == null
                ? null
                : invitations.copy();

        return copy;
    }

    boolean sanitize() {
        boolean changed = false;

        if (configVersion != CURRENT_CONFIG_VERSION) {
            configVersion = CURRENT_CONFIG_VERSION;
            changed = true;
        }

        if (permissions == null) {
            permissions = new PermissionSettings();
            changed = true;
        }

        if (protection == null) {
            protection = new ProtectionSettings();
            changed = true;
        }

        if (invitations == null) {
            invitations = new InvitationSettings();
            changed = true;
        }

        changed |= permissions.sanitize();
        changed |= protection.sanitize();
        changed |= invitations.sanitize();

        return changed;
    }

    public int getConfigVersion() {
        return configVersion;
    }

    public PermissionSettings getPermissions() {
        return permissions;
    }

    public ProtectionSettings getProtection() {
        return protection;
    }

    public InvitationSettings getInvitations() {
        return invitations;
    }

    public static final class PermissionSettings {

        private boolean fabricPermissionsApiEnabled = true;
        private int publicFallbackLevel = 0;
        private int managementFallbackLevel = 0;
        private int adminFallbackLevel = 3;

        /**
         * Optional exact-node overrides. Values use vanilla command levels
         * 0-4 and are only consulted when the external permission provider
         * returns its fallback/default result.
         */
        private Map<String, Integer> fallbackLevelOverrides =
                new LinkedHashMap<>();

        private PermissionSettings copy() {
            PermissionSettings copy =
                    new PermissionSettings();

            copy.fabricPermissionsApiEnabled =
                    fabricPermissionsApiEnabled;
            copy.publicFallbackLevel = publicFallbackLevel;
            copy.managementFallbackLevel = managementFallbackLevel;
            copy.adminFallbackLevel = adminFallbackLevel;
            copy.fallbackLevelOverrides =
                    new LinkedHashMap<>(fallbackLevelOverrides);

            return copy;
        }

        private boolean sanitize() {
            boolean changed = false;

            int sanitizedPublic =
                    clampPermissionLevel(publicFallbackLevel);
            changed |= sanitizedPublic != publicFallbackLevel;
            publicFallbackLevel = sanitizedPublic;

            int sanitizedManagement =
                    clampPermissionLevel(managementFallbackLevel);
            changed |= sanitizedManagement != managementFallbackLevel;
            managementFallbackLevel = sanitizedManagement;

            int sanitizedAdmin =
                    clampPermissionLevel(adminFallbackLevel);
            changed |= sanitizedAdmin != adminFallbackLevel;
            adminFallbackLevel = sanitizedAdmin;

            if (fallbackLevelOverrides == null) {
                fallbackLevelOverrides = new LinkedHashMap<>();
                return true;
            }

            Map<String, Integer> sanitizedOverrides =
                    new LinkedHashMap<>();

            for (Map.Entry<String, Integer> entry
                    : fallbackLevelOverrides.entrySet()) {
                if (entry.getKey() == null
                        || entry.getKey().isBlank()
                        || entry.getValue() == null) {
                    changed = true;
                    continue;
                }

                String normalizedNode =
                        entry.getKey()
                                .trim()
                                .toLowerCase(Locale.ROOT);

                int normalizedLevel =
                        clampPermissionLevel(entry.getValue());

                if (!normalizedNode.equals(entry.getKey())
                        || normalizedLevel != entry.getValue()) {
                    changed = true;
                }

                sanitizedOverrides.put(
                        normalizedNode,
                        normalizedLevel
                );
            }

            if (!sanitizedOverrides.equals(fallbackLevelOverrides)) {
                fallbackLevelOverrides = sanitizedOverrides;
                changed = true;
            }

            return changed;
        }

        public boolean isFabricPermissionsApiEnabled() {
            return fabricPermissionsApiEnabled;
        }

        public int getPublicFallbackLevel() {
            return publicFallbackLevel;
        }

        public int getManagementFallbackLevel() {
            return managementFallbackLevel;
        }

        public int getAdminFallbackLevel() {
            return adminFallbackLevel;
        }

        public Map<String, Integer> getFallbackLevelOverrides() {
            return Map.copyOf(fallbackLevelOverrides);
        }

        public int resolveFallbackLevel(
                ClaimPermission permission
        ) {
            Integer override =
                    fallbackLevelOverrides.get(
                            permission.getNode()
                    );

            if (override != null) {
                return override;
            }

            return switch (permission.getGroup()) {
                case PUBLIC -> publicFallbackLevel;
                case MANAGEMENT -> managementFallbackLevel;
                case ADMIN -> adminFallbackLevel;
            };
        }
    }

    public static final class ProtectionSettings {

        private boolean denialMessagesEnabled = true;
        private int denialMessageCooldownTicks = 20;

        private ProtectionSettings copy() {
            ProtectionSettings copy =
                    new ProtectionSettings();

            copy.denialMessagesEnabled =
                    denialMessagesEnabled;
            copy.denialMessageCooldownTicks =
                    denialMessageCooldownTicks;

            return copy;
        }

        private boolean sanitize() {
            int sanitizedCooldown =
                    Math.max(
                            0,
                            Math.min(
                                    20 * 60,
                                    denialMessageCooldownTicks
                            )
                    );

            if (sanitizedCooldown
                    == denialMessageCooldownTicks) {
                return false;
            }

            denialMessageCooldownTicks =
                    sanitizedCooldown;

            return true;
        }

        public boolean isDenialMessagesEnabled() {
            return denialMessagesEnabled;
        }

        public int getDenialMessageCooldownTicks() {
            return denialMessageCooldownTicks;
        }
    }


    public static final class InvitationSettings {

        private boolean enabled = true;
        private int expirationSeconds = 300;
        private int maxPendingPerClaim = 20;
        private boolean notifyOnExpiration = true;

        private InvitationSettings copy() {
            InvitationSettings copy = new InvitationSettings();
            copy.enabled = enabled;
            copy.expirationSeconds = expirationSeconds;
            copy.maxPendingPerClaim = maxPendingPerClaim;
            copy.notifyOnExpiration = notifyOnExpiration;
            return copy;
        }

        private boolean sanitize() {
            boolean changed = false;

            int sanitizedExpiration = Math.max(30, Math.min(24 * 60 * 60, expirationSeconds));
            changed |= sanitizedExpiration != expirationSeconds;
            expirationSeconds = sanitizedExpiration;

            int sanitizedLimit = Math.max(1, Math.min(100, maxPendingPerClaim));
            changed |= sanitizedLimit != maxPendingPerClaim;
            maxPendingPerClaim = sanitizedLimit;

            return changed;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public int getExpirationSeconds() {
            return expirationSeconds;
        }

        public int getMaxPendingPerClaim() {
            return maxPendingPerClaim;
        }

        public boolean isNotifyOnExpiration() {
            return notifyOnExpiration;
        }
    }

    private static int clampPermissionLevel(
            int level
    ) {
        return Math.max(
                0,
                Math.min(4, level)
        );
    }
}
