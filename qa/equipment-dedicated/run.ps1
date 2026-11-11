param(
    [Parameter(Mandatory)][ValidatePattern('^[A-Za-z0-9-]+$')][string]$RunName,
    [Parameter(Mandatory)][string]$ModJar,
    [string]$JavaHome = 'C:/Program Files/Java/jdk-21.0.11',
    [ValidateRange(1024,65535)][int]$Port = 25579,
    [switch]$Smoke
)

$ErrorActionPreference = 'Stop'
$project = (Resolve-Path -LiteralPath "$PSScriptRoot/../..").Path
$artifact = (Resolve-Path -LiteralPath $ModJar).Path
$fixture = Join-Path $project 'build/legacy-functional-qa'
$moddev = Join-Path $project 'build/moddev'
$run = Join-Path $project "build/equipment-dedicated-qa/$RunName"
if (Test-Path -LiteralPath $run) { throw "Run already exists: $run" }

$server = Join-Path $run 'server'
$owner = Join-Path $run 'owner'
$observer = Join-Path $run 'observer'
$classes = Join-Path $run 'classes'
$java = Join-Path $JavaHome 'bin/java.exe'
$cp = (Get-Content -LiteralPath "$fixture/classpath.txt") -join ';'
$merged = Join-Path $moddev 'artifacts/neoforge-21.1.265-merged.jar'
$processes = [Collections.Generic.List[Diagnostics.Process]]::new()

New-Item -ItemType Directory -Path $server,$owner,$observer,$classes | Out-Null
if (!$Smoke) {
    Copy-Item -LiteralPath "$PSScriptRoot/META-INF" -Destination $classes -Recurse
    & "$JavaHome/bin/javac.exe" -proc:none -encoding UTF-8 -sourcepath $classes -cp "$artifact;$cp;$merged" -d $classes "$PSScriptRoot/EquipmentDedicatedQa.java"
    if ($LASTEXITCODE) { throw 'Dedicated fixture did not compile' }
    & "$JavaHome/bin/jar.exe" --create --file "$run/crysis-equipment-dedicated-qa.jar" -C $classes .
    if ($LASTEXITCODE) { throw 'Dedicated fixture could not be packaged' }
}

foreach ($directory in $server,$owner,$observer) {
    New-Item -ItemType Directory -Path "$directory/mods" | Out-Null
    Copy-Item -LiteralPath $artifact -Destination "$directory/mods/crysis-legacy-port.jar"
    if (!$Smoke) {
        Copy-Item -LiteralPath "$run/crysis-equipment-dedicated-qa.jar" -Destination "$directory/mods"
    }
}

Copy-Item -LiteralPath "$fixture/template/saves/Cloak Integration Test" -Destination "$server/world" -Recurse
foreach ($directory in $owner,$observer) {
    $options = @(Get-Content -LiteralPath "$fixture/template/options.txt" |
        Where-Object { $_ -notmatch '^(pauseOnLostFocus|maxFps|renderDistance):' })
    $options + @('pauseOnLostFocus:false','maxFps:45','renderDistance:4') |
        Set-Content -LiteralPath "$directory/options.txt" -Encoding utf8NoBOM
}
'eula=true' | Set-Content -LiteralPath "$server/eula.txt" -Encoding ascii
@(
    'server-ip=127.0.0.1'
    "server-port=$Port"
    'online-mode=false'
    'enforce-secure-profile=false'
    'max-players=2'
    'spawn-protection=0'
    'view-distance=4'
    'simulation-distance=4'
) | Set-Content -LiteralPath "$server/server.properties" -Encoding ascii
Get-FileHash -LiteralPath $artifact -Algorithm SHA256 | Format-List | Out-File "$run/input-jar.txt"

function Start-Fixture([string]$Directory, [string]$Side, [string]$UserName = '') {
    $vm = Get-Content -LiteralPath "$moddev/${Side}RunVmArgs.txt"
    if ($Side -eq 'server') {
        $program = Get-Content -LiteralPath "$moddev/serverRunProgramArgs.txt"
    } else {
        $program = Get-Content -LiteralPath "$fixture/program.txt" | Select-Object -SkipLast 2
        $program += @('--username', $UserName, '--quickPlayMultiplayer', "127.0.0.1:$Port", '--width', '854', '--height', '480')
    }
    $argFile = Join-Path $Directory 'java.args'
    @('-Xmx2G', '-Dfile.encoding=UTF-8', '-Dcrysis.equipment.dedicated.qa=true') +
        $vm + @('-cp', ('"' + $cp.Replace('\', '/') + '"'), 'net.neoforged.devlaunch.Main') + $program |
        Set-Content -LiteralPath $argFile -Encoding utf8NoBOM
    $process = Start-Process -FilePath $java -ArgumentList ('"@' + $argFile + '"') -WorkingDirectory $Directory `
        -WindowStyle Hidden -PassThru -RedirectStandardOutput "$Directory/console.log" -RedirectStandardError "$Directory/stderr.log"
    $processes.Add($process)
    return $process
}

function Assert-Completed([string]$Directory, [string]$Marker) {
    $log = Join-Path $Directory 'console.log'
    if (!(Select-String -Quiet -LiteralPath $log -SimpleMatch $Marker)) {
        throw "Expected $Marker in $log"
    }
    Select-String -LiteralPath $log -Pattern '^EQUIPMENT_DEDICATED_QA_' | ForEach-Object Line
}

try {
    $dedicated = Start-Fixture $server 'server'
    $deadline = (Get-Date).AddSeconds(80)
    while (!(Select-String -Quiet -LiteralPath "$server/console.log" -SimpleMatch 'For help, type "help"')) {
        if ($dedicated.HasExited -or (Get-Date) -ge $deadline) {
            throw "Dedicated server did not start. See $server/console.log and stderr.log"
        }
        Start-Sleep -Milliseconds 500
    }
    Write-Output 'EQUIPMENT_DEDICATED_QA_LISTENER_READY'
    if ($Smoke) { return }

    $wearer = Start-Fixture $owner 'client' 'GearWearer'
    $viewer = Start-Fixture $observer 'client' 'GearObserver'
    $deadline = (Get-Date).AddSeconds(135)
    while (!$dedicated.HasExited -or !$wearer.HasExited -or !$viewer.HasExited) {
        foreach ($process in $processes) {
            if ($process.HasExited -and $process.ExitCode -ne 0) {
                throw "A Minecraft process exited with code $($process.ExitCode). See $run"
            }
        }
        if ((Get-Date) -ge $deadline) { throw "Dedicated session timed out. See $run" }
        Start-Sleep -Milliseconds 500
    }
    Assert-Completed $server 'EQUIPMENT_DEDICATED_QA_SERVER_COMPLETE failures=0'
    Assert-Completed $owner 'EQUIPMENT_DEDICATED_QA_OWNER_COMPLETE failures=0'
    Assert-Completed $observer 'EQUIPMENT_DEDICATED_QA_OBSERVER_COMPLETE failures=0'
} finally {
    foreach ($process in $processes) {
        if (!$process.HasExited) { $process.Kill() }
        $process.Dispose()
    }
}
