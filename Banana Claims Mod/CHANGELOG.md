# Changelog

## 1.0.0 Release Candidate

### Added

- Optional server-generated Book GUI for nearly all player-facing claim management
- Secure player-bound Book GUI sessions and destructive-action confirmation pages
- Session-based invitations with invite, accept, deny, cancel, list, timeout, and automatic lifecycle cleanup
- Invitation controls inside the Book GUI
- Per-claim BlueMap fill color, line color, opacity, and line-width settings
- BlueMap appearance commands and Book GUI presets
- Permission nodes for the Book GUI, invitations, and BlueMap appearance
- Main-config invitation settings
- Staff-facing documentation

### Improved

- Bare `/claim` now opens the management book
- Invitation selectors remain unambiguous when claim names repeat across owners
- Commands and the Book GUI share the same claim mutation services
- BlueMap updates immediately after style changes
- Existing claim data automatically receives default BlueMap styles

### Existing 1.0 Systems

- Claims, roles, lifecycle, notifications, Renderer V2, protection, administration, permissions, localization, BlueMap integration, atomic persistence, and release documentation
