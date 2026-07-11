$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$required = @(
  "src/main/java/com/bananasandwich/bananaclaims/book/ClaimBookManager.java",
  "src/main/java/com/bananasandwich/bananaclaims/book/BookPacketSender.java",
  "src/main/java/com/bananasandwich/bananaclaims/invitation/ClaimInvitationManager.java",
  "src/main/java/com/bananasandwich/bananaclaims/command/InviteClaimCommand.java",
  "src/main/java/com/bananasandwich/bananaclaims/command/BlueMapClaimCommand.java",
  "src/main/java/com/bananasandwich/bananaclaims/Claim/ClaimBlueMapStyle.java",
  "src/main/resources/assets/bananaclaims/lang/en_us.json"
)
foreach ($relative in $required) {
  $path = Join-Path $root $relative
  if (-not (Test-Path $path)) { throw "Missing required file: $relative" }
}
$configPath = Join-Path $root "config/bananaclaims.json"
$config = Get-Content $configPath -Raw | ConvertFrom-Json
if ($config.configVersion -ne 2) { throw "Expected bananaclaims.json configVersion 2." }
if ($null -eq $config.invitations) { throw "Invitation settings are missing from bananaclaims.json." }
$langPath = Join-Path $root "src/main/resources/assets/bananaclaims/lang/en_us.json"
$lang = Get-Content $langPath -Raw | ConvertFrom-Json
if ($null -eq $lang.'command.bananaclaims.invite.sent') { throw "Invitation language keys are missing." }
$command = Get-Content (Join-Path $root "src/main/java/com/bananasandwich/bananaclaims/command/ClaimCommand.java") -Raw
if ($command -notmatch 'BookClaimCommand\.register') { throw "Book command is not registered." }
if ($command -notmatch 'InviteClaimCommand\.register') { throw "Invite command is not registered." }
if ($command -notmatch 'BlueMapClaimCommand\.register') { throw "BlueMap command is not registered." }
Write-Host "SUCCESS: Final pre-1.0 feature files are installed and verified." -ForegroundColor Green
