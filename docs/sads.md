# Software Architecture and Design Specification (SADS)
**Project:** Java Design Linter
**Author:** Ervin Perkowski
**Course:** CSSE 375
**Last updated:** May 13, 2026

---

## 1. Overview

### 1.1 Architectural style: layered tiers

The Java Design Linter follows a **classical three-layer (tiered)
architecture** with strict downward-only dependencies. This is the
"layered tiers" architectural style (in the same family discussed in
CSSE 477 — distinct from microservices, event-driven, client-server,
service-oriented, or pipe-and-filter styles).

The three tiers:

```
       presentation  →  domain  →  data
```

- **presentation** (top tier) — Swing GUI and CLI adapters; user
  interaction, input parsing, output formatting. Owns `LinterService`,
  `LinterCLI`, `LinterGuiFrame`, the renderers, and the `CheckCatalog`.
- **domain** (middle tier) — the lint checks themselves, the
  `LinterHandler` that runs them, and the value types (`LinterResult`,
  `CheckResult`, `Violation`). No knowledge of files, JSON, or Swing.
- **data** (bottom tier) — ASM wrappers (`ASMProject`, `ASMClass`,
  `ASMMethod`, `Instruction`) and the JSON config loader. Owns every
  external-library import: ASM and Jakarta JSON-P live here and
  nowhere else.

**The architecture rule:** *dependencies flow downward only.*
Presentation may import from domain and data; domain may import from
data; data must not import upward. The rule is enforced by code
review on every commit and was followed without exception across M1–M4.

### 1.2 Why this design solves the Problem Statement

The SRS problem statement says the linter must (a) catch recurring
OO design mistakes mechanically, (b) be delivered as a portable
artifact that runs without an installer, and (c) be usable both
interactively (GUI) and headlessly (CLI for CI gating).

The three-layer design solves each of these directly:

- **(a) Mechanical detection.** All check logic lives in the domain
  tier as independent `LintCheck` implementations discovered by
  classpath scanning. Adding a check requires writing one class —
  no GUI plumbing, no CLI flag wiring, no central registry edit.
  This is what makes growing the rule set tractable for a small team.

- **(b) Portable single-artifact delivery.** The data tier owns every
  library dependency (ASM, JSON-P). The presentation tier owns Swing.
  The Maven assembly plugin produces a fat jar containing all of
  these in a single ~1.5 MB file that runs on any Java 17+ machine
  without an installer.

- **(c) Two adapters, one pipeline.** Both the GUI's `RunLinterWorker`
  and the CLI's `LinterCLI.run` delegate to the same
  `LinterService.run(Request) → Response` method. Adding the CLI in
  M3 took one commit (`986063e`) precisely because there was no
  detection logic to duplicate — the GUI never owned any. The same
  property lets the M3 `MockGuiHarness` exercise full user flows
  without Swing.

The architecture also defends the system against an obvious failure
mode: feature creep that tangles tiers. Because data-layer changes
(say, swapping JSON-P for another parser) cannot reach upward, and
because domain-layer checks cannot touch Swing, the cost of
maintenance is bounded — each tier changes for tier-local reasons.

The component-and-connector diagram is in
[`docs/architecture/design.puml`](architecture/design.puml) (rendered
to `class-diagram.png`).

---

## 2. Components

### 2.1 Presentation layer
| Component | Responsibility |
|---|---|
| `LinterGuiMain` | Swing entry point — sets the look-and-feel and shows the frame |
| `LinterGuiFrame` | Main window: target picker, check selection tabs, results tabs, summary banner |
| `RunLinterWorker` | `SwingWorker` that runs the linter off the EDT and posts results back |
| `ResultsAccordionPanel` | Accordion-style results display (delegates filtering to `ViolationFilter`, row rendering to `CollapsibleCheckPanel`) |
| `ViolationFilter` | Pure severity + text filter (no Swing) |
| `CollapsibleCheckPanel` | One expandable check row |
| `SeverityCellRenderer` | Colors severity in tables |
| `CheckCatalog` | Classpath-scanning discovery of `LintCheck` implementations |
| `LinterCLI` | Headless command-line entry: arg parsing, exit codes |
| `LinterService` | Application service shared by GUI and CLI; wraps the ConvertToASM → LinterHandler → LinterOutputText pipeline |
| `LinterOutputText` | Formats a `LinterResult` into plain text |
| `JsonReportWriter` | Formats a `LinterResult` as JSON |
| `HtmlReportWriter` | Formats a `LinterResult` as a self-contained HTML page (inline CSS, no external assets) |
| `ResultsSummary` | Formats `(LinterResult, Duration)` into the one-line banner |

