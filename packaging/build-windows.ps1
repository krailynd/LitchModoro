#Requires -Version 5.1
<#
.SYNOPSIS
    Builds the LitchModoro Windows app (portable app-image by default) end to end.

.DESCRIPTION
    One-command Windows build for the LitchModoro study timer:
      1. mvn -q clean package -DskipTests   (tests are validated in CI / on Linux)
      2. jlink -> trimmed runtime image     (java.base, java.prefs, javafx.base,
                                             javafx.graphics, javafx.controls)
      3. jpackage --type app-image          (portable folder with LitchModoro.exe)

    The default output is a portable app-image: no installer, no WiX needed.
    Pass -Installer to produce a .exe installer instead (requires WiX Toolset 3.x).

    Must run on a Windows 11 host: jpackage does not cross-target.

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File packaging\build-windows.ps1

.EXAMPLE
    .\packaging\build-windows.ps1 -JavaFxJmodsPath "C:\javafx-jmods-21.0.5"

.EXAMPLE
    .\packaging\build-windows.ps1 -Installer -Dest "D:\releases"
#>
[CmdletBinding()]
param(
    # Path to the JavaFX jmods directory (contains javafx.base.jmod etc.).
    # Default: %JAVAFX_JMODS%, then C:\javafx-jmods-21.0.5.
    [string]$JavaFxJmodsPath = $(if ($env:JAVAFX_JMODS) { $env:JAVAFX_JMODS } else { 'C:\javafx-jmods-21.0.5' }),

    # App version stamped into the package.
    [string]$AppVersion = '1.0.0',

    # Destination directory for the packaged output. Default: <project>\dist.
    [string]$Dest = '',

    # Build a .exe installer instead of the default portable app-image.
    # Requires WiX Toolset 3.x (candle.exe / light.exe) on PATH.
    [switch]$Installer
)

$ErrorActionPreference = 'Stop'

$AppName   = 'LitchModoro'
$Vendor    = 'Krailynd'
$MainClass = 'com.krailynd.pomodoro.MainApp'
$JarName   = 'litchmodoro-1.0.0.jar'
$Modules   = 'java.base,java.prefs,javafx.base,javafx.graphics,javafx.controls'

$ProjectRoot  = Split-Path -Parent $PSScriptRoot
$TargetDir    = Join-Path $ProjectRoot 'target'
$RuntimeImage = Join-Path $TargetDir 'runtime-image'
$DestDir      = if ($Dest) { $Dest } else { Join-Path $ProjectRoot 'dist' }
$IconPath     = Join-Path $ProjectRoot 'packaging\icon.ico'

function Resolve-Tool([string]$Name) {
    $cmd = Get-Command $Name -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    if ($env:JAVA_HOME) {
        $candidate = Join-Path $env:JAVA_HOME "bin\$Name.exe"
        if (Test-Path $candidate) { return $candidate }
    }
    throw "Tool '$Name' not found on PATH or in JAVA_HOME\bin. Install a JDK 21+ and set JAVA_HOME."
}

Write-Host "==> Checking prerequisites"

$mvn      = Resolve-Tool 'mvn.cmd'
$jlink    = Resolve-Tool 'jlink'
$jpackage = Resolve-Tool 'jpackage'

$javaVersion = & (Resolve-Tool 'java') -version 2>&1 | Select-Object -First 1
Write-Host "    $javaVersion"
if ($javaVersion -notmatch '"(2[1-9]|[3-9]\d)') {
    throw "JDK 21+ is required (found: $javaVersion)."
}

if (-not (Test-Path (Join-Path $JavaFxJmodsPath 'javafx.base.jmod'))) {
    throw @"
JavaFX jmods not found at '$JavaFxJmodsPath'.
Download the JavaFX SDK jmods for Windows (21.0.x) from
https://gluonhq.com/products/javafx/ and either:
  - extract to C:\javafx-jmods-21.0.5, or
  - pass -JavaFxJmodsPath <path>, or
  - set the JAVAFX_JMODS environment variable.
"@
}

if (-not (Test-Path $IconPath)) {
    throw "Windows icon not found: $IconPath (generate it from src/main/resources/.../icon.png)."
}

if ($Installer) {
    # jpackage --type exe requires WiX Toolset 3.x (candle.exe + light.exe) on PATH.
    if (-not (Get-Command 'candle.exe' -ErrorAction SilentlyContinue)) {
        throw @"
WiX Toolset 3.x not found on PATH.
jpackage '--type exe' requires WiX 3.11+ (candle.exe / light.exe).
Install WiX Toolset v3.14 from https://wixtoolset.org/docs/wix3/ and
add its bin directory to PATH, then re-run this script.
Tip: without WiX you can still use the default portable build (omit -Installer).
"@
    }
}

Write-Host "==> Building jar (mvn -q clean package -DskipTests)"
Push-Location $ProjectRoot
try {
    & $mvn -q clean package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw "Maven build failed (exit $LASTEXITCODE)." }
} finally {
    Pop-Location
}

$JarPath = Join-Path $TargetDir $JarName
if (-not (Test-Path $JarPath)) { throw "Expected jar not found: $JarPath" }

Write-Host "==> Building trimmed runtime image (jlink)"
if (Test-Path $RuntimeImage) { Remove-Item -Recurse -Force $RuntimeImage }
& $jlink --module-path $JavaFxJmodsPath `
         --add-modules $Modules `
         --strip-debug --no-header-files --no-man-pages `
         --output $RuntimeImage
if ($LASTEXITCODE -ne 0) { throw "jlink failed (exit $LASTEXITCODE)." }

$packageType = if ($Installer) { 'exe' } else { 'app-image' }
Write-Host "==> Packaging $AppName (jpackage --type $packageType)"
New-Item -ItemType Directory -Force -Path $DestDir | Out-Null

$jpackageArgs = @(
    '--type', $packageType,
    '--name', $AppName,
    '--app-version', $AppVersion,
    '--vendor', $Vendor,
    '--input', $TargetDir,
    '--main-jar', $JarName,
    '--main-class', $MainClass,
    '--runtime-image', $RuntimeImage,
    '--icon', $IconPath,
    '--dest', $DestDir
)
if ($Installer) {
    $jpackageArgs += @('--win-shortcut', '--win-menu')
}

& $jpackage @jpackageArgs
if ($LASTEXITCODE -ne 0) { throw "jpackage failed (exit $LASTEXITCODE)." }

Write-Host ""
if ($Installer) {
    $installer = Get-ChildItem $DestDir -Filter '*.exe' | Select-Object -First 1
    Write-Host "BUILD OK: $($installer.FullName)"
} else {
    $exePath = Join-Path $DestDir "$AppName\$AppName.exe"
    Write-Host "BUILD OK: $exePath (portable folder: $(Join-Path $DestDir $AppName))"
}
