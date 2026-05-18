# Developer and Maintenance Guide
**Java Design Linter** · CSSE 375 · Ervin Perkowski

This guide is for developers extending or maintaining the linter.

---

## 1. Project structure

```
CSSE375-Project/
├── pom.xml                     ← Maven build
├── src/main/java/rhit/csse/csse374/linter/
│   ├── data/                   ← ASM wrappers + JSON config loader
│   ├── domain/                 ← Lint checks + LinterHandler + result types
│   └── presentation/           ← LinterService + LinterCLI + LinterOutputText + JsonReportWriter + ResultsSummary
│       └── gui/                ← Swing GUI classes
├── src/test/java/
│   ├── unit/                   ← Q1: Unit tests
│   ├── system/                 ← Q2: End-to-end + MockGuiHarness + UseCaseDriverTest
│   ├── checks/                 ← Per-check tests against fixtures
│   ├── fixtures/               ← Intentional-violation Java source for tests
│   ├── performance/            ← Q4: Performance tests
│   ├── security/               ← Q4: Security tests
│   └── ManualLintRunner.java   ← Q3: Manual exploratory entry point
├── docs/                       ← These docs + architecture diagram + Feathers writeup
├── USB/                        ← Portable demo bundle
└── .github/workflows/ci.yml    ← GitHub Actions: mvn test on push
```

---

## 2. Architecture rule (most important)

**The codebase uses a 3-layer architecture:**

```
presentation → domain → data
```

- presentation may import from domain and data.
- domain may import from data.
- data must not import from domain or presentation.

Any pull request that imports presentation from domain or data, or
imports domain from data, must be revised. This rule kept the JSON
config feature clean (Feathers Ch. 14) and is the reason the GUI and
CLI share zero detection logic.

---

## 3. Build and test

| Task | Command |
|---|---|
| Compile | `mvn compile` |
| Run unit + system + perf + security tests | `mvn test` |
| Build fat jar | `mvn package` (output: `target/LinterProject-1.0-rc3-jar-with-dependencies.jar`) |
| Clean | `mvn clean` |

The CI pipeline (`.github/workflows/ci.yml`) runs `mvn test` on every
push to `master`. **Do not merge red builds.**

---

## 4. Adding a new lint check

### 4.1 Pick a category

- **Cursory** — surface-level, fast, per-class checks (naming, equals)
- **Principle** — OO principle violations (SRP, ISP, OCP, Hollywood)
- **Pattern** — design pattern detection (Template, Strategy, etc.)

### 4.2 Implement

1. Create a class in `src/main/java/rhit/csse/csse374/linter/domain/`
   that extends `Cursory`, `Principle`, or `Pattern`.
2. Implement `name(): String` and `checkClass(ASMClass cls): List<Violation>`.
3. Add a fixture under `src/test/java/fixtures/` that should trigger
   your check.
4. Add a test under `src/test/java/checks/` that runs your check
   against the fixture and asserts the expected violations.

**That's it.** `CheckCatalog` discovers new checks at startup by
classpath scanning — no GUI registration, no manual list update.

### 4.3 Worked example

`src/main/java/rhit/csse/csse374/linter/domain/MyNewCheck.java`:

```java
package rhit.csse.csse374.linter.domain;

import rhit.csse.csse374.linter.data.ASMClass;

import java.util.ArrayList;
import java.util.List;

public class MyNewCheck extends Cursory {
    @Override
    public String name() {
        return "MyNewCheck";
    }

    @Override
    public List<Violation> checkClass(ASMClass cls) {
        List<Violation> out = new ArrayList<>();
        if (cls.getClassName().contains("Bad")) {
            out.add(new Violation(
                "Class name contains 'Bad'",
                cls.getClassName(),
                SeverityLevel.WARNING));
        }
        return out;
    }
}
```

Re-run `mvn test`. `CheckCatalogTest` will pick it up automatically.

### 4.4 Reading per-check options

If your check should be configurable from `linter.json`'s `options`
block, accept a `LinterConfig` and read values:

```java
Map<String, String> opts = config.optionsFor(this.getClass().getName());
int threshold = Integer.parseInt(opts.getOrDefault("threshold", "20"));
```

Currently `LinterConfig` is wired through `LinterCLI.parseConfig()`
and `LinterGuiFrame.onLoadConfig()`. The simplest pattern is to read
options from a constructor (so `CheckCatalog` can pass them when the
discovery system is enriched in a future iteration).

---

## 5. Adding a new output format (renderer)

1. Create a class in `src/main/java/rhit/csse/csse374/linter/presentation/`
   that takes a `LinterResult` and produces a `String`.
