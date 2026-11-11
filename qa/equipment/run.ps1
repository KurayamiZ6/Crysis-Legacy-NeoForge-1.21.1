param(
    [Parameter(Mandatory)][ValidatePattern('^[A-Za-z0-9-]+$')][string]$RunName,
    [Parameter(Mandatory)][string]$ModJar,
    [string]$OldWorld = 'build/transition-lifecycle-qa/legacy-checked/game/saves/Cloak Integration Test',
    [string]$JavaHome = 'C:/Program Files/Java/jdk-21.0.11',
    [switch]$RenderOnly
)

$ErrorActionPreference = 'Stop'
$project = (Resolve-Path "$PSScriptRoot/../..").Path
$artifact = (Resolve-Path -LiteralPath $ModJar).Path
$legacyWorld = (Resolve-Path -LiteralPath $OldWorld).Path
$fixture = Join-Path $project 'build/legacy-functional-qa'
$run = Join-Path $project "build/equipment-qa/$RunName"
$game = Join-Path $run 'game'
$classes = Join-Path $run 'classes'
if (Test-Path -LiteralPath $run) {
    throw "Run already exists: $run"
}

New-Item -ItemType Directory -Path "$game/mods", "$game/saves", $classes | Out-Null
Copy-Item -LiteralPath $legacyWorld -Destination "$game/saves/Cloak Integration Test" -Recurse
Copy-Item -LiteralPath $artifact -Destination "$game/mods/crysis-legacy-port.jar"
Copy-Item -LiteralPath "$PSScriptRoot/META-INF" -Destination $classes -Recurse
Get-Content -LiteralPath "$fixture/template/options.txt" |
    ForEach-Object { $_ -replace '^resourcePacks:.*$', 'resourcePacks:[]' } |
    Set-Content -LiteralPath "$game/options.txt" -Encoding utf8NoBOM

$classpath = (Get-Content -LiteralPath "$fixture/classpath.txt") -join ';'
$merged = Join-Path $project 'build/moddev/artifacts/neoforge-21.1.265-merged.jar'
& "$JavaHome/bin/javac.exe" -proc:none -encoding UTF-8 -sourcepath $classes -classpath "$artifact;$classpath;$merged" -d $classes "$PSScriptRoot/EquipmentQa.java"
if ($LASTEXITCODE) { throw 'Equipment QA compilation failed' }
& "$JavaHome/bin/jar.exe" --create --file "$game/mods/crysis-equipment-qa.jar" -C $classes .
if ($LASTEXITCODE) { throw 'Equipment QA packaging failed' }

$vm = Join-Path $project 'build/moddev/clientRunVmArgs.txt'
$program = Join-Path $fixture 'program.txt'
$proof = "INPUT_JAR_SHA256 $((Get-FileHash -LiteralPath $artifact).Hash)", "COPIED_OLD_WORLD $legacyWorld"
$proof | Set-Content -LiteralPath "$run/inputs.txt"
$proof
Push-Location -LiteralPath $game
try {
    $phases = if ($RenderOnly) { 'captures' } else { 'play', 'reopen' }
    foreach ($phase in $phases) {
        $log = Join-Path $run "$phase.log"
        & "$JavaHome/bin/java.exe" '-Xmx3G' '-Dfile.encoding=UTF-8' "-Dcrysis.equipment.phase=$phase" "@$vm" -cp $classpath net.neoforged.devlaunch.Main "@$program" > $log 2>&1
        if ($LASTEXITCODE) { throw "Minecraft exited with $LASTEXITCODE in phase $phase. See $log" }
        $summary = "EQUIPMENT_QA_$($phase.ToUpperInvariant())_COMPLETE failures="
        $result = @(Select-String -LiteralPath $log -SimpleMatch $summary)
        if ($result.Count -ne 1 -or $result[0].Line -ne ($summary + '0')) {
            throw "Incomplete or failed equipment phase $phase. See $log"
        }
        $result[0].Line
    }
    foreach ($generation in 1, 2) {
        foreach ($view in 'first-person', 'third-person') {
            $image = "$game/screenshots/nanosuit-$generation-$view.png"
            if (!(Test-Path -LiteralPath $image -PathType Leaf)) { throw "Missing equipment capture: $image" }
        }
    }
    'EQUIPMENT_QA_FOUR_CAPTURES_WRITTEN'
} finally {
    Pop-Location
}