### 2.2 Domain layer
| Component | Responsibility |
|---|---|
| `LintCheck` (interface) | Contract: `run(ASMProject) → CheckResult`, `name()` |
| `Cursory`, `Principle`, `Pattern` | Abstract bases for the three check categories |
| Concrete checks | `EqualsChecker`, `CamelCaseChecker`, `PascalCaseChecker`, `MethodTooLongPattern`, `InterfaceSegregationPrinciple`, `SrpPrinciple`, `PrincipleOfLeastKnowledge`, `OpenClosedPrinciple`, `CohesionAnalyzer`, `HollywoodPrinciple` family, `TemplatePattern`, `StrategyPattern`, `DecoratorPattern`, `AdapterPattern`, `PascalClassName` |
| `LinterHandler` | Runs every check in a list against an `ASMProject` |
| `ConvertToASM` | Reads the disk and builds an `ASMProject` |
| `LinterResult`, `CheckResult`, `Violation`, `SeverityLevel` | Result types |

### 2.3 Data layer
| Component | Responsibility |
|---|---|
| `ASMProject` | Collection of `ASMClass` objects keyed by project path |
| `ASMClass` | Wraps an ASM `ClassNode`; exposes methods, `isInterface()`, `isAbstract()` |
| `ASMMethod` | Wraps an ASM `MethodNode`; performs basic + type-info analysis |
| `Instruction` | Wraps an ASM `AbstractInsnNode` with stack-frame context |
| `StackValuePair` | Top-two-stack-values helper for equals-check analysis |
| `LinterConfig` (interface) | `isEnabled(checkId)`, `optionsFor(checkId)` |
| `MapLinterConfig` | In-memory `LinterConfig` impl with simple-name fallback |
| `JsonLinterConfigLoader` | Parses JSON into a `LinterConfig`; only file with `jakarta.json` imports |

---

## 3. Key design decisions

### 3.1 Layered architecture with downward-only dependencies
The most important architectural rule. Enforced by code review.
Pays off most visibly in:

- The CLI was built in a single commit by reusing `LinterService` — no
  duplicated detection logic.
- Swapping the JSON-P provider would be a single-file change inside
  `JsonLinterConfigLoader`.

### 3.2 LintCheck as an interface with a classpath-scanned catalog
A new check is added by writing a class that extends `Cursory`,
`Principle`, or `Pattern` and giving it a `name()`. `CheckCatalog`
discovers it at startup via reflective scanning — no GUI changes
required.

### 3.3 LinterService as a parameterizable seam
`LinterService` accepts a `Function<String, ASMProject>` project
loader in its constructor. Tests pass a fake; production passes the
real `ConvertToASM`. The class is left non-final so tests can also
use Subclass-and-Override (e.g. `RecordingService` in
`UseCaseDriverTest`).

### 3.4 LinterConfig lives in data, not domain
The domain layer consumes `LinterConfig` but doesn't know it came from
JSON. The interface and its `MapLinterConfig` impl are in the data
layer alongside the loader — the architecture rule "domain depends on
data, never the reverse" is what makes that placement correct.

### 3.5 GUI mock harness as the test driver below presentation
[`MockGuiHarness`](../src/test/java/system/MockGuiHarness.java)
mirrors `LinterGuiFrame`'s user-facing operations but routes through
the production application service. This lets the user-action flow be
tested in plain JUnit, without Swing.

---

## 4. Error handling strategy (Objective 1.7)

Error handling is **layered and intentional**, not bolted on. Each
layer surfaces failures using types appropriate to its abstraction:

### Data layer
- `JsonLinterConfigLoader` throws **typed** exceptions:
  `IOException` for file problems, `ConfigParseException` (custom)
  for malformed JSON or shape mismatches.
- `ConvertToASM` returns an empty class list for non-existent paths
  (fail-closed) and skips files that fail to parse, logging to stderr.
- For corrupted bytecode, ASM may throw unchecked exceptions. These
  bubble up bounded — the JVM never crashes (verified by
  `LinterSecurityTest`).

### Domain layer
- Each `Cursory` / `Principle` / `Pattern` `run()` catches per-class
  exceptions and records them in `CheckResult.analysisErrors`; a
  single broken class never aborts a whole check.

### Presentation layer
- GUI: catches data-layer exceptions and shows user-meaningful dialogs
  ("Could not read config file", "Invalid config file", "Run failed").
  No stack traces leak into UI text.
- CLI: maps exceptions to **distinct exit codes** —
  - `0` clean (no violations)
  - `1` violations found (for CI gating)
  - `2` usage error (missing args, bad flags, unreadable config)
  - `3` runtime error
- Error messages on stderr include the failed input but never an
  internal stack trace.

