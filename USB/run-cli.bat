@echo off
REM Launches the linter CLI. Passes all command-line arguments straight through.
REM Usage: run-cli.bat ^<path^> [--only cursory,principle,pattern,all] [--config file.json] [--help]
java -cp "%~dp0linter.jar" rhit.csse.csse374.linter.presentation.LinterCLI %*
