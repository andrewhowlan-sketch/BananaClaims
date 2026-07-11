BANANA CLAIMS — PRESSURE-PLATE STARTUP HOTFIX

Cause:
The original redirect targeted BasePressurePlateBlock.getEntityCount. In
Minecraft 26.2, the compiled calls are owned by PressurePlateBlock and
WeightedPressurePlateBlock respectively, so the redirect matched zero targets
and Fabric stopped server startup.

Install:
1. Extract this ZIP into the project root.
2. Allow all four project files to replace/add.
3. Run:
   powershell -ExecutionPolicy Bypass -File .\VERIFY_PRESSURE_PLATE_HOTFIX.ps1
4. Build:
   .\gradlew.bat clean build
5. Replace the server JAR and start the server.

This hotfix preserves pressure-plate protection, including scheduled checks.
