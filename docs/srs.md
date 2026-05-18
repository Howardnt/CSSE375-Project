# Software Requirements Specification (SRS)
**Project:** Java Design Linter
**Author:** Ervin Perkowski
**Course:** CSSE 375 — Software Construction and Evolution
**Last updated:** May 13, 2026

---

## 1. Introduction

### 1.1 Problem Statement
Students and developers writing object-oriented Java code make
recurring design mistakes — missing `equals`/`hashCode`, methods that
grow too long, violations of well-known OO principles (SRP, ISP, OCP,
Hollywood), and design-pattern uses that are *almost* right but fall
just outside the intended shape. Catching these by code review alone
is slow, inconsistent, and depends on a reviewer's experience.

The **Java Design Linter** solves this by mechanically scanning
compiled Java bytecode and reporting concrete violations grouped by
severity. The tool is delivered as a single portable artifact (a
~1.5 MB fat jar) that runs on any machine with Java 17+ — no
installer, no service, no persistent state. It supports both an
interactive Swing GUI (for hands-on inspection) and a headless CLI
(for CI gating), with results renderable as text or JSON.

The client for this project is the CSSE 375 / CSSE 374 instructor,
acting in the role of a small-team technical lead who wants
lightweight static-design feedback in a course context without
heavyweight tooling commitments.

### 1.2 Purpose
This SRS describes the functional and non-functional requirements
of the Java Design Linter as delivered in Milestone 4. It is the
authoritative source for what the system does and how its quality
is measured. Functionality that was originally specified but is
**not** in the delivered system is marked **(deferred)** in the
relevant sections.

### 1.3 Intended audience
- **Students** in CSSE 375 and 374 inspecting their own work
- **Instructors** grading object-oriented design assignments
- **Developers** who want a lightweight, install-free static check
  layered into a CI pipeline

### 1.4 Scope
In scope:
- Read compiled `.class` files (single file, directory, or jar)
- Run a configurable catalog of "checks" (Cursory, Principle, Pattern)
- Report violations through GUI, CLI text, or JSON
- Accept a JSON configuration file to enable/disable individual checks
- Persist no state between runs

Out of scope (intentionally):
- Reading `.java` source files
- Compiling source on behalf of the user
- Auto-fixing detected issues
- Network / remote analysis

### 1.5 Delivery status overview
| Capability | Status |
|---|---|
| Bytecode discovery + ASM-based analysis | ✅ Delivered |
| 15+ checks across Cursory / Principle / Pattern categories | ✅ Delivered |
| Swing GUI | ✅ Delivered |
| CLI mode (`LinterCLI`) | ✅ Delivered (M3) |
| JSON configuration file | ✅ Delivered (M3) |
| Run summary banner | ✅ Delivered (M3) |
| Severity levels + colored rendering | ✅ Delivered (M2) |
| GitHub Actions CI | ✅ Delivered (M2) |
| Portable USB bundle | ✅ Delivered (M4) |
| HTML report renderer + browser launch | ✅ Delivered (M4) |
| Source-level line numbers in violations | **Deferred** — see §7 |
| `--list-checks` CLI flag (user-requested in M4 testing) | **Deferred** — see §7 |
| Auto-fix suggestions | **Out of scope** |

---

## 2. Use cases (naked form)

| ID | Use case | Primary actor |
|---|---|---|
| UC1 | Run all checks on a compiled project via the GUI | User |
| UC2 | Run a subset of check categories via the CLI | User / CI |
| UC3 | Use a JSON config to disable specific checks | User |
| UC4 | Export run results as a JSON report | User |
| UC5 | Read a summary of severities + duration after a run | User |
| UC6 | Filter the displayed results by severity or search text | User |
| UC7 | Receive a clear error when the chosen path doesn't exist or the config is malformed | User |

### Use case detail (UC1 — full)

**Actor:** User running the GUI.
**Pre-conditions:** Java 17+ installed; the user has a directory of
compiled `.class` files.
**Main flow:**
1. User launches the GUI (`java -jar linter.jar` or double-click).
2. User clicks **Browse…** and selects a directory of `.class` files.
3. User leaves all checks selected (default) and clicks **Run**.
4. The system analyses the bytecode and displays:
   - A bold one-line summary banner above the tabs
     (`N errors · N warnings · N info · N total · ran in X s`).
   - A **Summary** tab with run metadata.
   - A **Results** tab grouping violations by check, collapsible.
   - A **Raw report** tab with the formatted text report.
**Post-conditions:** The result is held in memory; the user can export it.
**Error flows:**
- Empty target path → warning dialog ("Missing target").
- Path doesn't exist → same dialog.
- No checks selected → warning dialog ("No checks selected").
- Run fails internally → warning dialog with the exception message.

(UC2–UC7 follow the same template; detail kept in
[`UseCaseDriverTest.java`](../src/test/java/system/UseCaseDriverTest.java) where each is
scripted end-to-end.)

---

## 3. Functional requirements

