$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

$ExpectedFiles = @(
    "src/main/java/com/bananasandwich/bananaclaims/Bananaclaims.java",
    "src/main/java/com/bananasandwich/bananaclaims/protection/ClaimProtectionManager.java",
    "src/main/java/com/bananasandwich/bananaclaims/protection/ClaimProtectionService.java",
    "src/main/java/com/bananasandwich/bananaclaims/protection/ProtectionAction.java",
    "src/main/java/com/bananasandwich/bananaclaims/mixin/BasePressurePlateBlockMixin.java",
    "src/main/java/com/bananasandwich/bananaclaims/mixin/BoatItemMixin.java",
    "src/main/java/com/bananasandwich/bananaclaims/mixin/PressurePlateEntityFilterMixin.java",
    "src/main/java/com/bananasandwich/bananaclaims/mixin/ProtectedEntityDamageMixin.java",
    "src/main/java/com/bananasandwich/bananaclaims/mixin/ServerExplosionMixin.java",
    "src/main/resources/bananaclaims.mixins.json"
)

$MissingFiles = @()
foreach ($RelativePath in $ExpectedFiles) {
    $FullPath = Join-Path $ProjectRoot $RelativePath
    if (-not (Test-Path -LiteralPath $FullPath)) {
        $MissingFiles += $RelativePath
    }
}

if ($MissingFiles.Count -gt 0) {
    Write-Host "ERROR: Missing protection update files:" -ForegroundColor Red
    $MissingFiles | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
    exit 1
}

$InitializerPath = Join-Path $ProjectRoot "src/main/java/com/bananasandwich/bananaclaims/Bananaclaims.java"
$Initializer = Get-Content -LiteralPath $InitializerPath -Raw

if ($Initializer -notmatch "CLAIM_PROTECTION_SERVICE" -or
    $Initializer -notmatch "CLAIM_PROTECTION_MANAGER" -or
    $Initializer -notmatch "CLAIM_PROTECTION_MANAGER\.register\(\)") {
    Write-Host "ERROR: Bananaclaims.java does not contain the new protection initialization." -ForegroundColor Red
    exit 1
}

$ManagerPath = Join-Path $ProjectRoot "src/main/java/com/bananasandwich/bananaclaims/protection/ClaimProtectionManager.java"
$Manager = Get-Content -LiteralPath $ManagerPath -Raw

$RequiredManagerMarkers = @(
    "BlockEvents.USE_ITEM_ON",
    "BlockEvents.USE_WITHOUT_ITEM",
    "ItemEvents.USE_ON",
    "UseEntityCallback.EVENT",
    "AttackEntityCallback.EVENT",
    "ServerLivingEntityEvents.ALLOW_DAMAGE"
)

foreach ($Marker in $RequiredManagerMarkers) {
    if ($Manager -notmatch [regex]::Escape($Marker)) {
        Write-Host "ERROR: Missing protection registration: $Marker" -ForegroundColor Red
        exit 1
    }
}

if ($Manager -match "UseBlockCallback") {
    Write-Host "ERROR: The legacy coarse UseBlockCallback hook is still present." -ForegroundColor Red
    exit 1
}

$MixinPath = Join-Path $ProjectRoot "src/main/resources/bananaclaims.mixins.json"
$MixinConfig = Get-Content -LiteralPath $MixinPath -Raw | ConvertFrom-Json
$RequiredMixins = @(
    "BasePressurePlateBlockMixin",
    "BoatItemMixin",
    "PressurePlateEntityFilterMixin",
    "ProtectedEntityDamageMixin",
    "ServerExplosionMixin"
)

foreach ($Mixin in $RequiredMixins) {
    if ($MixinConfig.mixins -notcontains $Mixin) {
        Write-Host "ERROR: Missing mixin registration: $Mixin" -ForegroundColor Red
        exit 1
    }
}

Write-Host "SUCCESS: Banana Claims Protection Expansion files are installed." -ForegroundColor Green
