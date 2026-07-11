BANANA CLAIMS FINAL FEATURE BUILD

This package adds the final pre-1.0 feature set:
- Time-limited claim invitations
- Optional server-generated Book GUI
- Per-claim BlueMap colors and opacity
- Book/Invite/BlueMap permission nodes
- Updated configuration, localization, release docs, and staff guide

INSTALL
1. Back up your project and config/bananaclaims directory.
2. Extract this package into the project root.
3. Allow complete replacement files to overwrite.
4. Run VERIFY_FINAL_FEATURE_BUILD.ps1.
5. Run .\gradlew.bat clean build.

The included config/bananaclaims.json is a reference/default project config. Preserve customized production config values and merge the invitations section when necessary.
