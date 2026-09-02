param(
    [string]$JavaFxHome = $env:JAVAFX_HOME,
    [string]$AppVersion = "1.0.0"
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

if ([string]::IsNullOrWhiteSpace($JavaFxHome)) {
    if (Test-Path "C:\javafx-sdk-21.0.12\lib") {
        $JavaFxHome = "C:\javafx-sdk-21.0.12"
    } else {
        throw "Set JAVAFX_HOME to the JavaFX SDK folder before building."
    }
}

$javaFxLib = Join-Path $JavaFxHome "lib"
$buildRoot = Join-Path $projectRoot "build\windows"
$classesDir = Join-Path $buildRoot "classes"
$inputDir = Join-Path $buildRoot "input"
$distDir = Join-Path $projectRoot "dist"
$sourceList = Join-Path $buildRoot "sources.txt"
$appJar = Join-Path $inputDir "the-unbroken.jar"
$iconPath = Join-Path $projectRoot "javafx-src\arena\fx\assets\icons\app-icon.ico"

if (-not (Test-Path $javaFxLib)) { throw "JavaFX library folder not found: $javaFxLib" }
if (-not (Test-Path $iconPath)) { throw "Application icon not found: $iconPath" }

Remove-Item -LiteralPath $buildRoot -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $classesDir, $inputDir, $distDir | Out-Null

$sources = @()
$sources += Get-ChildItem -Recurse -Filter *.java "src" | ForEach-Object { $_.FullName }
$sources += Get-ChildItem -Recurse -Filter *.java "javafx-src" | ForEach-Object { $_.FullName }
$sources | ForEach-Object { '"' + ($_.Replace('\', '/')) + '"' } | Set-Content -Encoding UTF8 $sourceList

& javac --module-path $javaFxLib --add-modules javafx.controls,javafx.media `
    -encoding UTF-8 -d $classesDir "@$sourceList"
if ($LASTEXITCODE -ne 0) { throw "Java compilation failed." }

Copy-Item -LiteralPath "javafx-src\arena\fx\fx-style.css" `
    -Destination (Join-Path $classesDir "arena\fx\fx-style.css")
Copy-Item -LiteralPath "javafx-src\arena\fx\assets" `
    -Destination (Join-Path $classesDir "arena\fx\assets") -Recurse

& jar --create --file $appJar --main-class arena.fx.FxMain -C $classesDir .
if ($LASTEXITCODE -ne 0) { throw "Application JAR creation failed." }

$existingImage = Join-Path $distDir "TheUnbrokenGladiatorsRise"
Remove-Item -LiteralPath $existingImage -Recurse -Force -ErrorAction SilentlyContinue

& jpackage `
    --type app-image `
    --name "TheUnbrokenGladiatorsRise" `
    --app-version $AppVersion `
    --vendor "The Unbroken" `
    --description "A gladiator's rise from the Ludus to freedom." `
    --input $inputDir `
    --main-jar "the-unbroken.jar" `
    --main-class "arena.fx.FxMain" `
    --module-path $javaFxLib `
    --add-modules "javafx.controls,javafx.media" `
    --icon $iconPath `
    --dest $distDir
if ($LASTEXITCODE -ne 0) { throw "Windows application packaging failed." }

# The JavaFX SDK keeps its Windows graphics/media native libraries in bin;
# jpackage does not copy them automatically when the modular JARs come from lib.
$javaFxBin = Join-Path $JavaFxHome "bin"
$runtimeBin = Join-Path $existingImage "runtime\bin"
if (-not (Test-Path $javaFxBin)) { throw "JavaFX native library folder not found: $javaFxBin" }
Copy-Item -Path (Join-Path $javaFxBin "*.dll") -Destination $runtimeBin -Force

Write-Host "Windows build created: $existingImage"
Write-Host "Launch with: $existingImage\TheUnbrokenGladiatorsRise.exe"
