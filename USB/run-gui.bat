@echo off
REM Launches the linter GUI using the host machine's Java (17+).
REM %~dp0 resolves to this script's folder, so the bundle is drive-letter agnostic.
java -jar "%~dp0linter.jar"
