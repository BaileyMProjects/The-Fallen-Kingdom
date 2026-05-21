@echo off
setlocal

:: ============================================================
::  The Fallen Kingdom — Windows build script
::
::  Produces a self-contained app in dist\The Fallen Kingdom\
::  that includes a bundled JRE.  No Java install required on
::  the target machine.
::
::  Requirements:
::    - JDK 17+ on this machine (jpackage is included)
::    - Run this script from the project root
::
::  Output:
::    dist\The Fallen Kingdom\The Fallen Kingdom.exe
::  Zip the "The Fallen Kingdom" folder and share it.
:: ============================================================

set APP_NAME=The Fallen Kingdom
set MAIN_CLASS=core.Main
set SRC=src
set OUT=out\production\cs1op-textgame
set JAR_DIR=build\jar
set JAR_NAME=fallen-kingdom.jar
set DIST=dist

:: Path to JDK tools (update this if you install a different JDK)
set JDK_BIN=C:\Program Files\Java\jdk-26.0.1\bin

:: ── Step 1: Compile ──────────────────────────────────────────
echo [1/3] Compiling source...
if not exist "%OUT%" mkdir "%OUT%"
javac -cp "%SRC%" -d "%OUT%" -encoding UTF-8 ^
    %SRC%\core\*.java ^
    %SRC%\characters\*.java ^
    %SRC%\combat\*.java ^
    %SRC%\enchantments\*.java ^
    %SRC%\events\*.java ^
    %SRC%\items\*.java ^
    %SRC%\puzzles\*.java ^
    %SRC%\quests\*.java ^
    %SRC%\save\*.java ^
    %SRC%\ui\*.java ^
    %SRC%\util\*.java ^
    %SRC%\world\*.java
if errorlevel 1 (
    echo [ERROR] Compilation failed.
    pause
    exit /b 1
)
echo     Compilation successful.

:: ── Step 2: Create fat JAR ────────────────────────────────────
echo [2/3] Packaging JAR...
if not exist "%JAR_DIR%" mkdir "%JAR_DIR%"

:: Write manifest
echo Main-Class: %MAIN_CLASS%> "%JAR_DIR%\MANIFEST.MF"

"%JDK_BIN%\jar" cfm "%JAR_DIR%\%JAR_NAME%" "%JAR_DIR%\MANIFEST.MF" -C "%OUT%" .
if errorlevel 1 (
    echo [ERROR] JAR creation failed.
    pause
    exit /b 1
)
echo     JAR created: %JAR_DIR%\%JAR_NAME%

:: ── Step 3: jpackage — bundle JRE into exe ───────────────────
echo [3/3] Building self-contained app with jpackage...
if exist "%DIST%\%APP_NAME%" rmdir /s /q "%DIST%\%APP_NAME%"
if not exist "%DIST%" mkdir "%DIST%"

"%JDK_BIN%\jpackage" ^
    --type app-image ^
    --name "%APP_NAME%" ^
    --app-version "1.0" ^
    --input "%JAR_DIR%" ^
    --main-jar "%JAR_NAME%" ^
    --main-class "%MAIN_CLASS%" ^
    --dest "%DIST%" ^
    --java-options "-Dfile.encoding=UTF-8"
if errorlevel 1 (
    echo [ERROR] jpackage failed.  Make sure you have JDK 17+ and jpackage in PATH.
    pause
    exit /b 1
)

:: ── Step 4: Copy resources (ASCII art + images) into the package ─────────
echo [4/4] Copying resources...
if exist "resources" (
    xcopy /E /I /Y "resources" "%DIST%\%APP_NAME%\resources" >nul
    echo     Resources copied.
) else (
    echo     No resources folder found, skipping.
)

echo.
echo ============================================================
echo  Build complete!
echo  Output: %DIST%\%APP_NAME%\%APP_NAME%.exe
echo.
echo  To share with friends:
echo    1. Right-click the "%APP_NAME%" folder in dist\
echo    2. Send to ^> Compressed (zipped) folder
echo    3. Send the zip — they unzip and double-click the .exe
echo    4. No Java installation required on their machine.
echo ============================================================
pause
