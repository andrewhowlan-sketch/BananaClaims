BANANA CLAIMS — ADMINISTRATION AND FLAG TAB COMPLETION

Install
=======
Extract the contents of this folder into the Banana Claims project root and
allow Windows to replace existing files.

Administrative Commands
=======================
/claim admin
/claim admin list [page]
/claim admin info [claim]
/claim admin nearest
/claim admin force-transfer <claim> <player>
/claim admin force-delete <claim> confirm
/claim admin reload
/claim admin reload claims
/claim admin reload preview
/claim admin diagnostics [claim]

Admin Claim Selectors
=====================
A globally unique claim name can be entered normally.

When multiple owners use the same claim name, tab completion returns:

    claimName@ownerName

A full claim UUID is also accepted.

Permissions
===========
The /claim admin branch requires Minecraft's administrator command permission
level. Unauthorized players do not receive the admin branch in tab completion.
Granular Banana Claims permission nodes remain part of the later 1.0
permissions milestone.

Flag Completion
===============
/claim flag <claim> now tab-completes:

- Managed claim names
- Canonical flag names
- true / false

Common historical aliases such as blockbreak, blockplace, container, entity,
and explosion remain accepted, but completion uses canonical names.

Safety
======
- Force delete requires the final literal: confirm
- Administrative transfer preserves owner/member/subowner invariants
- Administrative transfer refuses same-name ownership collisions
- Reloading malformed claim data keeps the current in-memory claims active
- Every destructive administrative action is written to the server log
- Owners are notified when online after a forced transfer or deletion

Build
=====
.\gradlew.bat build
