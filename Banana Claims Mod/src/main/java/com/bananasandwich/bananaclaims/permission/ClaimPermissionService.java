package com.bananasandwich.bananaclaims.permission;

import com.bananasandwich.bananaclaims.config.BananaClaimsConfigManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Single permission-policy entry point for commands and protection bypasses.
 */
public final class ClaimPermissionService {

    private final BananaClaimsConfigManager configManager;
    private final FabricPermissionsBridge permissionsBridge =
            new FabricPermissionsBridge();

    public ClaimPermissionService(
            BananaClaimsConfigManager configManager
    ) {
        this.configManager =
                Objects.requireNonNull(
                        configManager,
                        "configManager"
                );
    }

    public boolean has(
            CommandSourceStack source,
            ClaimPermission permission
    ) {
        if (source == null
                || permission == null) {
            return false;
        }

        int fallbackLevel =
                configManager.getFallbackLevel(permission);

        if (configManager.isFabricPermissionsApiEnabled()
                && permissionsBridge.isAvailable()) {
            Boolean externalResult =
                    permissionsBridge.check(
                            source,
                            permission.getNode(),
                            fallbackLevel
                    );

            if (externalResult != null) {
                return externalResult;
            }
        }

        return hasVanillaFallback(
                source,
                fallbackLevel
        );
    }

    public boolean has(
            ServerPlayer player,
            ClaimPermission permission
    ) {
        return player != null
                && has(
                player.createCommandSourceStack(),
                permission
        );
    }

    public Predicate<CommandSourceStack> require(
            ClaimPermission permission
    ) {
        return source -> has(source, permission);
    }

    public boolean isFabricPermissionsApiAvailable() {
        return permissionsBridge.isAvailable();
    }

    private static boolean hasVanillaFallback(
            CommandSourceStack source,
            int fallbackLevel
    ) {
        if (fallbackLevel <= 0) {
            return true;
        }

        Permission permission =
                new Permission.HasCommandLevel(
                        PermissionLevel.byId(
                                fallbackLevel
                        )
                );

        return source.permissions()
                .hasPermission(permission);
    }
}
