# Banana Claims Permissions

Banana Claims integrates with LuckPerms through Fabric Permissions API v0 when it is installed. If the API is not available, the configured vanilla command-level fallbacks are used.

The broad `bananaclaims.command.admin` node grants all Banana Claims administration commands. Individual admin nodes may be granted instead.

## Public commands

- `bananaclaims.command.claim`
- `bananaclaims.command.pos1`
- `bananaclaims.command.pos2`
- `bananaclaims.command.create`
- `bananaclaims.command.createarea`
- `bananaclaims.command.preview`
- `bananaclaims.command.leave`
- `bananaclaims.command.info`
- `bananaclaims.command.list`

## Claim-management commands

- `bananaclaims.command.expand`
- `bananaclaims.command.shrink`
- `bananaclaims.command.delete`
- `bananaclaims.command.rename`
- `bananaclaims.command.description`
- `bananaclaims.command.member`
- `bananaclaims.command.subowner`
- `bananaclaims.command.transfer`
- `bananaclaims.command.flag`
- `bananaclaims.command.popup`

These permission nodes control command availability. Claim ownership, subowner, and member rules are still enforced after the permission check.

## Administration

- `bananaclaims.command.admin`
- `bananaclaims.command.admin.list`
- `bananaclaims.command.admin.info`
- `bananaclaims.command.admin.nearest`
- `bananaclaims.command.admin.force-transfer`
- `bananaclaims.command.admin.force-delete`
- `bananaclaims.command.admin.reload`
- `bananaclaims.command.admin.reload.config`
- `bananaclaims.command.admin.reload.claims`
- `bananaclaims.command.admin.reload.preview`
- `bananaclaims.command.admin.diagnostics`

## Protection

- `bananaclaims.protection.bypass`

This bypasses enabled claim protections globally. Its default fallback is the configured administrator command level.

## Configuration fallbacks

`config/bananaclaims.json` contains three default fallback levels and optional exact-node overrides:

- `publicFallbackLevel`
- `managementFallbackLevel`
- `adminFallbackLevel`
- `fallbackLevelOverrides`

Levels are clamped to vanilla command levels `0` through `4`. Defaults preserve the behavior from before this permission milestone: public and management commands are available to players, while administration and global protection bypass require administrator level.
