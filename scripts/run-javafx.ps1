param(
    [string]$JavaFxHome = $env:JAVAFX_HOME
)

if ([string]::IsNullOrWhiteSpace($JavaFxHome)) {
    if (Test-Path "C:\javafx-sdk-21.0.12\lib") {
        $JavaFxHome = "C:\javafx-sdk-21.0.12"
    } else {
        Write-Host "Set JAVAFX_HOME to your JavaFX SDK folder, for example:"
        Write-Host '$env:JAVAFX_HOME = "C:\javafx-sdk-21.0.12"'
        exit 1
    }
}

$javaFxLib = Join-Path $JavaFxHome "lib"

if (-not (Test-Path $javaFxLib)) {
    Write-Host "Could not find JavaFX lib folder: $javaFxLib"
    exit 1
}

$sources = @()
$sources += Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName }
$sources += Get-ChildItem -Recurse -Filter *.java javafx-src | ForEach-Object { $_.FullName }

javac --module-path $javaFxLib --add-modules javafx.controls,javafx.media -d javafx-out $sources
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

java --module-path $javaFxLib --add-modules javafx.controls,javafx.media -cp "javafx-out;javafx-src" arena.fx.FxMain
