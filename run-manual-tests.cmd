@echo off
setlocal

REM Convenience wrapper for Windows users.
REM Runs the PowerShell script with ExecutionPolicy Bypass.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-manual-tests.ps1" %*

endlocal

