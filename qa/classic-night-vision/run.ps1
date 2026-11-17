param(
    [Parameter(Mandatory)][ValidatePattern('^[A-Za-z0-9-]+$')][string]$RunName,
    [Parameter(Mandatory)][string]$ModJar,
    [string]$JavaHome = 'C:/Program Files/Java/jdk-21.0.11',
    [switch]$NoEarlyWindow,
    [switch]$Dedicated
)

$ErrorActionPreference = 'Stop'
$project = (Resolve-Path "$PSScriptRoot/../..").Path
$artifact = (Resolve-Path -LiteralPath $ModJar).Path
$fixture = Join-Path $project 'build/legacy-functional-qa'
$run = Join-Path $project "build/classic-night-vision-qa/$RunName"
$classes = Join-Path $run 'classes'
$game = Join-Path $run 'game'
if (Test-Path -LiteralPath $run) {
    throw "Run already exists: $run"
}

New-Item -ItemType Directory -Path $game,$classes | Out-Null
Copy-Item -Path "$fixture/template/*" -Destination $game -Recurse
Copy-Item -LiteralPath $artifact -Destination "$game/mods/crysis-legacy-port.jar"
Copy-Item -LiteralPath "$PSScriptRoot/META-INF" -Destination $classes -Recurse
if ($Dedicated) {
    $manifest = "$classes/META-INF/neoforge.mods.toml"
    (Get-Content -Raw -LiteralPath $manifest).Replace('crysisclassicnightvisionqa', 'crysisclassicnightvisionserverqa') |
        Set-Content -LiteralPath $manifest -Encoding utf8NoBOM
    'eula=true' | Set-Content -LiteralPath "$game/eula.txt"
    @('server-ip=127.0.0.1', 'server-port=0', 'online-mode=false', 'view-distance=3', 'simulation-distance=3') |
        Set-Content -LiteralPath "$game/server.properties"
}
if ($NoEarlyWindow) {
    $config = Join-Path $game 'config'
    New-Item -ItemType Directory -Path $config -Force | Out-Null
    $fmlConfig = Join-Path $config 'fml.toml'
    $lines = if (Test-Path -LiteralPath $fmlConfig) {
        Get-Content -LiteralPath $fmlConfig | Where-Object { $_ -notmatch '^\s*earlyWindowControl\s*=' }
    }
    @($lines) + 'earlyWindowControl = false' | Set-Content -LiteralPath $fmlConfig -Encoding utf8NoBOM
}

$classpath = (Get-Content -LiteralPath "$fixture/classpath.txt") -join ';'
$merged = Join-Path $project 'build/moddev/artifacts/neoforge-21.1.265-merged.jar'
$probe = if ($Dedicated) { 'ClassicNightVisionServerQa.java' } else { 'ClassicNightVisionQa.java' }
& "$JavaHome/bin/javac.exe" -proc:none -encoding UTF-8 -sourcepath $classes -classpath "$artifact;$classpath;$merged" -d $classes "$PSScriptRoot/$probe"
if ($LASTEXITCODE) { throw 'Classic night vision QA compilation failed' }
& "$JavaHome/bin/jar.exe" --create --file "$game/mods/crysis-classic-night-vision-qa.jar" -C $classes .
if ($LASTEXITCODE) { throw 'Classic night vision QA packaging failed' }

$role = if ($Dedicated) { 'server' } else { 'client' }
$vm = Join-Path $project "build/moddev/${role}RunVmArgs.txt"
$program = if ($Dedicated) { Join-Path $project 'build/moddev/serverRunProgramArgs.txt' } else { Join-Path $fixture 'program.txt' }
$prefix = if ($Dedicated) { 'CLASSIC_NIGHT_VISION_SERVER_QA_' } else { 'CLASSIC_NIGHT_VISION_QA_' }
$log = Join-Path $run 'console.log'
"INPUT_JAR_SHA256 $((Get-FileHash -LiteralPath $artifact).Hash)" | Set-Content -LiteralPath "$run/input.txt"
Push-Location -LiteralPath $game
try {
    & "$JavaHome/bin/java.exe" '-Xmx3G' '-Dfile.encoding=UTF-8' "@$vm" -cp $classpath net.neoforged.devlaunch.Main "@$program" > $log 2>&1
    if ($LASTEXITCODE) { throw "Minecraft exited with $LASTEXITCODE. See $log" }
    $proof = @(Select-String -LiteralPath $log -Pattern "^$prefix")
    $proof | ForEach-Object Line
    if (($proof | Where-Object Line -ceq "${prefix}COMPLETE failures=0").Count -ne 1) {
        throw "Incomplete or failed classic night vision test. See $log"
    }
} finally {
    Pop-Location
}
