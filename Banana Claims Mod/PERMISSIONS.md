# Banana Claims Permissions

Banana Claims integrates with LuckPerms through Fabric Permissions API v0 when available. Without an external provider, configurable vanilla command-level fallbacks are used. Permission approval does not bypass claim-role checks.

## Public Commands

```text
bananaclaims.command.claim
bananaclaims.command.book
bananaclaims.command.pos1
bananaclaims.command.pos2
bananaclaims.command.create
bananaclaims.command.createarea
bananaclaims.command.preview
bananaclaims.command.invite
bananaclaims.command.leave
bananaclaims.command.info
bananaclaims.command.list
```

## Claim Management

```text
bananaclaims.command.expand
bananaclaims.command.shrink
bananaclaims.command.delete
bananaclaims.command.rename
bananaclaims.command.description
bananaclaims.command.member
bananaclaims.command.subowner
bananaclaims.command.transfer
bananaclaims.command.flag
bananaclaims.command.popup
bananaclaims.command.bluemap
```

## Administration

Broad node:

```text
bananaclaims.command.admin
```

Granular nodes:

```text
bananaclaims.command.admin.list
bananaclaims.command.admin.info
bananaclaims.command.admin.nearest
bananaclaims.command.admin.force-transfer
bananaclaims.command.admin.force-delete
bananaclaims.command.admin.reload
bananaclaims.command.admin.reload.config
bananaclaims.command.admin.reload.claims
bananaclaims.command.admin.reload.preview
bananaclaims.command.admin.diagnostics
```

## Protection Bypass

```text
bananaclaims.protection.bypass
```

## Examples

```text
/lp group default permission set bananaclaims.command.book true
/lp group default permission set bananaclaims.command.invite true
/lp group default permission set bananaclaims.command.bluemap true
/lp group admin permission set bananaclaims.command.admin true
/lp group moderator permission set bananaclaims.command.admin.diagnostics true
```
