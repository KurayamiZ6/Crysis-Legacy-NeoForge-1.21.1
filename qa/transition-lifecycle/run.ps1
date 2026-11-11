param(
    [Parameter(Mandatory)][ValidatePattern('^[A-Za-z0-9-]+$')][string]$RunName,
    [Parameter(Mandatory)][string]$ModJar,
    [switch]$UseTransitionOverlay,
    [string]$JavaHome = 'C:/Program Files/Java/jdk-21.0.11'
)

$ErrorActionPreference = 'Stop'
$project = (Resolve-Path "$PSScriptRoot/../..").Path
$artifact = (Resolve-Path -LiteralPath $ModJar).Path
$fixture = Join-Path $project 'build/legacy-functional-qa'
$run = Join-Path $project "build/transition-lifecycle-qa/$RunName"
$game = Join-Path $run 'game'
$classes = Join-Path $run 'classes'
if (Test-Path -LiteralPath $run) {
    throw "Run already exists: $run"
}

New-Item -ItemType Directory -Path $game, $classes | Out-Null
Copy-Item -Path "$fixture/template/*" -Destination $game -Recurse
Copy-Item -LiteralPath $artifact -Destination "$game/mods/crysis-legacy-port.jar"
Copy-Item -LiteralPath "$PSScriptRoot/META-INF" -Destination $classes -Recurse

$classpath = (Get-Content -LiteralPath "$fixture/classpath.txt") -join ';'
$merged = Join-Path $project 'build/moddev/artifacts/neoforge-21.1.265-merged.jar'
& "$JavaHome/bin/javac.exe" -proc:none -encoding UTF-8 -sourcepath $classes -classpath "$artifact;$classpath;$merged" -d $classes "$PSScriptRoot/LifecycleQa.java"
if ($LASTEXITCODE) { exit $LASTEXITCODE }
& "$JavaHome/bin/jar.exe" --create --file "$game/mods/crysis-transition-lifecycle-qa.jar" -C $classes .
if ($LASTEXITCODE) { exit $LASTEXITCODE }

$vm = Join-Path $project 'build/moddev/clientRunVmArgs.txt'
$program = Join-Path $fixture 'program.txt'
$overlayFlag = "-Dcrysis.transition.qa=$($UseTransitionOverlay.ToString().ToLowerInvariant())"
Write-Output "ARTIFACT_SHA256 $((Get-FileHash -LiteralPath $artifact).Hash)"
Push-Location -LiteralPath $game
try {
    foreach ($phase in 'prepare', 'reopen') {
        $log = Join-Path $run "$phase.log"
        & "$JavaHome/bin/java.exe" '-Xmx3G' '-Dfile.encoding=UTF-8' $overlayFlag "-Dcrysis.lifecycle.phase=$phase" "@$vm" -cp $classpath net.neoforged.devlaunch.Main "@$program" > $log 2>&1
        if ($LASTEXITCODE) { throw "Minecraft exited with $LASTEXITCODE in phase $phase. See $log" }
        $summary = "LIFECYCLE_QA_$($phase.ToUpperInvariant())_COMPLETE failures="
        $result = @(Select-String -LiteralPath $log -SimpleMatch $summary)
        if ($result.Count -ne 1 -or $result[0].Line -ne ($summary + '0')) {
            throw "Incomplete or failed lifecycle phase $phase. See $log"
        }
        $result[0].Line
    }
    $logout = Select-String -LiteralPath "$run/prepare.log" -Pattern '^LIFECYCLE_QA_LOGOUT (.*)$'
    $login = Select-String -LiteralPath "$run/reopen.log" -Pattern '^LIFECYCLE_QA_REOPEN_PERSISTED_LOCK_PASS (.*)$'
    if (!$logout -or !$login -or $logout.Matches[0].Groups[1].Value -ne $login.Matches[0].Groups[1].Value) {
        throw "Recovery state differs between logout and login. See $run"
    }
    "LIFECYCLE_QA_DISK_ROUNDTRIP_PASS " + $login.Matches[0].Groups[1].Value
} finally {
    Pop-Location
}