2. Add a `--format <name>` flag to `LinterCLI`.
3. Add a button or menu item in `LinterGuiFrame` if the format should
   be exposed in the GUI.
4. Write a characterization test pinning down the output byte-for-byte.

The existing `JsonReportWriter` and `LinterOutputText` are the
templates to copy. They are pure functions and live in the
presentation layer.

Note: a `ReportRenderer` interface was sketched in early planning but
not introduced — there are only two renderers today (`text` and
`json`) plus the on-screen accordion, so adding the interface would
be premature. Introduce it when you add a third renderer.

---

## 6. The MockGuiHarness (test driver)

When changing behavior that flows through user actions, prefer
writing a use-case scenario in
[`UseCaseDriverTest`](../src/test/java/system/UseCaseDriverTest.java)
rather than spinning up Swing. The harness exposes the same
operations as `LinterGuiFrame` (`setTargetPath`, `selectAll`,
`deselect`, `loadConfig`, `useDefaultConfig`, `clickRun`,
`summaryBanner`, `exportJson`) but is non-Swing.

This is the explicit "driver below presentation layer" Objective 2
artifact from M3.

---

## 7. Feathers seams in the codebase

See [`docs/feathers-techniques.md`](feathers-techniques.md) for the
full inventory. Quick index:

- **Parameterize Constructor** — `LinterService(Function<String, ASMProject>)`
- **Subclass and Override** — `LinterService` is non-final; tests use
  `RecordingService extends LinterService`.
- **Extract Interface** — `LinterConfig` interface in data, with
  `MapLinterConfig` impl.
- **Adapt Parameter** — `JsonLinterConfigLoader.load(Path) →
  read(Reader) → parse(String)` chain.
- **Constructor injection of a catalog supplier** — `LinterCLI(LinterService, Supplier<List<CheckDescriptor>>)`.

If you add new code that's hard to test, prefer adding a seam over
introducing a singleton or a static call.

---

## 8. Style and conventions

- **Comments:** use `//stuff` (no space after `//`) where used at all.
  Default to no comments unless the *why* is non-obvious.
- **One feature or one refactoring per commit.** Do not bundle a
  refactoring with a feature in the same commit.
- **No layer-rule violations.** See Section 2.
- **Tests first when adding a check** — characterization tests before
  refactoring (Feathers Ch. 13). The
  [JsonReportWriterTest](../src/test/java/unit/JsonReportWriterTest.java)
  file's docstring is the template.

---

## 9. Common maintenance tasks

### 9.1 Adding a new dependency

1. Add it to `pom.xml` under `<dependencies>`.
2. Run `mvn package` and verify the fat jar still builds.
3. If the dependency is for production code, document it in the SADS
   under "Constraints."

### 9.2 Bumping the Java version

Update `<source>` and `<target>` in `pom.xml`. The fat jar will only
run on JDKs at or above the new version. The Installation Guide must
also be updated.

### 9.3 Updating the architecture diagram

`docs/architecture/design.puml` is the source. Regenerate the PNG
with any PlantUML toolchain (IDE plugin or `plantuml.jar`).

### 9.4 Rebuilding the portable USB bundle

```powershell
# from project root, after mvn clean package
Copy-Item target\LinterProject-1.0-rc3-jar-with-dependencies.jar USB\linter.jar -Force
# the rest of USB/ (launchers, demo-targets, docs) is static
```

Then copy the whole `USB/` folder to a flash drive.

---

## 10. CI pipeline

`.github/workflows/ci.yml` runs `mvn test` on every push and pull
request to `master`. The pipeline fails on:

- Compile errors
- Any test failure (across all four quadrants: unit / system / perf / security)

There is no separate "build the fat jar" step in CI today — that
would be a useful addition (run `mvn verify` instead of `mvn test`).
See Trello backlog.

---

## 11. Where to file bugs and ideas

The project Trello board (referenced in M4 deliverables) is the
authoritative backlog. Current known issues are listed in the
**Deferred / known limitations** section of the SADS and in the
"Table of planned changes" in §13 below.

