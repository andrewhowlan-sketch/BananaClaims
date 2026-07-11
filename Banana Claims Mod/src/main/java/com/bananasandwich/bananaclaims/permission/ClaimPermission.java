package com.bananasandwich.bananaclaims.permission;

import java.util.List;

/**
 * Canonical Banana Claims permission nodes.
 */
public enum ClaimPermission {

    COMMAND_ROOT(
            "bananaclaims.command.claim",
            Group.PUBLIC
    ),
    POS1("bananaclaims.command.pos1", Group.PUBLIC),
    POS2("bananaclaims.command.pos2", Group.PUBLIC),
    CREATE("bananaclaims.command.create", Group.PUBLIC),
    CREATE_AREA("bananaclaims.command.createarea", Group.PUBLIC),
    PREVIEW("bananaclaims.command.preview", Group.PUBLIC),
    BOOK("bananaclaims.command.book", Group.PUBLIC),
    INVITE("bananaclaims.command.invite", Group.PUBLIC),
    LEAVE("bananaclaims.command.leave", Group.PUBLIC),
    INFO("bananaclaims.command.info", Group.PUBLIC),
    LIST("bananaclaims.command.list", Group.PUBLIC),

    EXPAND("bananaclaims.command.expand", Group.MANAGEMENT),
    SHRINK("bananaclaims.command.shrink", Group.MANAGEMENT),
    DELETE("bananaclaims.command.delete", Group.MANAGEMENT),
    RENAME("bananaclaims.command.rename", Group.MANAGEMENT),
    DESCRIPTION("bananaclaims.command.description", Group.MANAGEMENT),
    MEMBER("bananaclaims.command.member", Group.MANAGEMENT),
    SUBOWNER("bananaclaims.command.subowner", Group.MANAGEMENT),
    TRANSFER("bananaclaims.command.transfer", Group.MANAGEMENT),
    FLAG("bananaclaims.command.flag", Group.MANAGEMENT),
    POPUP("bananaclaims.command.popup", Group.MANAGEMENT),
    BLUEMAP("bananaclaims.command.bluemap", Group.MANAGEMENT),

    ADMIN_ROOT("bananaclaims.command.admin", Group.ADMIN),
    ADMIN_LIST("bananaclaims.command.admin.list", Group.ADMIN),
    ADMIN_INFO("bananaclaims.command.admin.info", Group.ADMIN),
    ADMIN_NEAREST("bananaclaims.command.admin.nearest", Group.ADMIN),
    ADMIN_FORCE_TRANSFER(
            "bananaclaims.command.admin.force-transfer",
            Group.ADMIN
    ),
    ADMIN_FORCE_DELETE(
            "bananaclaims.command.admin.force-delete",
            Group.ADMIN
    ),
    ADMIN_RELOAD_ALL(
            "bananaclaims.command.admin.reload",
            Group.ADMIN
    ),
    ADMIN_RELOAD_CONFIG(
            "bananaclaims.command.admin.reload.config",
            Group.ADMIN
    ),
    ADMIN_RELOAD_CLAIMS(
            "bananaclaims.command.admin.reload.claims",
            Group.ADMIN
    ),
    ADMIN_RELOAD_PREVIEW(
            "bananaclaims.command.admin.reload.preview",
            Group.ADMIN
    ),
    ADMIN_DIAGNOSTICS(
            "bananaclaims.command.admin.diagnostics",
            Group.ADMIN
    ),

    PROTECTION_BYPASS(
            "bananaclaims.protection.bypass",
            Group.ADMIN
    );

    private static final List<ClaimPermission> ADMIN_PERMISSIONS =
            List.of(
                    ADMIN_ROOT,
                    ADMIN_LIST,
                    ADMIN_INFO,
                    ADMIN_NEAREST,
                    ADMIN_FORCE_TRANSFER,
                    ADMIN_FORCE_DELETE,
                    ADMIN_RELOAD_ALL,
                    ADMIN_RELOAD_CONFIG,
                    ADMIN_RELOAD_CLAIMS,
                    ADMIN_RELOAD_PREVIEW,
                    ADMIN_DIAGNOSTICS
            );

    private final String node;
    private final Group group;

    ClaimPermission(
            String node,
            Group group
    ) {
        this.node = node;
        this.group = group;
    }

    public String getNode() {
        return node;
    }

    public Group getGroup() {
        return group;
    }

    public static List<ClaimPermission> adminPermissions() {
        return ADMIN_PERMISSIONS;
    }

    public enum Group {
        PUBLIC,
        MANAGEMENT,
        ADMIN
    }
}
