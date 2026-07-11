Banana Claims - Complete Legacy Renderer Retirement v2

This corrected package retires the entire legacy particle-renderer subsystem.
The earlier script listed only some classes, which caused IntelliJ to report
that BoundaryPreview was still used by BoundaryShapeFactory.

REPLACE:
- src/main/java/com/bananasandwich/bananaclaims/Bananaclaims.java
- src/main/java/com/bananasandwich/bananaclaims/command/ClaimCommand.java
- src/main/java/com/bananasandwich/bananaclaims/command/PreviewClaimCommand.java
- src/main/java/com/bananasandwich/bananaclaims/command/SelectionClaimCommand.java

THEN RUN FROM THE PROJECT ROOT:
powershell -ExecutionPolicy Bypass -File .\RETIRE_LEGACY_RENDERER.ps1

The script:
1. Verifies the replacement files no longer reference the old renderer.
2. Detects every Java class currently inside the legacy preview package.
3. Checks that no source outside the retirement set still references them.
4. Creates legacy-renderer-backup-<timestamp>.zip.
5. Deletes the entire legacy preview package and PreviewV2Command.java.
6. Verifies that legacy runtime references are gone.

DO NOT delete BoundaryPreview.java individually in IntelliJ. The package must be
retired together because BoundaryShapeFactory, BoundaryLine, BoundarySurface,
and the particle renderer form one obsolete subsystem.

BUILD:
.\gradlew.bat clean build