When filing a new bug, please include:
- The target classpath you ran the linter on
- The Java version (`java -version`)
- The CLI command or GUI steps to reproduce
- The actual exception text (run from a terminal, not double-click,
  so the popup doesn't swallow the stack trace)

---

## 12. Comprehensive troubleshooting chart

| # | Symptom | Likely root cause | First check | Fix |
|---|---|---|---|---|
| T1 | Generic "A Java Exception has occurred" popup on double-click of jar | Windows `.jar` file association points to an old JRE, not the Java 17+ on `PATH` | Run from a terminal: `java -jar linter.jar` to see the real exception | Either always use `run-gui.bat`, or repair the association: right-click `linter.jar` → Open with → Choose another app → browse to `<JDK>\bin\javaw.exe` |
| T2 | Terminal shows `UnsupportedClassVersionError … class file version 61.0` | Host Java is < 17 | `java -version` | Install Java 17+ (see Installation Guide §2) |
| T3 | Terminal shows `'java' is not recognized` | Java not installed, or not on `PATH` | `java -version` | Install Java 17+ and check the "Add to PATH" box; reopen terminal |
| T4 | `Error: Unable to access jarfile` | Wrong path, or USB copy truncated | Check size — fat jar should be ~1.5 MB (1,523,913 bytes) | Re-copy from `target/LinterProject-1.0-rc3-jar-with-dependencies.jar` |
| T5 | CLI prints `Warning: Path does not exist` | The supplied target path doesn't exist on the host machine | Verify the path with `Get-Item <path>` or `ls <path>` | Pass an absolute path that exists locally |
| T6 | GUI shows "Invalid config file" dialog | JSON syntax error in the supplied config | Open the file in any JSON validator | Fix the JSON; common cause is missing quotes around keys |
| T7 | GUI shows "Could not read config file" | File missing, unreadable, or behind a permission boundary | Check existence + ACL | Move the config file beside the jar |
| T8 | GUI shows "No checks selected" | All check boxes off | Look at the three selection tabs | Click "Reset defaults" or tick at least one check |
| T9 | GUI shows "Run failed" | Pipeline threw — usually corrupt or non-class file in target | Read the dialog message | Remove the offending file from the target dir, or accept the bounded failure |
| T10 | CLI exit code 2 with `"Unknown category"` | Typo in `--only` value | Re-read the message | Use `cursory`, `principle`, `pattern`, or `all` (comma-separated, no spaces) |
| T11 | `mvn test` fails with compile errors after a pull | Stale `target/` from an old Java version | Check JDK with `mvn -version` | `mvn clean test` |
| T12 | `mvn package` succeeds but the GUI doesn't open from the jar | Manifest pointing at wrong class (rare after a refactor) | Inspect with `jar -tf target/*-dependencies.jar | grep MANIFEST -A 5` | Check `<mainClass>` in `pom.xml` (should be `…gui.LinterGuiMain`) |
| T13 | CI build is red but local `mvn test` is green | Test depends on filesystem-case sensitivity, file ordering, or a missing fixture file | Read the CI log | Make the test deterministic; never assume filesystem order |
| T14 | New `LintCheck` class isn't discovered | Class is abstract, isn't on the classpath, or has no no-arg constructor | Run `CheckCatalogTest` and look for the class | Add a no-arg constructor; move it under `rhit.csse.csse374.linter.domain` |

---

## 13. Table of planned changes (requirements not completed in delivery)

These are items planned in earlier milestones or surfaced by user
testing but **not** shipped in M4. None block normal use. All are on
the project Trello board.

| Item | Source | Status | Architectural cost when implemented |
|---|---|---|---|
| HTML report renderer | M1 plan, deferred in M3, deferred in M4 | **Planned** — high value, low risk | Add `ReportRenderer` interface; HtmlRenderer as a sibling impl |
| Source-level line numbers in violations | M2 stretch goal | **Planned** — requires `Violation` field addition | Domain-model change; touches every check |
| `--list-checks` CLI flag | User-testing session #2 (Jamie K., May 9) | **Planned** — small, isolated | New CLI flag printing `CheckCatalog.allChecks()` |
| GUI: header counts on filter-toggle + active search | Q3 manual session (Apr 17), M4 fix sweep | **Closed without fix** — could not reproduce | Likely a redraw-order issue in `ResultsAccordionPanel.rebuild()` |
| Accordion vs raw-report ordering disagreement | Q3 manual session | **Deferred** — late-cycle risk too high | Pick one ordering; apply to both renderers |
| Pre-fill / placeholder text on target-path field | User-testing session #1 (Alex L., May 3) | **Backlog** | Set placeholder in `LinterGuiFrame` constructor |
| `mvn verify` (or explicit `mvn package`) step in CI | M4 self-audit (May 11) | **Backlog** — one-line workflow change | Edit `.github/workflows/ci.yml` |
| Multi-module project scanning (batched runs) | M1 stretch goal | **Out of scope for this release** | Add a `LinterServiceBatch.runMany(List<Request>)` |
| Auto-fix suggestions | Aspirational | **Out of scope** | Would require a write-back transformation layer |
