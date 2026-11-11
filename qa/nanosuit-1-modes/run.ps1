param(
    [Parameter(Mandatory)][ValidatePattern('^[A-Za-z0-9-]+$')][string]$RunName,
    [Parameter(Mandatory)][string]$ModJar,
    [string]$OldWorld = 'build/transition-lifecycle-qa/legacy-checked/game/saves/Cloak Integration Test',
    [string]$JavaHome = 'C:/Program Files/Java/jdk-21.0.11'
)

$ErrorActionPreference = 'Stop'
$project = (Resolve-Path "$PSScriptRoot/../..").Path
$artifact = (Resolve-Path -LiteralPath $ModJar).Path
$legacyWorld = (Resolve-Path -LiteralPath $OldWorld).Path
$fixture = Join-Path $project 'build/legacy-functional-qa'
$run = Join-Path $project "build/nanosuit-1-modes-qa/$RunName"
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
& "$JavaHome/bin/javac.exe" -proc:none -encoding UTF-8 -sourcepath $classes -classpath "$artifact;$classpath;$merged" -d $classes "$PSScriptRoot/Nanosuit1ModesQa.java"
if ($LASTEXITCODE) { throw 'Nanosuit 1 modes QA compilation failed' }
& "$JavaHome/bin/jar.exe" --create --file "$game/mods/crysis-n1-modes-qa.jar" -C $classes .
if ($LASTEXITCODE) { throw 'Nanosuit 1 modes QA packaging failed' }

$vm = Join-Path $project 'build/moddev/clientRunVmArgs.txt'
$program = Join-Path $fixture 'program.txt'
$proof = "INPUT_JAR_SHA256 $((Get-FileHash -LiteralPath $artifact).Hash)", "COPIED_OLD_WORLD $legacyWorld"
$proof | Set-Content -LiteralPath "$run/inputs.txt"
$proof
Push-Location -LiteralPath $game
try {
    foreach ($phase in 'play', 'reopen') {
        $log = Join-Path $run "$phase.log"
        & "$JavaHome/bin/java.exe" '-Xmx3G' '-Dfile.encoding=UTF-8' "-Dcrysis.n1modes.phase=$phase" "@$vm" -cp $classpath net.neoforged.devlaunch.Main "@$program" > $log 2>&1
        if ($LASTEXITCODE) { throw "Minecraft exited with $LASTEXITCODE in phase $phase. See $log" }
        $summary = "N1_MODES_QA_$($phase.ToUpperInvariant())_COMPLETE failures="
        $result = @(Select-String -LiteralPath $log -SimpleMatch $summary)
        if ($result.Count -ne 1 -or $result[0].Line -ne ($summary + '0')) {
            throw "Incomplete or failed Nanosuit 1 modes phase $phase. See $log"
        }
        $result[0].Line
    }
} finally {
    Pop-Location
}
