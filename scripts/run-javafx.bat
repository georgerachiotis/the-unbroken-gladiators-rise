@echo off
setlocal EnableDelayedExpansion

if "%JAVAFX_HOME%"=="" (
  if exist "C:\javafx-sdk-21.0.12\lib" (
    set "JAVAFX_HOME=C:\javafx-sdk-21.0.12"
  ) else (
    echo Set JAVAFX_HOME to your JavaFX SDK folder, for example:
    echo set JAVAFX_HOME=C:\javafx-sdk-21.0.12
    exit /b 1
  )
)

set "JAVAFX_LIB=%JAVAFX_HOME%\lib"

if not exist "%JAVAFX_LIB%" (
  echo Could not find JavaFX lib folder: %JAVAFX_LIB%
  exit /b 1
)

if not exist out mkdir out
if not exist javafx-out mkdir javafx-out

del "%TEMP%\unbroken-javafx-sources.txt" 2>nul

for /r src %%f in (*.java) do (
    set "FILE=%%~ff"
    set "FILE=!FILE:\=/!"
    echo "!FILE!">>"%TEMP%\unbroken-javafx-sources.txt"
)

for /r javafx-src %%f in (*.java) do (
    set "FILE=%%~ff"
    set "FILE=!FILE:\=/!"
    echo "!FILE!">>"%TEMP%\unbroken-javafx-sources.txt"
)

javac --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.media -d javafx-out @"%TEMP%\unbroken-javafx-sources.txt"
if errorlevel 1 exit /b %errorlevel%

java --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.media -cp "javafx-out;javafx-src" arena.fx.FxMain
