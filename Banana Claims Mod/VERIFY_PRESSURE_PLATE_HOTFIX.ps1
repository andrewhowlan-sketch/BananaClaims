$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$normal = Join-Path $root "src/main/java/com/bananasandwich/bananaclaims/mixin/PressurePlateEntityFilterMixin.java"
$weighted = Join-Path $root "src/main/java/com/bananasandwich/bananaclaims/mixin/WeightedPressurePlateEntityFilterMixin.java"
$filter = Join-Path $root "src/main/java/com/bananasandwich/bananaclaims/protection/PressurePlateEntityFilter.java"
$config = Join-Path $root "src/main/resources/bananaclaims.mixins.json"

$required = @($normal, $weighted, $filter, $config)
foreach ($path in $required) {
    if (-not (Test-Path $path)) {
        throw "Missing required hotfix file: $path"
    }
}

$normalText = Get-Content $normal -Raw
$weightedText = Get-Content $weighted -Raw
$configText = Get-Content $config -Raw

if ($normalText -notmatch "PressurePlateBlock;getEntityCount") {
    throw "Normal pressure-plate redirect target was not installed."
}
if ($normalText -match "BasePressurePlateBlock;getEntityCount") {
    throw "Old broken BasePressurePlateBlock redirect target is still present."
}
if ($weightedText -notmatch "WeightedPressurePlateBlock;getEntityCount") {
    throw "Weighted pressure-plate redirect target was not installed."
}
if ($configText -notmatch '"WeightedPressurePlateEntityFilterMixin"') {
    throw "Weighted pressure-plate mixin is not registered."
}

Write-Host "SUCCESS: Pressure-plate startup hotfix files are present and verified."
