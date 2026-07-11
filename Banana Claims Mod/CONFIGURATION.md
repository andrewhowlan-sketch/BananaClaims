# Banana Claims Configuration

Banana Claims uses two server configuration files and one claim-data file.

## Main Configuration

Path: `config/bananaclaims.json`

```json
{
  "configVersion": 2,
  "permissions": {
    "fabricPermissionsApiEnabled": true,
    "publicFallbackLevel": 0,
    "managementFallbackLevel": 0,
    "adminFallbackLevel": 3,
    "fallbackLevelOverrides": {}
  },
  "protection": {
    "denialMessagesEnabled": true,
    "denialMessageCooldownTicks": 20
  },
  "invitations": {
    "enabled": true,
    "expirationSeconds": 300,
    "maxPendingPerClaim": 20,
    "notifyOnExpiration": true
  }
}
```

### Invitations

- `enabled`: Enables invite creation, acceptance, denial, and cancellation.
- `expirationSeconds`: Lifetime of a pending invitation; valid range 30-86400.
- `maxPendingPerClaim`: Maximum active outgoing invitations per claim; valid range 1-100.
- `notifyOnExpiration`: Sends an expiration message to an online invitee.

Invitations are session-based and intentionally clear on restart.

Reload with `/claim admin reload config`.

## Preview Configuration

Path: `config/bananaclaims-preview.json`

Controls duration, view range, materials, terrain following, corners, guide columns, glow, fade, and pulse. Reload with `/claim admin reload preview`.

## Claim Data

Path: `config/bananaclaims/claims.json`

This file stores claims, roles, flags, notifications, and per-claim BlueMap appearance. BlueMap style fields include fill color, line color, opacity, and line width. Missing style data automatically receives safe defaults.

Do not edit claim data while the server is running. Invalid administrative reloads preserve the current in-memory claims. Invalid startup data stops initialization rather than risking an empty overwrite.

## Full Reload

`/claim admin reload` reloads the main config, claims, and preview config.
