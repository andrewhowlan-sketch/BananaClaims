BANANA CLAIMS — 1.0 POLISH: CONFIGURATION AND PERMISSIONS

INSTALLATION
1. Copy the contents of this package into the Banana Claims project root.
2. Allow Windows to replace the listed existing source files.
3. Run VERIFY_PERMISSIONS_UPDATE.ps1 from the project root.
4. Build with: .\gradlew.bat build
5. Replace the server JAR and start the server.

MAIN CONFIG
The mod creates:
  config/bananaclaims.json

It controls:
- Optional Fabric Permissions API integration
- Vanilla fallback command levels for public, management, and admin nodes
- Exact-node fallback overrides
- Protection denial-message enable/disable
- Protection denial-message cooldown

RELOAD
  /claim admin reload config

PERMISSION DESIGN
- No hard dependency on Fabric Permissions API or LuckPerms was added.
- When the API is available and enabled, Banana Claims uses its permission checks.
- Otherwise, configured vanilla command-level fallbacks are used.
- Default fallback levels preserve existing behavior:
  public = 0
  management = 0
  admin = 3

See PERMISSIONS.md for the complete canonical node list.

LOCALIZATION FOUNDATION
The first language file is included at:
  src/main/resources/assets/bananaclaims/lang/en_us.json

This batch localizes permission-denied and protection-denial messages and creates the foundation for the full localization pass.
