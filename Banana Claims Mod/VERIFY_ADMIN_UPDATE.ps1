$ErrorActionPreference = "Stop"

$requiredFiles = @(
    ".\src\main\java\com\bananasandwich\bananaclaims\Claim\ClaimFlagDefinition.java",
    ".\src\main\java\com\bananasandwich\bananaclaims\Claim\ClaimManager.java",
    ".\src\main\java\com\bananasandwich\bananaclaims\command\ClaimCommand.java",
    ".\src\main\java\com\bananasandwich\bananaclaims\command\FlagClaimCommand.java",
    ".\src\main\java\com\bananasandwich\bananaclaims\command\admin\AdminClaimCommand.java",
    ".\src\main\java\com\bananasandwich\bananaclaims\command\admin\AdminClaimPermission.java",
    ".\src\main\java\com\bananasandwich\bananaclaims\command\admin\AdminClaimSelector.java",
    ".\src\main\java\com\bananasandwich\bananaclaims\command\admin\ClaimDiagnostics.java",
    ".\src\main\java\com\bananasandwich\bananaclaims\storage\ClaimStorage.java"
)

foreach ($file in $requiredFiles) {
    if (-not (Test-Path $file)) {
        throw "Missing required file: $file"
    }
}

$checks = @(
    @{ File = $requiredFiles[0]; Pattern = "enum ClaimFlagDefinition" },
    @{ File = $requiredFiles[1]; Pattern = "forceTransferOwnership" },
    @{ File = $requiredFiles[1]; Pattern = "removeClaim\(" },
    @{ File = $requiredFiles[2]; Pattern = "AdminClaimCommand\.register" },
    @{ File = $requiredFiles[3]; Pattern = "ClaimFlagDefinition\.canonicalNames" },
    @{ File = $requiredFiles[4]; Pattern = 'Commands\.literal\("admin"\)' },
    @{ File = $requiredFiles[4]; Pattern = 'Commands\.literal\("force-delete"\)' },
    @{ File = $requiredFiles[8]; Pattern = "tryLoadClaims" }
)

foreach ($check in $checks) {
    if (-not (Select-String -Path $check.File -Pattern $check.Pattern -Quiet)) {
        throw "Verification failed for $($check.File): $($check.Pattern)"
    }
}

Write-Host "SUCCESS: Administration and flag completion files are installed." -ForegroundColor Green
