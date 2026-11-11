param(
    [Parameter(Mandatory)][ValidatePattern('^[A-Za-z0-9-]+$')][string]$RunName,
    [Parameter(Mandatory)][string]$ModJar,
    [string]$JavaHome = 'C:/Program Files/Java/jdk-21.0.11'
)

$ErrorActionPreference = 'Stop'
$project = (Resolve-Path "$PSScriptRoot/../../..").Path
$artifact = (Resolve-Path -LiteralPath $ModJar).Path
$source = Join-Path $project 'run/qa-multi-suit-render'
$fixture = Join-Path $project 'build/legacy-functional-qa'
$game = Join-Path $project "build/equipment-compat-render/$RunName"
$classes = Join-Path $game 'classes'
if (Test-Path -LiteralPath $game) {
    throw "Run already exists: $game"
}

New-Item -ItemType Directory -Path $classes, "$game/mods" | Out-Null
foreach ($name in 'config','resourcepacks','saves','shaderpacks','tacz','options.txt') {
    Copy-Item -LiteralPath (Join-Path $source $name) -Destination $game -Recurse
}
Get-ChildItem -LiteralPath "$source/mods" -File -Filter '*.jar' |
    Where-Object Name -NotMatch '^crysis-' |
    Copy-Item -Destination "$game/mods"
Copy-Item -LiteralPath $artifact -Destination "$game/mods/crysis-legacy-port.jar"
Copy-Item -LiteralPath "$PSScriptRoot/META-INF" -Destination $classes -Recurse

$iris = Join-Path $game 'config/iris.properties'
$settings = (Get-Content -LiteralPath $iris -Raw).Replace('enableShaders=false', 'enableShaders=true')
[IO.File]::WriteAllText($iris, $settings, [Text.UTF8Encoding]::new($false))

$classpath = (Get-Content -LiteralPath "$fixture/classpath.txt") -join ';'
$dependencies = (Get-ChildItem -LiteralPath "$game/mods" -File -Filter '*.jar').FullName -join ';'
$merged = Join-Path $project 'build/moddev/artifacts/neoforge-21.1.265-merged.jar'
& "$JavaHome/bin/javac.exe" -proc:none -encoding UTF-8 -sourcepath $classes -classpath "$dependencies;$classpath;$merged" -d $classes "$PSScriptRoot/EquipmentRenderQa.java"
if ($LASTEXITCODE) { throw 'Equipment render QA compilation failed' }
& "$JavaHome/bin/jar.exe" --create --file "$game/mods/crysis-equipment-render-qa.jar" -C $classes .
if ($LASTEXITCODE) { throw 'Equipment render QA packaging failed' }

"INPUT_JAR_SHA256 $((Get-FileHash -LiteralPath $artifact).Hash)" | Set-Content -LiteralPath "$game/input-jar.txt"
$vm = Join-Path $project 'build/moddev/clientRunVmArgs.txt'
$program = Join-Path $fixture 'program.txt'
Push-Location -LiteralPath $game
try {
    & "$JavaHome/bin/java.exe" '-Xmx3G' '-Dfile.encoding=UTF-8' "@$vm" '-cp' $classpath net.neoforged.devlaunch.Main "@$program" > "$game/console.log" 2>&1
    if ($LASTEXITCODE) { throw "Minecraft exited with $LASTEXITCODE. See $game/console.log" }
    $summary = Select-String -LiteralPath "$game/console.log" -Pattern '^EQUIPMENT_RENDER_QA_COMPLETE captures=16 failures=0$'
    if (!$summary -or @(Get-ChildItem -LiteralPath "$game/screenshots" -Filter '*.png').Count -ne 16) {
        throw "Incomplete or failed equipment render QA. See $game/console.log"
    }
    $summary.Line
} finally {
    Pop-Location
}
