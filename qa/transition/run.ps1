param(
    [Parameter(Mandatory)][ValidatePattern('^[A-Za-z0-9-]+$')][string]$Profile,
    [Parameter(Mandatory)][string]$ModJar,
    [string]$SourceRef,
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$LaunchFixture = 'build/legacy-functional-qa',
    [switch]$AllowFailures
)

$ErrorActionPreference = 'Stop'
$project = (Resolve-Path -LiteralPath "$PSScriptRoot/../..").Path
$artifact = (Resolve-Path -LiteralPath $ModJar).Path
$fixture = (Resolve-Path -LiteralPath $LaunchFixture).Path
$game = Join-Path $project "build/transition-qa/$Profile"
if (Test-Path -LiteralPath $game) {
    throw "QA profile already exists: $game"
}
$java = if ($JavaHome) { Join-Path $JavaHome 'bin/java.exe' } else { 'java.exe' }
$javac = if ($JavaHome) { Join-Path $JavaHome 'bin/javac.exe' } else { 'javac.exe' }
$jar = if ($JavaHome) { Join-Path $JavaHome 'bin/jar.exe' } else { 'jar.exe' }
$moddev = Join-Path $project 'build/moddev'
$classes = Join-Path $game 'classes'
$sources = Join-Path $game 'sources'
$scratchJar = Join-Path $game 'mods/crysis-legacy-transition-overlay.jar'
$sourceRoot = 'src/main/java/io/github/painworks/crysismod/nanosuit'
$classRoot = 'io/github/painworks/crysismod/nanosuit'
$profileSource = "$sourceRoot/NanosuitProfile.java"
$resolverSource = "$sourceRoot/util/NanosuitArmorUtil.java"

function Get-Source([string]$Path) {
    if (!$SourceRef) {
        return Get-Content -Raw -LiteralPath (Join-Path $project $Path)
    }
    $lines = & git -C $project show "${SourceRef}:$Path"
    if ($LASTEXITCODE) { throw "Cannot read $Path from $SourceRef" }
    return $lines -join "`n"
}

New-Item -ItemType Directory -Path $classes, $sources | Out-Null
Copy-Item -Path "$fixture/template/*" -Destination $game -Recurse
if (!(Test-Path -LiteralPath "$game/mods")) {
    New-Item -ItemType Directory -Path "$game/mods" | Out-Null
}
Copy-Item -LiteralPath $artifact -Destination $scratchJar
Get-FileHash -LiteralPath $artifact -Algorithm SHA256 | Format-List | Out-File "$game/input-jar.txt"

$profiles = Get-Source $profileSource
$enumAnchor = '   );'
if (!$profiles.Contains($enumAnchor)) { throw 'Unexpected profile enum declaration' }
$enumExtension = @'
   ),
   QA_RESTRICTED(
      NanosuitGeneration.NANOSUIT_1,
      new NanosuitControls(Set.of(), Set.of()),
      2,
      new NanosuitEnergyRules(
         new NanosuitEnergyRules.Recovery(2, 1, 2, 1, 8),
         NanosuitEnergyRules.LEGACY_CURRENT.healing(),
         NanosuitEnergyRules.LEGACY_CURRENT.criticalWarning()
      ),
      new NanosuitMobilityRules(NanosuitMobilityRules.LEGACY_CURRENT.jump(), 2, 0.0),
      NanosuitCombatRules.LEGACY_CURRENT,
      new NanosuitAuxiliaryRules(false, false)
   ),
   QA_SHARED(
      NanosuitGeneration.NANOSUIT_2,
      LEGACY_CURRENT.controls(),
      LEGACY_CURRENT.modeCooldownTicks(),
      LEGACY_CURRENT.energy(),
      LEGACY_CURRENT.mobility(),
      LEGACY_CURRENT.combat(),
      LEGACY_CURRENT.auxiliary()
   );
'@
$profiles = $profiles.Replace($enumAnchor, $enumExtension)
$profiles | Set-Content -LiteralPath "$sources/NanosuitProfile.java" -Encoding utf8NoBOM

$resolver = Get-Source $resolverSource
$method = '   public static NanosuitProfile getEquippedProfile(Player player) {'
if (!$resolver.Contains($method)) { throw 'Unexpected equipment resolver declaration' }
$override = @'
   private static final Map<UUID, NanosuitProfile> QA_PROFILES = new ConcurrentHashMap<>();

   public static void qaSelectProfile(net.minecraft.server.level.ServerPlayer player, NanosuitProfile profile) {
      if (!Boolean.getBoolean("crysis.transition.qa") || !player.server.isSameThread()) {
         throw new IllegalStateException("Transition QA requires the test server thread");
      }
      if (profile == null) {
         QA_PROFILES.remove(player.getUUID());
      } else {
         QA_PROFILES.put(player.getUUID(), profile);
      }
   }

   public static NanosuitProfile getEquippedProfile(Player player) {
      NanosuitProfile qaProfile = QA_PROFILES.get(player.getUUID());
      if (qaProfile != null) {
         return qaProfile;
      }
'@
$resolver.Replace($method, $override) | Set-Content -LiteralPath "$sources/NanosuitArmorUtil.java" -Encoding utf8NoBOM

$cp = (Get-Content -LiteralPath "$fixture/classpath.txt") -join ';'
$compileCp = $artifact + ';' + $cp
$javacArgs = @(
    '-proc:none', '-encoding', 'UTF-8', '-cp', ('"' + $compileCp.Replace('\', '/') + '"'),
    '-d', ('"' + $classes.Replace('\', '/') + '"')
) + @(
    "$sources/NanosuitProfile.java", "$sources/NanosuitArmorUtil.java", "$PSScriptRoot/TransitionQa.java"
    | ForEach-Object { '"' + $_.Replace('\', '/') + '"' }
)
$argFile = Join-Path $game 'javac.args'
$javacArgs | Set-Content -LiteralPath $argFile -Encoding utf8NoBOM
& $javac "@$argFile"
if ($LASTEXITCODE) { throw 'QA fixture compilation failed' }

& $jar --update --file $scratchJar -C $classes "$classRoot/NanosuitProfile.class" `
    -C $classes "$classRoot/util/NanosuitArmorUtil.class"
if ($LASTEXITCODE) { throw 'QA overlay could not be written' }
& $jar --create --file "$game/mods/crysis-transition-qa.jar" -C $classes local -C $PSScriptRoot META-INF
if ($LASTEXITCODE) { throw 'QA runner mod could not be packaged' }

$vm = Join-Path $moddev 'clientRunVmArgs.txt'
$program = Join-Path $fixture 'program.txt'
$log = Join-Path $game 'transition.log'
Push-Location -LiteralPath $game
try {
    & $java '-Xmx3G' '-Dfile.encoding=UTF-8' '-Dcrysis.transition.qa=true' `
        "@$vm" '-cp' $cp 'net.neoforged.devlaunch.Main' "@$program" > $log 2>&1
    if ($LASTEXITCODE) { throw "Minecraft exited with code ${LASTEXITCODE}. See $log" }
    $summary = @(Select-String -LiteralPath $log -Pattern '^TRANSITION_QA_COMPLETE failures=\d+$')
    if ($summary.Count -ne 1 -or (!$AllowFailures -and $summary[0].Line -ne 'TRANSITION_QA_COMPLETE failures=0')) {
        throw "Transition QA did not pass. See $log"
    }
    $summary[0].Line
} finally {
    Pop-Location
}
