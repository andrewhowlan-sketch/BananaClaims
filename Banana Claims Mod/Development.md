# Banana Claims Development

## Project Information

- Project: Banana Claims
- Type: Server-side Fabric mod
- Minecraft: 26.2
- Fabric Loader: 0.19.3+
- Fabric API: 0.154.1+26.2
- Java: 25+

## Development Philosophy

- Large, logical implementation batches
- Complete replacement files and project-structured ZIP delivery
- Clean, reusable architecture
- Vanilla-quality behavior
- Performance-first and event-driven where practical
- Commands and GUI interfaces share domain services
- No milestone advances until the current build passes in-game testing

# Current Status

## Estimated Completion

Approximately **99%** toward Banana Claims 1.0.

All planned feature milestones are implemented. The remaining work is the final release-candidate audit, production-like testing, documentation confirmation, and versioned release packaging.

## Current Milestone

**Banana Claims 1.0 Release Candidate Audit**

Current goals:

- Run the complete release checklist
- Resolve compile or runtime regressions
- Confirm upgrade safety using existing server data
- Verify Book GUI and invitation workflows with multiple players
- Verify BlueMap style migration and updates
- Prepare the final distributable JAR and release tag

# Completed Systems

## Claims

- Single-chunk, rectangular, and irregular claims
- Stable IDs, rename, description, info, list, expand, shrink, and delete
- Chunk-indexed lookup
- Atomic JSON persistence

## Roles, Lifecycle, and Invitations

- Owner, subowner, and member roles
- Role invariants and repair
- Add/remove members
- Promote/demote subowners
- Leave and step-down behavior
- Ownership transfer and administrative force transfer
- Session-based invitations
- Invite, accept, deny, cancel, list, timeout, and expiration
- Duplicate and capacity safeguards
- Invitation cleanup after deletion, transfer, or restart

## Book GUI

- Optional server-generated written-book interface
- `/claim`, `/claim book`, and `/claim book <claim>` entry points
- Claim selection and overview
- Claim information and command shortcuts
- Members, invitations, and subowners
- Protection flags
- Notification appearance and previews
- BlueMap appearance
- Boundary preview controls
- Transfer, leave, and deletion
- Secure player-bound sessions
- Revalidation of permissions and roles on every action
- Confirmation pages for destructive actions

## Notifications

- Enter/leave detection
- Action bar, title, and chat modes
- Custom text, colors, gradients, placeholders, and sounds

## Renderer V2

- Terrain following and irregular outlines
- Selection and automatic previews
- Tree filtering, ground borders, anchors, corner and guide columns
- Underground visibility and occlusion controls
- Packet-only client-isolated displays
- Configurable materials, dimensions, glow, fade, and pulse
- No persistent world entities
- Legacy renderer removed

Preview config version: **4**

## Protection

Protection remains disabled by default.

- Block breaking and placement
- Containers and vehicle containers
- Doors, switches, pressure plates, tools, and utility interactions
- Entity interaction, placement, and player-attributed damage
- PvP
- Cross-boundary explosion filtering
- Owner, subowner, member, and permission bypass
- Denial-message cooldowns

## BlueMap

- Optional runtime integration
- Incremental per-claim updates
- Multiple disconnected outlines and interior-hole handling
- Escaped HTML details
- Owner, subowner, member, description, and flag display
- Per-claim fill color, line color, opacity, and line width
- Automatic migration to safe default styles
- Commands and Book GUI controls

## Administration

- List, lookup, nearest, diagnostics
- Force transfer and confirmed force delete
- Config, claims, preview, and full reload
- Stable selectors and audit logging

## Configuration, Permissions, and Localization

- Validated main and preview configuration managers
- Invitation configuration version 2
- Fabric Permissions API/LuckPerms bridge
- Vanilla fallback levels and exact-node overrides
- Granular command permissions
- Book, invitation, and BlueMap permission nodes
- Global protection bypass
- Server-resolved `en_us.json`

## Documentation

- README
- Book GUI guide
- Command reference
- Flag reference
- Configuration guide
- Permission guide
- Staff guide
- Performance review
- Changelog
- Release checklist

# Current Architecture

## Core Managers

- `ClaimManager`: identity, lookup, roles, lifecycle, persistence events
- `ClaimInvitationManager`: session invitation lifecycle
- `ClaimBookManager`: secure server-generated book sessions
- `ClaimStorage`: atomic claim JSON persistence
- `BananaClaimsConfigManager`: main config and invitations
- `PreviewV2ConfigManager`: preview config
- `ClaimPermissionService`: external permissions and fallbacks
- `ClaimProtectionService`: centralized protection policy
- `DisplayPreviewV2Manager`: packet-only preview lifecycle and animation

## Interface Rule

Commands and the Book GUI must call the same managers and mutation methods. The GUI must not duplicate claim business logic.

# Post-1.0 Roadmap

- Optional richer client GUI
- Per-claim preview themes
- Fire-spread and mob-griefing enforcement
- Localization packs beyond English
- Public API and third-party integration hooks
