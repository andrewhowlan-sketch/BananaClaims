$ErrorActionPreference = "Stop"

$projectRoot = (Get-Location).Path
$sourceRoot = Join-Path $projectRoot "src\main\java"
$previewDirectory = Join-Path $sourceRoot "com\bananasandwich\bananaclaims\preview"
$previewV2Command = Join-Path $sourceRoot "com\bananasandwich\bananaclaims\command\PreviewV2Command.java"

if (-not (Test-Path $sourceRoot)) {
    Write-Host "ERROR: Run this script from the Banana Claims project root." -ForegroundColor Red
    Write-Host "Expected to find: $sourceRoot"
    exit 1
}

# The four replacement files must be installed before cleanup. These checks
# prevent the script from deleting legacy sources while production code still
# references them.
$requiredFiles = @(
    (Join-Path $sourceRoot "com\bananasandwich\bananaclaims\Bananaclaims.java"),
    (Join-Path $sourceRoot "com\bananasandwich\bananaclaims\command\ClaimCommand.java"),
    (Join-Path $sourceRoot "com\bananasandwich\bananaclaims\command\PreviewClaimCommand.java"),
    (Join-Path $sourceRoot "com\bananasandwich\bananaclaims\command\SelectionClaimCommand.java")
)

foreach ($requiredFile in $requiredFiles) {
    if (-not (Test-Path $requiredFile)) {
        Write-Host "ERROR: Required source file not found: $requiredFile" -ForegroundColor Red
        exit 1
    }
}

$productionLegacyPattern = "BOUNDARY_PREVIEW_MANAGER|BoundaryPreviewManager|PreviewV2Command\.register"
$productionLegacyReferences = $requiredFiles |
    Select-String -Pattern $productionLegacyPattern

if ($productionLegacyReferences) {
    Write-Host "ERROR: The replacement files have not been installed yet." -ForegroundColor Red
    Write-Host "Legacy runtime references remain:" -ForegroundColor Yellow
    foreach ($match in $productionLegacyReferences) {
        Write-Host "$($match.Path):$($match.LineNumber): $($match.Line.Trim())"
    }
    exit 2
}

$previewFiles = @()
if (Test-Path $previewDirectory) {
    $previewFiles = @(Get-ChildItem -Path $previewDirectory -Recurse -Filter "*.java")
}

$candidatePaths = @($previewFiles.FullName)
if (Test-Path $previewV2Command) {
    $candidatePaths += $previewV2Command
}
$candidatePaths = @($candidatePaths | ForEach-Object {
    [System.IO.Path]::GetFullPath($_)
})

$allJavaFiles = @(Get-ChildItem -Path $sourceRoot -Recurse -Filter "*.java")
$outsideFiles = @($allJavaFiles | Where-Object {
    $fullName = [System.IO.Path]::GetFullPath($_.FullName)
    $candidatePaths -notcontains $fullName
})

# Build a complete list from the actual legacy directory. This avoids the
# earlier problem where BoundaryPreview was kept because BoundaryShapeFactory,
# BoundaryLine, and BoundarySurface were not included in the candidate set.
$legacyClassNames = @($previewFiles | ForEach-Object {
    [System.IO.Path]::GetFileNameWithoutExtension($_.Name)
})

$referencePatterns = @(
    "com\.bananasandwich\.bananaclaims\.preview(?!v2)"
)

foreach ($className in $legacyClassNames) {
    $referencePatterns += "\b" + [regex]::Escape($className) + "\b"
}

if (Test-Path $previewV2Command) {
    $referencePatterns += "\bPreviewV2Command\b"
}

$externalReferences = @()
foreach ($file in $outsideFiles) {
    foreach ($pattern in $referencePatterns) {
        $matches = Select-String -Path $file.FullName -Pattern $pattern
        if ($matches) {
            $externalReferences += $matches
        }
    }
}

if ($externalReferences.Count -gt 0) {
    Write-Host "ERROR: Legacy renderer classes are still referenced outside the retirement set." -ForegroundColor Red
    Write-Host "Nothing was deleted. Remaining references:" -ForegroundColor Yellow
    foreach ($match in ($externalReferences | Sort-Object Path, LineNumber -Unique)) {
        Write-Host "$($match.Path):$($match.LineNumber): $($match.Line.Trim())"
    }
    exit 3
}

if ($candidatePaths.Count -eq 0) {
    Write-Host "SUCCESS: No legacy renderer files remain." -ForegroundColor Green
    exit 0
}

# Create a one-time safety backup before deletion.
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$backupPath = Join-Path $projectRoot "legacy-renderer-backup-$timestamp.zip"
$backupItems = @()
if (Test-Path $previewDirectory) {
    $backupItems += $previewDirectory
}
if (Test-Path $previewV2Command) {
    $backupItems += $previewV2Command
}

if ($backupItems.Count -gt 0) {
    Compress-Archive -Path $backupItems -DestinationPath $backupPath -Force
    Write-Host "BACKUP: $backupPath" -ForegroundColor Cyan
}

if (Test-Path $previewV2Command) {
    Remove-Item -Path $previewV2Command -Force
    Write-Host "REMOVED: $previewV2Command" -ForegroundColor Green
}

if (Test-Path $previewDirectory) {
    Remove-Item -Path $previewDirectory -Recurse -Force
    Write-Host "REMOVED: $previewDirectory" -ForegroundColor Green
}

$remainingLegacyReferences = Get-ChildItem -Path $sourceRoot -Recurse -Filter "*.java" |
    Select-String -Pattern "BOUNDARY_PREVIEW_MANAGER|BoundaryPreviewManager|BoundaryShapeFactory|BoundaryParticleRenderer|PreviewV2Command\.register|com\.bananasandwich\.bananaclaims\.preview(?!v2)"

if ($remainingLegacyReferences) {
    Write-Host "WARNING: Cleanup completed, but legacy references remain:" -ForegroundColor Yellow
    foreach ($match in $remainingLegacyReferences) {
        Write-Host "$($match.Path):$($match.LineNumber): $($match.Line.Trim())"
    }
    exit 4
}

Write-Host "SUCCESS: The complete legacy particle renderer was retired." -ForegroundColor Green
Write-Host "Renderer V2 is now the only preview implementation."
