package com.bananasandwich.bananaclaims.command.admin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permissions;

/**
 * Central permission gate for Banana Claims administration commands.
 *
 * <p>This currently uses Minecraft's administrator command level so it works
 * without adding a new hard dependency. The class provides one integration
 * point for granular permission nodes during the 1.0 permissions milestone.</p>
 */
public final class AdminClaimPermission {

    private AdminClaimPermission() {
    }

    public static boolean canUse(
            CommandSourceStack source
    ) {
        return source != null
                && source.permissions()
                .hasPermission(
                        Permissions.COMMANDS_ADMIN
                );
    }
}
