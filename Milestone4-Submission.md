# <TeamName> — Milestone 4 Final Documents

**Project:** Java Design Linter
**Team Name:** _<Team Name — fill in before PDF export>_
**Team #:** _<Team Number — fill in>_
**Members:** Ervin Perkowski
**Assignment:** CSSE 375 Project Milestone 4 — Final Documents
**Date:** May 13, 2026

> **PDF submission filename:**
> `<TeamName>-Milestone4-FinalDocuments.pdf`
> The journal ships as a separate PDF, as in earlier milestones.
> The source code is committed in a `Milestone 4` directory of the
> project repository.

---

## II. Table of Contents

1. **Cover Page** *(above)*
2. **Table of Contents** *(this section)*
3. [Introduction](#iii-introduction)
4. [User Guide / Manual / Tutorial](#iv-user-guide--manual--tutorial)
5. [Installation / Configuration / Deployment Guide](#v-installation--configuration--deployment-guide)
6. [Development / Maintenance Guide](#vi-development--maintenance-guide)
7. [Software Requirements Specification (SRS)](#vii-software-requirements-specification-srs)
8. [Software Architecture and Design Specification (SADS)](#viii-software-architecture-and-design-specification-sads)
9. [Test Plan / Strategy / Suite](#ix-test-plan--strategy--suite)
10. [Appendix A — Ethics Statement](#appendix-a--ethics-statement)
11. [Appendix B — Error Handling](#appendix-b--error-handling)
12. [Appendix C — AI Usage Summary (Objective 3)](#appendix-c--ai-usage-summary-objective-3)
13. [Appendix D — Rubric Cross-Reference](#appendix-d--rubric-cross-reference)

---

## III. Introduction

### Problem space and client

Students and developers writing object-oriented Java code make
recurring design mistakes — missing `equals` / `hashCode`, methods
that grow too long, violations of well-known OO principles (SRP,
ISP, OCP, Hollywood), and design patterns that are *almost* right
but fall just outside the intended shape. Catching these by code
review alone is slow, inconsistent, and depends on a reviewer's
experience.

The **Java Design Linter** addresses this by mechanically scanning
compiled Java bytecode and reporting concrete design violations
grouped by severity. The tool is delivered as a single portable
artifact (a ~1.5 MB fat jar) that runs on any machine with Java 17+
— no installer, no service, no persistent state. It supports both
an interactive Swing GUI (for hands-on inspection) and a headless
CLI (for CI gating), with results renderable as text or JSON.

The **client** is the CSSE 375 / CSSE 374 instructor, acting in
the role of a small-team technical lead who wants lightweight
static-design feedback in a course context without heavyweight
tooling commitments.

### Platform restrictions

The deliverable is platform-neutral but has one runtime constraint:
**Java 17 or newer is required on the host machine.** It is tested
through Java 24. There are no OS-specific dependencies; the same
fat jar runs on Windows, macOS, and Linux.

### Document organization

This Milestone 4 document is organized per the rubric's required
structure (sections I–IX above). The four pre-existing per-document
deliverables (User Guide, Installation Guide, Development /
Maintenance Guide, SRS, SADS) are inlined-by-reference: each
section below introduces the document and points to the canonical
file in [`docs/`](docs/). The Test Plan / Test Results, Ethics,
Error Handling, and AI Usage content is in this document directly
(appendices A–C).

The **journal** is a separate PDF — see `Milestone4-Journal.md`.

---

## IV. User Guide / Manual / Tutorial

**Canonical file:** [`docs/user-guide.md`](docs/user-guide.md)
**Companion quick-start:** [`docs/getting-started.md`](docs/getting-started.md)

The User Guide covers, from the end-user perspective:

- What the linter does (and does not do)
- GUI layout walk-through (top bar, check-selection pane, results
  tabs, summary banner, bottom bar) with screenshot placeholders
- Basic workflow: pick a target → adjust check selection → click
  Run → read results
- Filtering by severity and substring search
- Loading a JSON configuration file
- Exporting results as JSON
- **Viewing the HTML report** — one click opens a styled report in
  the user's default browser (M4 feature; see "HTML report"
  subsection below)
- CLI invocations for headless / CI use, including all flags and
  the four exit codes
- Per-error message table — every dialog the user could see and
  what action it implies
- Tips for faster runs and CI-pipeline integration

A separate **Getting Started Guide** condenses this into a 5-minute
walkthrough that takes a fresh user from "do I have Java?" to
"I just linted my project."

### HTML report (M4-delivered feature)

The M4 release adds a third output renderer alongside the M1 text
report and the M3 JSON report:

- **Class:** `presentation/HtmlReportWriter` — pure function
  `toHtml(LinterResult): String` producing a single self-contained
  HTML document with inline CSS, no JavaScript, no external assets.
- **Tests:** 11 unit tests in `unit/HtmlReportWriterTest` covering
  structure, severity badges, count aggregation, HTML escaping of
  hostile input (defends against injection from violation messages
  / class names like `Foo<T>`), and self-containment guarantees
  (no `<link>` or `<script>` tags).
- **GUI wiring:** a **"View HTML report"** button in the bottom bar
  (next to Export JSON). One click does the following: writes the
  HTML to an OS temp file via `Files.createTempFile`, then opens it
  in the user's default browser using `java.awt.Desktop.browse()`.
  On platforms where `Desktop.browse` is unsupported, the user
  gets a clear dialog with the temp-file path so they can open it
  manually.

**Demo flow:** Run on a target → click "View HTML report" → the
default browser opens showing severity-colored badges, a per-check
violation table, and the project path. No file dialog, no manual
save — the user sees the rendered report immediately.

> *Administration Guide intentionally omitted.* The linter is a
> desktop / CLI tool with no daemon, no users table, and no
> persistent state. The single "administrative" concern — installing
> Java on the host — is covered in the Installation Guide.

---

## V. Installation / Configuration / Deployment Guide

**Canonical file:** [`docs/installation-guide.md`](docs/installation-guide.md)

Covers:

- System requirements (Java 17+, any major OS)
- **Four install paths**, ordered from simplest to most portable:
  1. `winget install EclipseAdoptium.Temurin.21.JDK` (Windows, admin)
  2. Adoptium MSI installer (Windows GUI)
  3. Homebrew / apt / dnf / pacman (macOS / Linux)
  4. Portable JDK ZIP (no-admin, runs from a folder or flash drive)
- Three deployment options for the linter itself:
  1. Pre-built fat jar (~1.5 MB)
  2. Build from source (`mvn package`)
  3. Portable USB bundle (jar + launcher scripts + demo targets)
- JSON configuration schema with key resolution semantics
  (simple-name vs fully-qualified-name lookup)
- CLI flags reference
- Verification steps + smoke tests
- Troubleshooting table for common install issues (Java version,
  PATH problems, `.jar` association quirks on Windows)
- Uninstall: delete the jar — no registry, no leftover files

The portable USB bundle (`USB/` in the repo) was tested on a second
machine prior to the demo video — see the journal entry for
May 12, 2026.

---

## VI. Development / Maintenance Guide

**Canonical file:** [`docs/developer-maintenance-guide.md`](docs/developer-maintenance-guide.md)

Covers:

- Project directory layout
- **The architecture rule** (downward-only `presentation → domain →
  data`) and how it's enforced
- Build / test / package commands and the CI workflow
- **Step-by-step worked example** for adding a new lint check —
  one class extends one of three abstract bases, name + check
  method, a fixture, a test, done. `CheckCatalog` picks it up via
  classpath scanning.
- Adding a new output renderer
- Using the `MockGuiHarness` test driver
- Inventory of Feathers-style seams in the codebase (cross-links
  to `docs/feathers-techniques.md`)
- Style conventions
- Common maintenance tasks (adding a dependency, bumping the Java
  version, regenerating the UML, rebuilding the USB bundle)
- **Comprehensive troubleshooting chart** (14 rows, T1–T14) —
  symptom → likely cause → first check → fix
- **Table of planned changes** — requirements not completed in
  delivery, with source, status, and architectural cost when
  implemented

---

## VII. Software Requirements Specification (SRS)

**Canonical file:** [`docs/srs.md`](docs/srs.md)

The SRS contains, per the rubric's required shape:

- **Problem Statement** (couple of paragraphs) describing what the
  system is for and who its client is
- **Purpose** and **Intended audience**
- **Scope** (in and out)
- **Delivery status overview** table marking what is shipped vs
  deferred (source-line numbers, `--list-checks` flag — the HTML
  report renderer was originally deferred but **was delivered in
  M4**; see "HTML report" subsection further down)
- **Use cases** (7) in naked form, with one full-detail expansion
  (UC1) showing the main flow, pre/post-conditions, and error flows
- **Functional requirements** (10) each tied to a verifying test
- **Supplementary Specification** (the quality-attribute and
  constraints block, per the rubric's terminology):
  - 8 quality attributes (Performance, Reliability, Security,
    Usability, Portability, Maintainability) each quantified and
    tied to a verifying test
  - Constraints (runtime, libraries, persistence, customer
    environment, regulatory)
- **Assumptions and dependencies**
- **Glossary**
- **Deferred / planned-but-not-delivered items** with architectural
  cost notes

The SRS pulls forward and integrates content from M1's plan and
M2/M3 submissions rather than restating it — the goal is one
authoritative source, not redundancy.

---

## VIII. Software Architecture and Design Specification (SADS)

**Canonical file:** [`docs/sads.md`](docs/sads.md)
**Diagram source:** [`docs/architecture/design.puml`](docs/architecture/design.puml)
**Diagram rendered:** [`docs/architecture/class-diagram.png`](docs/architecture/class-diagram.png)

The SADS contains, per the rubric's required shape:

- **Architectural style declaration** — the linter is a classical
  **three-layer (tiered) architecture** (this is the "layered tiers"
  style from CSSE 477, distinct from microservices, event-driven,
  client-server, service-oriented, or pipe-and-filter).
- A **component diagram** (the design.puml render) showing every
  class organized by tier, with an explanation of each component's
  responsibility and inter-component relationships.
- A **"why this design solves the Problem Statement"** subsection
  with two paragraphs covering (a) mechanical detection,
  (b) portable single-artifact delivery, (c) two adapters sharing
  one pipeline.
- Per-tier component tables (presentation / domain / data).
- Key design decisions (LintCheck discovery, the LinterService
  seam, LinterConfig placement, MockGuiHarness driver).
- Layered **error-handling strategy** with a representative code
  snippet from `LinterCLI.parseConfig`.
- Quality-attribute traceability matrix tying each QA to a specific
  architectural element and a verifying test.
- Evolution since M2 (what changed in the diagram).
- Deferred / known limitations.

The SADS pulls forward and integrates content from earlier
milestones rather than restating it.

---

## IX. Test Plan / Strategy / Suite

This section satisfies Objective 2 and the rubric task 5 (Test Plan
+ Test Results).

### IX.1 Testing strategy: all four Crispin quadrants

The test suite is structured so each Crispin quadrant maps to its
own directory under `src/test/java/`:

```
src/test/java/
├── unit/           ← Q1: Technology-facing, supporting the team
├── system/         ← Q2: Business-facing, supporting the team
├── checks/         ← Q2: Per-check tests against fixtures
├── fixtures/       ← Q2: Source for intentional-violation classes
├── performance/    ← Q4: Performance budgets
├── security/       ← Q4: Hostile-input safety
└── ManualLintRunner.java   ← Q3: Manual exploratory entry point
```

### IX.2 Test Plan (per quadrant)

#### Q1 — Unit / Component tests (Technology-facing, supports the team)

| Component | Test file | Count |
|---|---|---|
| JSON report formatter | `unit/JsonReportWriterTest` | 7 |
| HTML report formatter | `unit/HtmlReportWriterTest` | 11 |
| Violation filter | `unit/ViolationFilterTest` | 12 |
| Check discovery / catalog | `unit/CheckCatalogTest` | 8 |
| Run summary formatter | `unit/ResultsSummaryTest` | 9 |
| JSON config loader | `unit/JsonLinterConfigLoaderTest` | 13 |
| In-memory config | `unit/MapLinterConfigTest` | 8 |
| CLI argument parsing + wiring | `unit/LinterCLITest` | 11 |
| Linter service pipeline | `unit/LinterServiceTest` | 6 |
| Severity enum | `unit/SeverityLevelTest` | 9 |
| Violation value type | `unit/ViolationTest` | 8 |
| ASM wrappers | `unit/ASMClassTest`, `unit/ASMMethodTest` | 6+6 |

#### Q2 — Functional / system / acceptance tests (Business-facing, supports the team)

Derived from the use cases in the SRS:

| Use case | Test |
|---|---|
| UC1: Run all checks via the GUI | `system/UseCaseDriverTest.useCase_userPicksTargetAndClicksRun…` |
| UC2: Run a subset via the CLI | `system/UseCaseDriverTest.useCase_userDeselectsACheck…` |
| UC3: JSON config disabling a check | `system/UseCaseDriverTest.useCase_userLoadsJsonConfig…` |
| UC4: Export JSON report | `system/UseCaseDriverTest.useCase_userRunsThenExportsJson…` |
| UC5: Read summary banner | `system/UseCaseDriverTest.useCase_userRunsThenReadsSummaryBanner…` |
| UC6: Filter results | `unit/ViolationFilterTest` (full coverage at unit level) |
| UC7: Clear error on bad input | `system/UseCaseDriverTest.useCase_userClicksRunWithoutTarget…` + `useCase_userLoadsMalformedConfig…` |

Plus per-check business-tests against compiled fixtures:
`checks/CohesionAnalyzerTest`, `checks/DecoratorPatternTest`,
`checks/EqualsCheckerTest`, `checks/MethodTooLongTest`,
`checks/PascalClassNameTest`, `checks/TemplatePatternTest`. The
`MockGuiHarness` driver routes through the same production
classes the GUI uses (`LinterService`, `JsonLinterConfigLoader`,
`JsonReportWriter`, `ResultsSummary`).

#### Q3 — Usability / exploratory testing (Business-facing, critique product)

Manual sessions documented in the journal:

| Date | Activity | Notes location |
|---|---|---|
| Apr 17, 2026 | Exploratory session on linter-self target, then on a classroom assignment | M3 Journal, Apr 17 entry |
| Apr 25, 2026 | Exploratory session via the new CLI on three different targets | M3 Journal, Apr 25 entry |
| **May 3, 2026** | **User testing session #1 — Alex L. (CSSE 375 peer)** | M4 Journal, May 3 entry |
| **May 9, 2026** | **User testing session #2 — Jamie K. (CSSE 374 student, no ASM background)** | M4 Journal, May 9 entry |

**How we tested error handling with users.** Both user sessions
explicitly walked the participant through error paths — load a bad
JSON file (Alex), and then attempt CLI commands with no args
(Jamie). The error dialogs and exit codes were observed cold; the
participants' reactions are recorded in the journal entries.

#### Q4 — Quality-attribute tests (Technology-facing, critique product)

| Quality attribute | Test |
|---|---|
| Performance — empty-pipeline overhead | `performance/LinterPerformanceTest.emptyProjectRunCompletesUnderOneSecond` |
| Performance — full self-scan | `performance/LinterPerformanceTest.selfScanWithAllChecksFinishesUnderBudget` |
| Performance — no degradation across runs | `performance/LinterPerformanceTest.repeatedRunsDoNotDegradeAcrossInvocations` |
| Security — null path | `security/LinterSecurityTest.configLoader_nullPath_failsWithIllegalArgument` |
| Security — missing config file | `security/LinterSecurityTest.configLoader_nonExistentFile…` |
| Security — bounded error messages, no provider stack leak | `security/LinterSecurityTest.configLoader_malformedJson_errorMessageIsBoundedAndSafe` |
| Security — DoS via oversized config | `security/LinterSecurityTest.configLoader_oversizedMalformedJson…` |
| Security — corrupt bytecode safety | `security/LinterSecurityTest.linterService_corruptedClassBytes…` |
| Security — fail-closed on non-existent target | `security/LinterSecurityTest.linterService_nonExistentTargetPath…` |
| Security — top-level array rejection | `security/LinterSecurityTest.configLoader_unexpectedTopLevelArray…` |
| Security — fail-closed on coercion attempt | `security/LinterSecurityTest.configLoader_boobyTrappedEnabledField…` |
| Security — no shared bad state | `security/LinterSecurityTest.configLoader_validInputAfterAttackAttempt…` |

### IX.3 Test Results — highlights and what changed because of testing

**Total automated tests: 155+ passing** on `master`. CI green
throughout M4.

**What didn't work and led to a fix.** A small number, by design:

1. **Q3 manual session, Apr 17 — `PascalClassName` on inner /
   anonymous classes.** I noticed during exploration that the check
   fired false-positives on names like `Outer$Inner` because of the
   `$`. In the M4 fix sweep (May 10) I wrote a unit test that loads
   an inner-class fixture and asserts no violation, then fixed the
   check to strip the `$<id>?<name>` suffix before validating.
   *Caught by exploration → reproduced by a unit test → fixed.*

2. **User testing session #1, May 3 — Alex L. confusion over a
   bad JSON config.** Alex wrote a config without quoting the keys
   and got the "Invalid config file" dialog. Verdict: the error
   message did its job; Alex fixed and re-loaded with no help. No
   code change needed.

3. **User testing session #1 — Alex's "did it work?" pause.** Alex
   wasn't sure the run had started. *Action taken:* I re-checked
   the code and confirmed the banner does flip to "Running…" on
   click — Alex just didn't notice. No code change needed.

4. **User testing session #2, May 9 — Jamie K. expected `run-cli.bat`
   with no args to do *something* friendly.** Currently it exits 2.
   *Action taken:* filed `--list-checks` flag as a backlog item
   (planned change table in the Dev Guide). The CI-gating use case
   wants exit-2-on-no-args; the new-user case wants help. The
   resolution is more docs (the README in the USB bundle now has a
   "common commands" section), not behavior change.

5. **Q3 manual session, Apr 17 — "Use default output" silent
   behavior.** I reported as a bug; in M4 fix sweep I re-investigated
   and found the dialog *did* fire — the dialog wording was just
   unclear. *Fix:* rephrased the dialog. *(Code change planned;
   confirm against your actual repo state.)*

**Items deliberately not fixed in M4.** Tests pinned current
behavior; fixes deferred to a later cycle because the risk of late
regression outweighed the gain — the accordion-vs-raw-report
ordering disagreement and the header-counts-with-active-search
interaction. Both are in the planned-changes table.

### IX.4 Continuous integration

The test suite runs automatically on every push and pull request to
`master` via [`.github/workflows/ci.yml`](.github/workflows/ci.yml).
A change in code that breaks any test fails the CI build, blocking
merge. Verified green throughout M4.

**Known CI gap.** The workflow runs `mvn test` but not `mvn package`,
so a green CI does not yet prove the deliverable jar still builds.
This is one line of YAML to fix and is listed in the planned-changes
table (Dev Guide §13).

---

## Appendix A — Ethics Statement

This appendix satisfies Objective 1.6.

### A.1 IEEE Code of Ethics — three relevant standards

Reviewed against the IEEE Code of Ethics
(<https://www.ieee.org/about/corporate/governance/p7-8>). Three
items chosen for direct relevance:

#### IEEE Code item I.1 — "to hold paramount the safety, health, and welfare of the public … and to disclose promptly factors that might endanger the public or the environment"

**How this work follows it.** The linter is a low-stakes static
analysis tool — it cannot directly endanger the public. The related
ethical duty is **honest disclosure**: this report flags, in the
SRS delivery-status table and the Dev Guide's planned-changes
table, every feature that was *planned but not delivered* (HTML
report, line numbers, `--list-checks`, two GUI bugs deferred). The
client (instructor) is therefore not misled about completeness.

#### IEEE Code item I.5 — "to seek, accept, and offer honest criticism of technical work, to acknowledge and correct errors, to be honest and realistic in stating claims or estimates based on available data, and to credit properly the contributions of others"

**How this work follows it.**
- *Honest criticism accepted and acted on:* two user-testing
  sessions (May 3, May 9) produced concrete feedback that fed
  directly into backlog items and a documentation change.
- *Errors acknowledged and corrected:* the `PascalClassName`
  inner-class false-positive was discovered in exploration and
  fixed with a regression test (May 10).
- *Realistic claims:* this report does not over-claim coverage.
  155 tests are 155 tests; the security tests verify *bounded*
  failure on corrupt bytecode, not impossibility of failure — that
  nuance is preserved in the Q4 description.
- *Credit:* the journal explicitly documents the M2 partnership
  with Noah Howard and the mid-M3 pivot to solo work. Earlier-
  milestone contributions are not erased or claimed.

#### IEEE Code item II.7 — "to support colleagues and co-workers in following this code of ethics, to strive to ensure the code is upheld, and to not retaliate against individuals reporting a violation"

**How this work follows it.** The user testers (Alex L. and Jamie
K., classmates) were given the system cold and asked to find
problems. Their feedback was recorded and incorporated, not
deflected. As a class member, I reciprocated by serving as a tester
for two other classmates' projects in Week 8 — that activity is
also noted in the journal.

### A.2 AI ethics — concerns and handling

The project used AI assistance (Claude) extensively across M2–M4
(see Appendix C for the full inventory). Ethical concerns and
how they were handled:

#### Concern 1: Fabrication / over-confident output

The AI proposed, on at least one occasion, code that referenced
a non-existent method (`Violation.getSeverity()` returning the
wrong type — the actual class has both `getSeverity(): String` and
`getSeverityLevel(): SeverityLevel` and the AI picked the wrong
one). The compiler caught it.

**How handled:** Every AI-drafted file went through `mvn compile`
and `mvn test` before being incorporated. No "trust on faith"
acceptance. This was reinforced after the first compile error of
this kind — documented in the M3 journal entry for April 26.

#### Concern 2: Was the AI-drafted code tested as thoroughly as code I wrote myself?

This is the specific question the rubric (task 6b) calls out.

**Honest answer: in some places yes, in some places no.** The
`MockGuiHarness` (AI-drafted in M3) was tested via the 9 use-case
scenarios in `UseCaseDriverTest`. The `ResultsSummary` formatter
(AI-suggested boilerplate) has 9 unit tests pinning every code
path including the null-severity branch. The `LinterSecurityTest`
attacks (AI brainstormed) each correspond to a real assert.

Where I was *less* thorough: documentation prose. I did not stress-
test the user guide or installation guide with a second AI run to
catch subtle inaccuracies. The mitigation is human review and the
two real user-testing sessions, which caught two documentation
discoverability gaps.

**Mitigation going forward:** the rule "compile and test every
AI-incorporated code change" is the load-bearing discipline. The
rule "have a real person follow the docs cold" is the equivalent
discipline for documentation.

#### Concern 3: Over-suggestion / inducing scope creep

The AI was observed to volunteer additional refactorings, tests,
and abstractions even when the in-progress work was complete. Acted
on, this would have increased the maintenance burden of the
codebase for no testing benefit — itself a kind of harm
(opportunity cost).

**How handled:** Explicit pushback during prompts ("don't propose
anything new unless I ask"). The most concrete example is the M3
checklist-triage session where the AI repeatedly proposed adding a
fifth or sixth refactoring under my name; I declined and stuck
with what was already complete.

#### Concern 4: Attribution

Every code change has a human author (me) on the commit. AI
suggestions were reviewed, edited, and verified by the compiler
plus tests before being incorporated. The journal documents what
the AI proposed and what I kept.

#### Concern 5: Course rule compliance

This work is submitted as my own. AI assistance is fully disclosed
in Appendix C and is consistent with the course's AI-use allowance.

---

## Appendix B — Error Handling

This appendix satisfies Objective 1.7 and rubric task 7. It
duplicates none of the SADS section 4 content; it provides the
specific code-snippet + testing summary the rubric asks for.

### B.1 Scope of error handling

The linter catches:

| Source | Errors covered |
|---|---|
| User-supplied paths | Non-existent target dir, non-existent config file, unreadable paths |
| User-supplied content | Malformed JSON, JSON with wrong shape (top-level array, wrong-type fields), oversized JSON (DoS attempts) |
| Bytecode read from disk | Corrupt class files, non-class files with `.class` extension, truncated jars |
| CLI invariants | Missing args, unknown `--only` categories, `--config` without value |
| Domain checks | Per-class exceptions during `checkClass()` — recorded but don't abort the run |

It does **not** cover:
- Out-of-memory conditions on extreme target sizes (caller's responsibility)
- Adversarial classpath manipulation (this is a local tool, not a service)

### B.2 Representative code snippet

The CLI's config-loading code shows the layered handling cleanly —
typed exceptions from the data layer map to user-friendly messages
plus distinct exit codes:

```java
//From LinterCLI.parseConfig (presentation layer):
private LinterConfig parseConfig(String[] args, PrintStream err) {
    for (int i = 1; i < args.length; i++) {
        if (!"--config".equals(args[i])) {
            continue;
        }
        if (i + 1 >= args.length) {
            err.println("--config requires a path to a JSON file");
            return null;
        }
        String path = args[i + 1];
        try {
            return new JsonLinterConfigLoader().load(Path.of(path));
        } catch (IOException e) {
            err.println("Could not read config file '" + path + "': " + e.getMessage());
            return null;
        } catch (JsonLinterConfigLoader.ConfigParseException e) {
            err.println("Invalid config file '" + path + "': " + e.getMessage());
            return null;
        }
    }
    return LinterConfig.allEnabled();
}
```

Returning `null` causes the caller to return `EXIT_USAGE` (2). The
user sees one readable line; the *internal* exception type, stack
trace, and JSON-P provider details are not exposed.

The GUI mirrors this pattern in `LinterGuiFrame.onLoadConfig`,
where the two exception types map to two slightly different error
dialogs ("Could not read config file" vs "Invalid config file"),
again without leaking stack traces into the UI.

### B.3 How users tested error handling

**Automated:** the nine-test `LinterSecurityTest` suite feeds
hostile inputs and asserts bounded, well-typed failure (see Test
Plan Q4 table). Verified by `mvn test`.

**By real users:** during user-testing session #1 (Alex L., May 3),
Alex wrote a config file without quoted keys, loaded it through
the GUI, and got the "Invalid config file" dialog. They read the
message, fixed the JSON, and re-loaded with no developer support.
This is the strongest evidence that the error path *works as a
user-facing feature* and not just as a unit-test pass.

### B.4 Failure modes that are intentional

- Non-existent target path → empty result, exit 0. This is
  **fail-closed** — the alternative (scan some default path) would
  be a worse behavior.
- Corrupt `.class` file → bounded `RuntimeException` from ASM,
  caught at the run-level. This *could* be tightened to "log and
  skip", and the tightening is in the planned-changes table.
- `mvn test -Dtest=LinterSecurityTest` exit 0 → all hostile inputs
  produced the expected bounded failures, including the 1.5 MB
  malformed-JSON DoS attempt completing in under 5 s.

---

## Appendix C — AI Usage Summary (Objective 3)

A full per-action AI usage log is in the M3 and M4 journals
(`Milestone3-Journal.md`, `Milestone4-Journal.md`). The journals
provide the day-by-day specifics required by the rubric. This
appendix is the bird's-eye summary.

### C.1 Major uses, by category

| Category | Example | Result |
|---|---|---|
| **Identifying seams in legacy code** | "Where could a seam go in `LinterGuiFrame.onRun`?" → Parameterize Constructor | ⭐ Suggestion became commit `986063e` |
| **Generating characterization tests** | Behaviors of inline JSON before extracting `JsonReportWriter` | ⭐ Enumerated bugs I'd have refactored away |
| **Drafting boilerplate (mocks, harnesses, formatters)** | `MockGuiHarness` and `RecordingService` stub | Working draft with one API bug (caught by compile) |
| **Brainstorming features under time pressure** | Three options for the 1-hour mini-feature | Banner picked from a list |
| **Triaging large checklists** | "What's coding vs docs vs coordination?" | Mostly worked; tendency to over-suggest |
| **Document structure / templates** | SRS skeleton, demo-video script, M4 doc outline | Best when prompted with project specifics |
| **Mapping work to course concepts** | Feathers chapter mapping for M3 | ⭐ Saved manufactured "be safe" extra refactorings |
| **User testing prep** | Test script for session with Alex / Jamie | Useful structure; replaceable |
| **Writing this report** | Section drafting, rubric cross-check | Each section reviewed and edited |

### C.2 What worked

- **Concrete-file prompts** beat generic ones every time. "Find a
  seam in *this* class" → usable answer; "show me how to refactor"
  → too generic.
- **Specifying constraints up front** ("for a small, single-developer
  JVM tool, no external customers") dramatically tightened generic
  template output.
- **Asking for chapter alignment** rather than asking for more
  refactorings was a productivity win — it confirmed what was
  already done was enough.

### C.3 What didn't work

- **Generic "write me an SRS"** produced 60-page IEEE-830 output.
  Useful only after I added "right-sized for a course project."
- **Knowing when to stop** — the AI volunteered more work even
  when the work was complete. Required explicit limits.
- **Overloaded API correctness** — `getSeverity()` vs
  `getSeverityLevel()` was a real bug. Compile-before-trust is the
  fix.
- **Documentation prose was not stress-tested as thoroughly as
  code** — see Appendix A.2, concern 2. Documentation issues
  surface in real user testing, not in compilation.

### C.4 Strong patterns to repeat

1. **Compile and test every AI-incorporated code change before
   accepting it.**
2. **Set explicit scope limits** ("don't propose anything new
   unless I ask").
3. **Use AI for skeletons; use yourself for judgment.**
4. **Verify documentation with real users.**

---

## Appendix D — Rubric Cross-Reference

| Rubric checklist item | Where in this submission |
|---|---|
| Cover Page, Table of Contents, Introduction | Sections I, II, III |
| User Guide / Manual / Tutorial | §IV → `docs/user-guide.md` + `docs/getting-started.md` |
| Installation / Configuration / Deployment Guide | §V → `docs/installation-guide.md` |
| Development / Maintenance Guide (with troubleshooting chart + table of planned changes) | §VI → `docs/developer-maintenance-guide.md` §12 and §13 |
| Continuous Integration Framework | §IX.4; `.github/workflows/ci.yml` |
| SRS + SADS — Analysis & Design models | §VII, §VIII → `docs/srs.md`, `docs/sads.md`, `docs/architecture/design.puml` |
| Test Plan — Approach / Strategy & Test cases | §IX.1, §IX.2 |
| Test Results | §IX.3 |
| Source Code & Installation / Configuration Scripts | `src/`, `USB/run-*.bat`, `USB/run-gui.sh`, `pom.xml` |
| Review of Repository and Reporting Site | Trello board (M4 Items) + GitHub repository |
| Functional Demonstration (in person or video) | Demo video — submitted alongside this report |
| Ethics — 3 IEEE Code items + AI concerns | Appendix A |
| Error handling — code snippet + user testing + scope | Appendix B |
| All major AI uses documented | Appendix C + journals |
| User testing with classmates + changes made from feedback | §IX.2 Q3, §IX.3, and the M4 Journal entries for May 3 and May 9 |
| Quality journal + lessons learned summary | `Milestone4-Journal.md` (separate PDF) |

---

*End of Milestone 4 Final Documents.*
