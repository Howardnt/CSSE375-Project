# Installation and Configuration Guide
**Java Design Linter** · CSSE 375 · Ervin Perkowski

This guide covers installing the linter and the Java runtime it
depends on, on Windows, macOS, and Linux. There is no installer — the
linter ships as a single self-contained jar.

---

## 1. System requirements

| Requirement | Minimum | Notes |
|---|---|---|
| Java | **17** | Tested through Java 24 |
| Operating system | Windows 10 / 11, macOS 10.15+, Linux | The fat jar is platform-independent |
| Disk | ~2 MB for the jar | Plus optional ~180 MB for a bundled JDK |
| RAM | ~256 MB working set | Larger targets need more |

---

## 2. Installing Java

If `java -version` already prints 17 or newer, skip to Section 3.

### 2.1 Windows — using `winget` (easiest, needs admin)

Open PowerShell as Administrator:

```powershell
winget install --id EclipseAdoptium.Temurin.21.JDK
```

Open a **new** PowerShell window and verify:

```powershell
java -version
```

You should see `openjdk version "21" …` (or higher).

### 2.2 Windows — MSI installer

1. Go to <https://adoptium.net/temurin/releases/>
2. Filter: Windows × x64 × JDK
3. Download the `.msi` (~180 MB)
4. Run the installer. **Check** both "Set JAVA_HOME variable" and
   "Add to PATH" — they are off by default in some versions.
5. Open a new PowerShell window and verify with `java -version`.

### 2.3 macOS — Homebrew

```bash
brew install --cask temurin@21
```

Verify with `java -version`.

### 2.4 Linux — distribution package

```bash
# Debian/Ubuntu
sudo apt update && sudo apt install -y temurin-21-jdk

# Fedora/RHEL
sudo dnf install -y temurin-21-jdk

# Arch
sudo pacman -S jdk21-temurin
```

Verify with `java -version`.

### 2.5 No-admin install (portable JDK)

Useful if you don't have admin rights (lab machine, classmate's
laptop, etc.):

1. Download the **ZIP** (not MSI) from Adoptium for your OS.
2. Extract anywhere — e.g., `C:\jdk-21\` or `~/jdk-21/`.
3. Run the linter with the full path:
   ```
   C:\jdk-21\bin\java.exe -jar linter.jar
   ```

---

## 3. Installing the linter

### 3.1 Option A — pre-built fat jar

If you have a release build of `linter.jar` (~1.5 MB):

1. Put `linter.jar` anywhere — a folder on your hard drive, a flash
   drive, or a network share.
2. Done. There is no install step.

To run:

- **GUI:** `java -jar linter.jar` (or double-click on most systems)
- **CLI:** `java -cp linter.jar rhit.csse.csse374.linter.presentation.LinterCLI <path>`

### 3.2 Option B — build from source

```bash
git clone <repo-url>
cd CSSE375-Project
mvn package
```

The fat jar is at
`target/LinterProject-1.0-rc3-jar-with-dependencies.jar`. Rename to
`linter.jar` for convenience.

### 3.3 Option C — portable USB bundle

A self-contained bundle suitable for a flash drive — runs on any
machine with Java 17+ without any install:

```
USB-drive/Lint/
├── linter.jar                  ← the fat jar
├── run-gui.bat                 ← Windows GUI launcher (double-click)
├── run-cli.bat                 ← Windows CLI launcher
├── run-gui.sh                  ← Mac/Linux GUI launcher
├── sample-config.json          ← example JSON config
├── demo-targets/
│   ├── linter-self/            ← compiled classes for a demo target
│   └── fixtures/               ← intentional-violation fixtures
├── README.txt                  ← general how-to
└── DEMO.txt                    ← 10-minute demo walkthrough
```

This bundle is built from the repository's `USB/` folder. Copy the
whole `USB/` folder onto a flash drive and you have a portable
linter ready to demo on any Java-equipped machine.

### 3.4 Option D — fully portable (USB with bundled JDK)

Drop a portable JDK into the same bundle to remove the
host-machine Java requirement entirely:

```
USB-drive/Lint/
├── jdk-21/                     ← unzipped portable JDK
│   └── bin/java(.exe)
├── linter.jar
├── run-gui.bat                 ← edited to use jdk-21\bin\java.exe
└── …
```

Edit `run-gui.bat` to point at the bundled JDK:

```bat
@echo off
"%~dp0jdk-21\bin\java.exe" -jar "%~dp0linter.jar"
```

`%~dp0` resolves to "this script's folder", so the bundle is
drive-letter agnostic — it works whether the USB mounts as `D:`, `E:`,
etc.

Footprint: about 200 MB total. Works on any Windows machine with no
installation and no admin rights.

---

## 4. Configuration

The linter has two configuration mechanisms:

### 4.1 JSON config file

A file like `linter.json`:

```json
{
  "defaultEnabled": true,
  "checks": {
    "EqualsChecker":        { "enabled": false },
    "MethodTooLongPattern": { "enabled": true, "options": { "threshold": "20" } }
  }
}
```

Used by both the GUI (Load config… button) and the CLI
(`--config <file>` flag). Keys can be simple class names or fully
qualified names. Missing keys fall back to `defaultEnabled` (which
itself defaults to `true`).

### 4.2 CLI flags

| Flag | Purpose |
|---|---|
| `--only cursory,principle,pattern,all` | Restrict which check categories run |
| `--config <file>` | Apply a JSON config |
| `--help`, `-h` | Show usage |

These flags can be combined.

---

> **[Screenshot — completed Adoptium install wizard with "Set
> JAVA_HOME" and "Add to PATH" boxes ticked]** *Suggested filename:
> `docs/screenshots/install-temurin.png`.*

> **[Screenshot — terminal output of `java -version` showing a
> 17+ release]** *Suggested filename:
> `docs/screenshots/install-java-version.png`.*

## 5. Verifying the install

### Quick smoke test
```
java -jar linter.jar
```
Should open the GUI with `Ready.` in the banner.

### CLI smoke test
```
java -cp linter.jar rhit.csse.csse374.linter.presentation.LinterCLI --help
```
Should print the usage message and exit 0.

### Full functional test (lint the linter on itself)
```
java -cp linter.jar rhit.csse.csse374.linter.presentation.LinterCLI <path-to-compiled-classes>
```
Replace `<path-to-compiled-classes>` with the linter's own
`target/classes` (or the `demo-targets/linter-self` folder from the
USB bundle). You should see a wall of violations and exit code 1.

---

## 6. Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| `'java' is not recognized` | Java not on PATH | Reinstall and check "Add to PATH" |
| `UnsupportedClassVersionError … class file version 61.0` | Host Java is < 17 | Install Java 17+ |
| Generic "A Java Exception has occurred" popup on double-click | Windows `.jar` association points to an old JRE | Use the `.bat` launcher, or repair the association via right-click → Open with → choose `javaw.exe` from your modern JDK's `bin/` |
| `Error: Unable to access jarfile` | Wrong path or truncated copy | Re-copy; jar should be ~1.5 MB |
| GUI opens but Browse… seems to do nothing | File chooser opened behind the main window | Alt-Tab |
| `--only` rejects valid-looking values | Typo, or extra spaces | Format: `--only cursory,principle,pattern` (comma-separated, no spaces) |

---

> **[Screenshot — File Explorer view of the USB bundle showing the
> 7 expected files and `demo-targets/` subfolder]** *Suggested
> filename: `docs/screenshots/usb-layout.png`.*

## 7. Uninstalling

Delete the jar. The linter writes no configuration files, no registry
keys, no system files — it is fully self-contained.
