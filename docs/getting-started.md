# Getting Started in 5 Minutes
**Java Design Linter** · CSSE 375 · Ervin Perkowski

---

## Prerequisites (30 seconds)

- **Java 17 or newer** on your machine. Check with:
  ```
  java -version
  ```
  If you don't have it, follow the [Installation Guide](installation-guide.md) first.

- A folder of **compiled `.class` files** to inspect. If you have a
  Maven project, that's usually `target/classes`. If you have an IDE
  project, it's wherever the IDE puts compiled output (`out/production/<module>`,
  `build/classes`, etc.).

## Step 1 — Get the linter (1 minute)

Either:

- **Download the fat jar** (`linter.jar`) from your release, or
- **Build from source:**
  ```
  git clone <repo-url>
  cd CSSE375-Project
  mvn package
  ```
  The fat jar lands at
  `target/LinterProject-1.0-rc3-jar-with-dependencies.jar` (~1.5 MB).
  Rename it to `linter.jar` if you want shorter commands.

## Step 2 — Run the GUI (1 minute)

```
java -jar linter.jar
```

Or **double-click** the jar (Windows usually opens it with `javaw`).

The window opens with `Ready.` in the banner at the top.

## Step 3 — Lint something (2 minutes)

1. Click **Browse…**
2. Navigate to a directory of compiled classes — easiest target is
   the linter's own `target/classes` from Step 1, or use the bundled
   `demo-targets/fixtures` folder if you have the portable USB bundle.
3. Click **Run**.

You should see:
- A bold banner at the top, e.g.
  `3 errors · 2 warnings · 1 info · 6 total · ran in 0.45 s`
- The **Results** tab on the right shows expandable groups per check.

## Step 4 — Try the CLI (30 seconds)

For headless / CI use:

```
java -cp linter.jar rhit.csse.csse374.linter.presentation.LinterCLI target/classes
echo Exit: %ERRORLEVEL%
```

Exit codes: `0` clean, `1` violations, `2` usage error, `3` runtime error.

## Step 5 — Try the JSON config (30 seconds)

Drop a `linter.json` next to the jar:

```json
{
  "checks": {
    "EqualsChecker": { "enabled": false }
  }
}
```

Run:

```
java -cp linter.jar rhit.csse.csse374.linter.presentation.LinterCLI target/classes --config linter.json
```

`EqualsChecker` violations are gone; everything else still runs.

---

## What's next

- Full feature tour: [User Guide](user-guide.md)
- Setting up Java or running portably: [Installation Guide](installation-guide.md)
- Extending with your own checks: [Developer Guide](developer-maintenance-guide.md)
