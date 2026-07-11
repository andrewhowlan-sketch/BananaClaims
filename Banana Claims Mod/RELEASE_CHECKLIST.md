# Banana Claims 1.0 Release Checklist

## Build

- [ ] Run `.\gradlew.bat clean build`
- [ ] Confirm Java 25 is used
- [ ] Confirm no mixin target warnings
- [ ] Inspect the release JAR resources and metadata

## Fresh and Upgrade Installation

- [ ] Start with no Banana Claims config directory
- [ ] Confirm main config version 2 and preview config version 4 generate
- [ ] Start with existing claims from the production-like server
- [ ] Confirm role, flag, popup, and BlueMap-style migration
- [ ] Confirm startup without BlueMap or LuckPerms

## Claims and Roles

- [ ] Create, resize, rename, describe, preview, and delete claims
- [ ] Add/remove members and promote/demote subowners
- [ ] Test leave, step-down, and ownership transfer
- [ ] Restart and confirm persistence

## Invitations

- [ ] Invite from command and Book GUI
- [ ] Accept and deny from chat and Book GUI
- [ ] Cancel outgoing invitations
- [ ] Test duplicate, self, participant, limit, and authorization rejection
- [ ] Test expiration and online notification
- [ ] Confirm transfer and deletion cancel pending invitations
- [ ] Confirm restart clears pending invitations
- [ ] Test duplicate claim names using `claim@inviter` selectors

## Book GUI

- [ ] Open with `/claim`, `/claim book`, and `/claim book <claim>`
- [ ] Test owner, subowner, and member page visibility
- [ ] Test claim switching
- [ ] Test flags, popup mode, preview, and BlueMap presets
- [ ] Test member and subowner confirmation pages
- [ ] Test leave and delete confirmations
- [ ] Confirm expired or copied action tokens cannot bypass checks
- [ ] Confirm free-text links prefill usable commands

## Protection

- [ ] Test every active flag with an outsider
- [ ] Test owner, subowner, member, and permission bypass
- [ ] Test normal and weighted pressure plates
- [ ] Test cross-boundary explosions
- [ ] Confirm new claims remain unprotected by default

## Preview

- [ ] Test old, new, irregular, and selection previews
- [ ] Test stop, expiration, dimension change, and disconnect
- [ ] Test two-player visibility isolation
- [ ] Test fade and pulse enabled and disabled

## BlueMap

- [ ] Test without BlueMap installed
- [ ] Test creation, update, transfer, and deletion
- [ ] Test fill/line colors, opacity, width, presets, and reset
- [ ] Confirm style persistence after restart
- [ ] Confirm disconnected claims and holes
- [ ] Confirm HTML escaping

## Permissions and Administration

- [ ] Test Book, invite, and BlueMap nodes
- [ ] Test external permission grants and explicit denies
- [ ] Test vanilla fallback levels
- [ ] Test granular admin nodes and diagnostics
- [ ] Test force transfer, force delete, and reloads

## Release

- [ ] Confirm documentation matches the shipped command tree
- [ ] Confirm staff guide is approved
- [ ] Update version and changelog
- [ ] Create a clean release JAR
- [ ] Tag the release commit
