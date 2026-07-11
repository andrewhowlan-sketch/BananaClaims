# Banana Claims Commands

Arguments in `<angle brackets>` are required. Arguments in `[square brackets]` are optional. Tab completion is available for claims, players, flags, booleans, sounds, modes, and invitation selectors.

## Book GUI

| Command | Purpose |
|---|---|
| `/claim` | Opens the optional Banana Claims management book. |
| `/claim book` | Opens the management book. |
| `/claim book <claim>` | Opens the book with a participating claim selected. |

The book provides clickable pages for claim information, members, invitations, subowners, protection flags, notifications, BlueMap appearance, previews, transfer, leave, and deletion. Text-entry actions open a prefilled command because vanilla books do not contain text fields.

## Claims

| Command | Purpose |
|---|---|
| `/claim create <name>` | Creates a single-chunk claim in the current chunk. |
| `/claim pos1` | Sets selection corner one. |
| `/claim pos2` | Sets selection corner two and previews a valid selection. |
| `/claim createarea <name>` | Creates a rectangular multi-chunk claim from the selection. |
| `/claim info [claim]` | Shows claim information. |
| `/claim list` | Lists claims owned by the player. |
| `/claim description <text>` | Updates the claim at the player's position. |
| `/claim description <claim> <text>` | Updates a managed claim remotely. |
| `/claim rename <newName>` | Renames the claim at the player's position. |
| `/claim rename <claim> <newName>` | Renames a managed claim remotely. |
| `/claim delete [claim]` | Deletes an owned claim. |
| `/claim expand <claim>` | Adds the current unclaimed chunk. |
| `/claim shrink <claim>` | Removes the current chunk. |
| `/claim leave [claim]` | Leaves a claim; a subowner steps down to member first. |
| `/claim transfer [claim] <player>` | Transfers ownership. |

## Invitations

```text
/claim invite <player> [claim]
/claim invite for <claim> <player>
/claim invite accept <claim@inviter>
/claim invite deny <claim@inviter>
/claim invite cancel <player> [claim]
/claim invite cancel-for <claim> <player>
/claim invite list
```

Owners and subowners can invite online players. Invitations expire automatically, cannot be duplicated, and are cancelled when a claim is deleted or transferred. Pending invitations clear on server restart.

## Preview

```text
/claim preview
/claim preview <claim>
/claim preview nearest
/claim preview stop
```

Previews are packet-only, client-isolated, and do not create persistent world entities.

## Members

```text
/claim member add <player>
/claim member remove <player>
/claim member list
/claim member <claim> add <player>
/claim member <claim> remove <player>
/claim member <claim> list
```

Owners and subowners can manage regular members.

## Subowners

```text
/claim subowner add <player>
/claim subowner remove <player>
/claim subowner list
/claim subowner <claim> add <player>
/claim subowner <claim> remove <player>
/claim subowner <claim> list
```

Only the owner can add or remove subowners. Removing a subowner demotes them to member.

## Protection Flags

```text
/claim flag <claim> <flag>
/claim flag <claim> <flag> <true|false>
```

The first form displays the current value. The second changes it. See [FLAGS.md](FLAGS.md).

## Notifications

```text
/claim popup <claim> preview enter
/claim popup <claim> preview leave
/claim popup <claim> set mode <ACTIONBAR|TITLE|CHAT>
/claim popup <claim> set enterTitle <text>
/claim popup <claim> set enterSubtitle <text>
/claim popup <claim> set leaveTitle <text>
/claim popup <claim> set leaveSubtitle <text>
/claim popup <claim> set enterSound <sound|none>
/claim popup <claim> set leaveSound <sound|none>
```

## BlueMap Appearance

```text
/claim bluemap
/claim bluemap <claim>
/claim bluemap <claim> fill <#RRGGBB>
/claim bluemap <claim> line <#RRGGBB>
/claim bluemap <claim> fillopacity <0.0-1.0>
/claim bluemap <claim> lineopacity <0.0-1.0>
/claim bluemap <claim> linewidth <1-10>
/claim bluemap <claim> reset
```

These values are stored per claim. BlueMap is optional; style settings remain saved even while BlueMap is absent.

## Administration

```text
/claim admin list [page]
/claim admin info [claim]
/claim admin nearest
/claim admin force-transfer <claim> <player>
/claim admin force-delete <claim> confirm
/claim admin reload
/claim admin reload config
/claim admin reload claims
/claim admin reload preview
/claim admin diagnostics [claim]
```