**Scope.** The error-handling strategy covers:
- All user-supplied paths (target dir, config file)
- All user-supplied content (JSON config text)
- All bytecode read from disk (corrupt class files, non-class files
  with `.class` extension, truncated jars)
- All invariants the CLI cares about (missing args, unknown
  categories)

It does **not** cover:
- Out-of-memory conditions on extreme target sizes (caller's
  responsibility)
- Adversarial classpath manipulation (this is a local tool, not a
  service)

### Representative error-handling code

The CLI is the clearest illustration of layered handling — typed
exceptions from the data layer map to user-friendly messages plus
distinct exit codes:

```java
//From LinterCLI.parseConfig (presentation layer):
try {
    return new JsonLinterConfigLoader().load(Path.of(path));
} catch (IOException e) {
    err.println("Could not read config file '" + path + "': " + e.getMessage());
    return null;
} catch (JsonLinterConfigLoader.ConfigParseException e) {
    err.println("Invalid config file '" + path + "': " + e.getMessage());
    return null;
}
```

Returning `null` causes `run()` to return `EXIT_USAGE` (2). The
end user sees a single readable line; the *internal* exception
type, stack trace, and provider details are not exposed.

The same pattern is mirrored in the GUI's `onLoadConfig`, where the
two exception types map to two slightly different error dialogs
("Could not read config file" vs "Invalid config file"), again
without leaking stack traces into the UI.

### How error handling was tested

- **Automated:** the nine-test `LinterSecurityTest` suite feeds
  hostile inputs (null paths, missing files, malformed JSON, 1.5 MB
  oversized JSON, top-level array, type-coercion attempts, corrupt
  bytecode) and asserts bounded, well-typed failure.
- **By users:** during user-testing session #1, Alex L. wrote a
  config file with no quotes around the check name (a typo, not a
  deliberate attack), loaded it, and got the "Invalid config file"
  dialog. They read the message, fixed the JSON, and re-loaded. The
  error message did its job — no developer support needed.

---

## 5. Quality attribute traceability

Each quality attribute from the SRS maps to a specific architectural
element and a verifying test:

| QA | Architectural element | Verifying test |
|---|---|---|
| Performance | Single-pass `LinterHandler.runLinterAnalysis()` | `LinterPerformanceTest` |
| Reliability — DoS resistance | Bounded JSON parser via Jakarta JSON-P | `LinterSecurityTest.configLoader_oversizedMalformedJson…` |
| Reliability — corrupt input | `ConvertToASM` skip-on-IOException + ASM unchecked bubble-up | `LinterSecurityTest.linterService_corruptedClassBytes…` |
| Security — fail-closed | `JsonLinterConfigLoader.readBoolean` strict type check | `LinterSecurityTest.configLoader_boobyTrappedEnabledField…` |
| Usability | 3-click GUI flow (browse → select → run) | manual user testing (M4 journal §5) |
| Portability | Fat jar via `maven-assembly-plugin` | manual USB demo (M4 demo video) |
| Maintainability | Classpath-scanned `CheckCatalog` | `CheckCatalogTest` |

---

## 6. Evolution since M2

The architecture diagram was substantially refreshed in M3 (commit
`2d4522e`). Key shape changes:

- Removed speculative `ReportRenderer` interface and its
  `Json/Text/Html` siblings that were never implemented.
- Moved configuration loading from a sketched domain `ConfigLoader`
  to the real `data/JsonLinterConfigLoader`.
- Added `LinterService`, `LinterCLI`, `JsonReportWriter`,
  `ResultsSummary`, `ViolationFilter`, `CollapsibleCheckPanel`,
  `LinterConfig`, `MapLinterConfig`.
- Added the `LinterService.Request` and `LinterService.Response`
  inner records as the shared boundary between adapters and the
  service.

---

## 7. Deferred / known limitations (backlog)

Items not delivered in this release; see Trello board "Milestone 4
Items":

- **HTML report renderer.** A future renderer would slot into a
  `ReportRenderer` interface alongside `JsonReportWriter` and
  `LinterOutputText`.
- **Severity-toggle vs search-filter consistency bug.** The header
  counts shown when "Show checks with 0 issues" is toggled
  alongside an active search do not update.
- **Accordion vs raw report ordering disagreement.** The accordion
  sorts violations by location string; the raw report uses emission
  order. Pick one.
- **"Use default output" silent no-op** when no `target/` directory
  exists — needs a status dialog.
- **Inner/anonymous class handling in `PascalClassName`** — ASM
  reports inner classes with `$` in the name; the check should
  verify by stripping the `$<id>` suffix.

Architectural impact of fixing each is minor; no layer boundary
changes required.
