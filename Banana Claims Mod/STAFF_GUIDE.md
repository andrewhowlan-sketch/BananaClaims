# Banana Claims Staff Guide

## What Banana Claims Does

Banana Claims is the land, settlement, membership, notification, protection, BlueMap, and claim-administration system for the server. Players can use commands or the optional written-book interface.

## Staff Principles

- Protection is off by default; a claim is not automatically a locked private zone.
- Owners control ownership-only actions. Subowners handle normal day-to-day membership and settings.
- Members bypass enabled protections in their claims.
- Staff should prefer normal player workflows unless recovery or enforcement requires an admin command.
- Back up `config/bananaclaims/` before manual data work.

## Common Player Workflow

1. Create a one-chunk claim or select an area.
2. Open `/claim` to manage it in the book.
3. Invite players or add roles.
4. Enable only the desired protection flags.
5. Configure notifications, preview, and BlueMap appearance.

## Important Staff Commands

```text
/claim admin list [page]
/claim admin info [claim]
/claim admin nearest
/claim admin diagnostics [claim]
/claim admin force-transfer <claim> <player>
/claim admin force-delete <claim> confirm
/claim admin reload config
/claim admin reload claims
/claim admin reload preview
```

## Protection Flags

- `breakblocks`: prevents outsiders from breaking blocks.
- `placeblocks`: prevents outsiders from placing blocks.
- `containers`: protects block and vehicle containers.
- `interact`: protects doors, buttons, levers, pressure plates, tools, and utility interactions.
- `entities`: protects entity interaction, placement, and player-attributed damage.
- `pvp`: prevents outsider PvP against players inside the claim.
- `explosions`: filters explosion damage inside the claim.

## Invitations

Owners and subowners can invite online players. Invitations expire, cannot be duplicated, and clear on restart. Accepted players become regular members. Invitations are cancelled when ownership transfers or a claim is deleted.

## Book GUI

`/claim` opens the book. It is a convenience layer over the same services used by commands. It includes claim information, roles, invitations, protection, notification appearance, preview controls, BlueMap appearance, and ownership actions. Destructive actions require confirmation.

## BlueMap

Each claim can choose its fill and outline colors, opacities, and line width. Staff can reset a claim to the server-safe defaults with `/claim bluemap <claim> reset`. BlueMap remains optional and Banana Claims still starts without it.

## Troubleshooting

- Run `/claim admin diagnostics` for global integrity checks.
- Run `/claim admin diagnostics <claim>` for one claim.
- Use `/claim admin reload preview` after preview-config edits.
- Use `/claim admin reload config` after main-config edits.
- Do not reload invalid claim JSON; restore the last backup first.
- If a claim must be recovered, use force transfer rather than editing UUIDs manually.
