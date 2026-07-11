$ErrorActionPreference = "Stop"

$requiredFiles = @(
    ".\src\main\java\com\bananasandwich\bananaclaims\Bananaclaims.java",
    ".\src\main\java\com\bananasandwich\bananaclaims\command\ClaimCommand.java",
    ".\src\main\java\com\bananasandwich\bananaclaims\command\admin\AdminClaimCommand.java",
    ".\src\main\java\com\bananasandwich\bananaclaims\command\admin\AdminClaimPermission.java",
    ".\src\main\java\com\bananasandwich\bananaclaims\config\BananaClaimsConfig.java",
    ".\src\main\java\com\bananasandwich\bananaclaims\config\BananaClaimsConfigManager.java",
    ".\src\main\java\com\bananasandwich\bananaclaims\permission\ClaimPermission.java",
    ".\src\main\java\com\bananasandwich\bananaclaims\permission\ClaimPermissionService.java",
    ".\src\main\java\com\bananasandwich\bananaclaims\permission\FabricPermissionsBridge.java",
    ".\src\main\java\com\bananasandwich\bananaclaims\protection\ClaimProtectionManager.java",
    ".\src\main\java\com\bananasandwich\bananaclaims\protection\ClaimProtectionService.java",
    ".\src\main\java\com\bananasandwich\bananaclaims\protection\ProtectionAction.java",
    ".\src\main\resources\assets\bananaclaims\lang\en_us.json",
    ".\src\main\resources\fabric.mod.json"
)

foreach ($file in $requiredFiles) {
    if (-not (Test-Path $file)) {
        throw "Missing required file: $file"
    }
}

$checks = @(
    @{ Path = ".\src\main\java\com\bananasandwich\bananaclaims\Bananaclaims.java"; Pattern = "CONFIG_MANAGER" },
    @{ Path = ".\src\main\java\com\bananasandwich\bananaclaims\Bananaclaims.java"; Pattern = "PERMISSION_SERVICE" },
    @{ Path = ".\src\main\java\com\bananasandwich\bananaclaims\permission\ClaimPermission.java"; Pattern = "bananaclaims.protection.bypass" },
    @{ Path = ".\src\main\java\com\bananasandwich\bananaclaims\command\admin\AdminClaimCommand.java"; Pattern = 'Commands.literal("config")' },
    @{ Path = ".\src\main\resources\assets\bananaclaims\lang\en_us.json"; Pattern = "command.bananaclaims.permission_denied" },
    @{ Path = ".\src\main\resources\fabric.mod.json"; Pattern = "fabric-permissions-api-v0" }
)

foreach ($check in $checks) {
    if (-not (Select-String -Path $check.Path -SimpleMatch -Pattern $check.Pattern -Quiet)) {
        throw "Verification marker '$($check.Pattern)' was not found in $($check.Path)"
    }
}

$legacyAdminCheck = Select-String `
    -Path ".\src\main\java\com\bananasandwich\bananaclaims\command\admin\AdminClaimPermission.java" `
    -SimpleMatch `
    -Pattern "Permissions.COMMANDS_ADMIN" `
    -Quiet

if ($legacyAdminCheck) {
    throw "AdminClaimPermission.java still contains the old hard-coded Permissions.COMMANDS_ADMIN check."
}

Write-Host "SUCCESS: Banana Claims configuration and permissions files are installed." -ForegroundColor Green