| FR | Requirement | Verified by |
|---|---|---|
| FR-1 | Discover all `LintCheck` implementations on the classpath at startup | `CheckCatalogTest` |
| FR-2 | Allow each check to be individually enabled or disabled | `JsonLinterConfigLoaderTest`, `UseCaseDriverTest` |
| FR-3 | Categorize checks as Cursory, Principle, or Pattern | `CheckCatalogTest` |
| FR-4 | Accept a target path that is a directory, a single `.class`, or a jar | `LinterEndToEndTest` |
| FR-5 | Run the same pipeline from GUI and CLI without duplicating detection logic | `LinterServiceTest`, `LinterCLITest` |
| FR-6 | Emit a JSON report of all violations | `JsonReportWriterTest` |
| FR-6b | Emit a self-contained HTML report of all violations and open it in the default browser | `HtmlReportWriterTest` (renderer); manual demo (browser launch) |
| FR-7 | Show severity-grouped counts and run duration after each GUI run | `ResultsSummaryTest` |
| FR-8 | Filter results by severity and substring search in the GUI | `ViolationFilterTest` |
| FR-9 | Read configuration from a JSON file, isolated to the data layer | `JsonLinterConfigLoaderTest` |
| FR-10 | Return distinct exit codes from the CLI: 0 clean, 1 violations, 2 usage, 3 error | `LinterCLITest` |

---

## 4. Supplementary Specification (Quality Attributes + Constraints)

This section captures the non-functional requirements. Quality
attributes are quantified where possible and tied to verifying tests.

### 4.1 Quality attributes

| QA | Target | Verified by |
|---|---|---|
| **Performance** | Lint the linter's own ~85 compiled classes with every check enabled in **under 30 seconds** | `LinterPerformanceTest.selfScanWithAllChecksFinishesUnderBudget` |
| **Performance (overhead)** | Empty-pipeline run completes in **under 1 second** | `LinterPerformanceTest.emptyProjectRunCompletesUnderOneSecond` |
| **Reliability — DoS resistance** | A 1.5 MB malformed JSON config blob must fail within a **5-second budget** | `LinterSecurityTest.configLoader_oversizedMalformedJson_failsBoundedNotHangs` |
| **Reliability — corrupt input** | A `.class` file containing random bytes must produce a bounded RuntimeException, never a JVM crash | `LinterSecurityTest.linterService_corruptedClassBytesInTarget_failsBoundedNotCrashes` |
| **Security — fail-closed** | A string `"true"` in a boolean `enabled` field must be rejected, not silently coerced | `LinterSecurityTest.configLoader_boobyTrappedEnabledField_doesNotBypassToTrue` |
| **Usability** | A user can lint a project in three clicks: Browse → select dir → Run | manual user testing (see Section 5 of M4 journal) |
| **Portability** | Run on any machine with Java 17+ from a single fat jar (1.5 MB), no installer required | manual portability test from USB |
| **Maintainability** | New `LintCheck` implementations are discovered automatically at startup; no GUI code change needed | `CheckCatalogTest` discovery tests |

---

### 4.2 Constraints

- **Runtime:** Java 17 (minimum), per `pom.xml` `<source>17</source>`. Tested through Java 24.
- **Bytecode parsing:** ASM 9.9 (`org.ow2.asm`)
- **JSON parsing:** Jakarta JSON-P 2.1.3 + Parsson 1.1.7
- **GUI toolkit:** Swing (bundled with the JDK; no external GUI dependency)
- **Build:** Maven (no Gradle / Bazel alternative)
- **Network:** No network access required at runtime
- **Persistence:** None — the tool writes no files outside what the user explicitly exports
- **Customer environment:** No assumption about a specific OS or hardware platform; the deliverable is a single platform-neutral jar
- **No governmental regulations** apply (no PII, no security-sensitive data, no health/financial scope)

---

## 5. Assumptions and dependencies

- Target machine has Java 17 or newer
- Target classes have been compiled with debug info (`-g`) and parameter
  info (`-parameters`); without these, some name-based checks degrade
- Compiled classes use ASM-compatible bytecode (standard `javac` output)

---

## 6. Glossary

| Term | Meaning |
|---|---|
| **Check** | A single lint rule (e.g. `EqualsChecker`) implementing `LintCheck` |
| **Category** | One of Cursory, Principle, Pattern (groups related checks in the GUI) |
| **Violation** | A single finding emitted by a check, carrying message, location, and severity |
| **Severity** | `ERROR` / `WARNING` / `INFO` — sets colors and ordering |
| **Fixture** | A compiled Java class designed to deliberately trip a specific check (used by tests) |
| **LinterConfig** | A plain Java object the data layer produces from a JSON config file |

---

## 7. Deferred / planned-but-not-delivered items

Items planned in earlier milestones but **not** shipped in the delivered
system. None block normal use; all are tracked on the project Trello
board.

| Item | Why deferred | Architectural fit when added |
|---|---|---|
| ~~HTML report renderer~~ | **Delivered in M4** — see `HtmlReportWriter`, `HtmlReportWriterTest`, and the GUI's "View HTML report" button | n/a |
| **Source-level line numbers in violations** | ASM provides line info via debug attributes; threading it through `Violation` is a domain-model change | New `Violation` field + propagation through every check |
| **`--list-checks` CLI flag** | Surfaced in user-testing session #2 (Jamie, May 9). Backlogged | New CLI flag that prints the `CheckCatalog` contents |
| **GUI bug: header counts on filter-toggle + active search** | Could not reproduce reliably in M4 fix sweep; closed without fix | Likely a redraw-order issue in `ResultsAccordionPanel.rebuild()` |
| **Accordion vs raw-report ordering disagreement** | Touching both renderers risked late regression; deferred to a stable cycle | Pick one ordering, apply to both renderers |
| **Auto-fix suggestions** | Always out of scope for this release | Would require a transformation layer between domain and a write-back data adapter |
| **Multi-module project support** | Single-target directory scanning covers all observed usage | Would require batching `LinterService.run` invocations |
