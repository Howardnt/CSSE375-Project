CSSE 375 Java Design Linter - Portable Build
============================================

Requirements
------------
  Java 17 or newer on the host machine.
  Check with:  java -version

Run the GUI
-----------
  Windows:   double-click run-gui.bat   (or double-click linter.jar)
  Mac/Linux: bash run-gui.sh

The window shows a top bar with target-path / Browse / Use default output /
Load config buttons, a check-selection pane on the left, and results tabs
on the right with a bold one-line summary banner above them.

Run the CLI
-----------
  Windows:   run-cli.bat <path-to-classes> [--only ...] [--config file.json]
  Mac/Linux: java -cp linter.jar rhit.csse.csse374.linter.presentation.LinterCLI <path>

Sample CLI usage
----------------
  run-cli.bat C:\some\project\target\classes
  run-cli.bat C:\some\project\target\classes --only pattern
  run-cli.bat C:\some\project\target\classes --config sample-config.json
  run-cli.bat --help

Exit codes
----------
  0  clean (no violations)
  1  violations found  (useful for CI gating)
  2  usage error       (missing args, bad flags, bad config file)
  3  runtime error

Files in this bundle
--------------------
  linter.jar           - fat jar with all dependencies (ASM, FlatLaf, JSON-P)
  run-gui.bat          - Windows GUI launcher
  run-cli.bat          - Windows CLI launcher
  run-gui.sh           - Mac/Linux GUI launcher
  sample-config.json   - example config disabling EqualsChecker
  README.txt           - this file

Troubleshooting
---------------
  "'java' is not recognized" / "command not found"
      -> Java is not installed or not on PATH. Install JDK 17+.

  "UnsupportedClassVersionError: ... has been compiled by a more recent
   version of the Java Runtime"
      -> The host has Java but it's older than 17. Upgrade.

  GUI opens but Browse... does nothing
      -> Look behind the main window; the file chooser may have opened
         beneath it.

  CLI prints "Warning: Path does not exist"
      -> The target path you supplied doesn't exist on this machine.
         Run with an absolute path that exists locally.
