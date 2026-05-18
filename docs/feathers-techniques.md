# Feathers Techniques Applied — Milestone 3

This document maps the Milestone 3 work back to specific chapters of
*Working Effectively with Legacy Code* by Michael Feathers, satisfying
Objective 1.5 ("pick the problem Feathers solves in any 6 of his chapters
and apply his techniques to resolve those problems").

Each section names the chapter, the technique, where it lives in the code,
and the commit it landed in. Where useful, a one-line "before / after" is
included so the contrast is obvious.

---

## Ch. 6 — "I Don't Have Much Time and I Have to Change It"
### Technique: Sprout Class

**Problem Feathers solves.** When you need to add behavior to code that's
already complicated, modifying the existing class risks breaking things
you can't easily test. Feathers' answer: sprout the new behavior in a
*new* class so the old code stays untouched and the new class is
test-driven from day one.

**Where applied.** The run-summary banner feature.

- New class: [src/main/java/.../presentation/ResultsSummary.java](../src/main/java/rhit/csse/csse374/linter/presentation/ResultsSummary.java)
- Commit: `b0279a6` ("one hour feature done")
- Tests for the sprouted class: [src/test/java/unit/ResultsSummaryTest.java](../src/test/java/unit/ResultsSummaryTest.java)
  (9 unit tests, written *before* wiring into the GUI)

`LinterGuiFrame.onRunCompleted()` was already a tangle of Swing calls; instead
of adding count-by-severity and timing logic inside it, the formatting was
sprouted into `ResultsSummary.format(LinterResult, Duration)` — a pure
function with no Swing knowledge. The GUI only had to call one method:

```java
summaryBanner.setText(resultsSummary.format(result, runDuration));
```

The sprouted class is fully testable on its own (9 unit tests, no Swing
required); the existing `onRunCompleted` flow is unchanged in shape.

---

## Ch. 13 — "I Need to Make a Change, but I Don't Know What Tests to Write"
### Technique: Characterization Tests

**Problem Feathers solves.** Before refactoring untested legacy code, you
need a safety net that locks in *current* behavior — even bugs — so the
refactoring is verifiably "no behavior change." Feathers calls these
characterization tests: tests that pin down what the code *actually*
does, not what someone wishes it did.

**Where applied.** [src/test/java/unit/JsonReportWriterTest.java](../src/test/java/unit/JsonReportWriterTest.java).

- Commit: `fd2a584` ("Extract JsonReportWriter from LinterGuiFrame")
- The test file's docstring explicitly said it locked in pre-existing
  quirks — no general JSON escaping, and the `"message"` field carrying
  the location string — so the extraction was *purely* a refactoring,
  not a behavior change.

> *Original quote from the M3 file:* "These tests lock in the current
> output format, including two pre-existing quirks preserved by this
> pure refactoring: lack of general JSON escaping, and the 'message'
> field carrying the location string. Fixing those is a separate
> commit."

This is the textbook Feathers framing: capture the bug-and-all behavior
first, refactor inside that net, fix the bugs in a follow-up commit.

**M4 follow-up.** That "separate commit" landed in M4. Both pre-existing
bugs were fixed:

- The `"message"` JSON field now emits the actual violation message
  (it previously emitted the location string).
- All string fields are now JSON-escaped via a new `escape()` helper
  (quotes, backslashes, newlines, tabs, control chars).

The characterization tests were updated to pin the *correct* behavior
rather than the legacy quirks. Three new tests were added:
`messageFieldEmitsTheActualViolationMessage`,
`backslashAndControlCharactersAreEscaped`, and
`projectPathWithSpecialCharsIsEscaped`. The full sequence —
characterize-then-extract-then-fix — is exactly the Feathers-style
"work inside the safety net" loop.

---

## Ch. 14 — "Dependencies on Libraries Are Killing Me"
### Technique: Wrap external library behind a stable internal abstraction

**Problem Feathers solves.** Direct calls to a third-party library scatter
across your codebase, so swapping the library, mocking it in tests, or
upgrading it requires touching everywhere. Feathers' answer: wrap the
library in a thin internal abstraction so only one file knows the library
exists.

**Where applied.** Jakarta JSON-P (Parsson) is reached *only* through
[src/main/java/.../data/JsonLinterConfigLoader.java](../src/main/java/rhit/csse/csse374/linter/data/JsonLinterConfigLoader.java).

- Commit: `dfd84a6` ("Milestone 3 done")
- Domain code receives a [LinterConfig](../src/main/java/rhit/csse/csse374/linter/data/LinterConfig.java) interface — it
  has no idea the data came from JSON.
- Verification: `grep` for `jakarta.json` across the codebase; the only
  hits are inside `JsonLinterConfigLoader.java`. Swapping Parsson for
  another JSON-P provider — or for a YAML/TOML loader — is a single-file
  change.

This also satisfies the architecture rule "data-layer parsing must not
leak into domain or presentation."

---

## Ch. 22 — "I Need to Change a Monster Method"
### Technique: Extract Method

**Problem Feathers solves.** Long methods are hard to test because they
mix many concerns; you can't drive any single concern in isolation.
Extract Method splits the monster into named sub-steps, each of which can
be reasoned about and tested directly.

**Where applied.** `CheckCatalog.toDescriptor` was 46 lines of nested
reflection.

- Commit: `efc5b6f` ("Extract helpers from CheckCatalog.toDescriptor")
- File: [src/main/java/.../presentation/gui/CheckCatalog.java](../src/main/java/rhit/csse/csse374/linter/presentation/gui/CheckCatalog.java)
- Tests: [src/test/java/unit/CheckCatalogTest.java](../src/test/java/unit/CheckCatalogTest.java)

**Before** (one method, 46 lines, doing five things):
- load class by name
- check it implements LintCheck
- reject base classes
- categorize by superclass
- build supplier + display name

**After** (`toDescriptor` is now 11 lines that read top-to-bottom):

```java
private static CheckDescriptor toDescriptor(String className, ClassLoader cl) {
    Class<? extends LintCheck> checkClass = loadConcreteCheckClass(className, cl);
    if (checkClass == null) return null;
    Category category = categorize(checkClass);
    if (category == null) return null;
    Supplier<LintCheck> supplier = createSupplier(checkClass);
    String displayName = computeDisplayName(supplier, checkClass);
    return new CheckDescriptor(checkClass.getName(), displayName, category, true, supplier);
}
```

The five concerns are now five named methods (`loadConcreteCheckClass`,
`isConcreteLintCheck`, `isRejectedBaseClass`, `createSupplier`,
`instantiateCheck`), each independently testable.

---

## Ch. 23 — "How Do I Know That I'm Not Breaking Anything?"
### Technique: Preserve Signatures (and byte-for-byte preservation)

**Problem Feathers solves.** When you refactor, the safest move is one that
the compiler can mechanically verify — preserve the signature of what
you're moving so callers don't change. Combined with characterization
tests (Ch. 13), this guarantees the refactor is behavior-neutral.

**Where applied.** Extracting `JsonReportWriter` from `LinterGuiFrame`.

- Commit: `fd2a584` ("Extract JsonReportWriter from LinterGuiFrame")
- The new [JsonReportWriter](../src/main/java/rhit/csse/csse374/linter/presentation/JsonReportWriter.java)
  emits *byte-for-byte* the same JSON the inline version produced —
  including two pre-existing bugs the file's javadoc calls out
  explicitly. Tests in `JsonReportWriterTest` enforce this.
- The call site in `LinterGuiFrame.onExportJson()` shrunk from 35
  lines of inline string-building to one line:

  ```java
  out.print(new JsonReportWriter().toJson(lastResult));
  ```

By preserving the exact output (signatures of the data flowing in/out),
the refactoring was *automatically* verified by re-running the existing
tests; no behavior debugging was needed.

---

## Ch. 25 — "Dependency-Breaking Techniques"
### Multiple techniques (Parameterize Constructor, Subclass and Override, Extract Interface, Adapt Parameter)

**Problem Feathers solves.** A class is hard to put under test because it
*creates its own dependencies* (file system, classpath, network, GUI). To
isolate it for testing, you need to break those dependencies. Ch. 25 is
Feathers' catalog of techniques for doing exactly that.

**Where applied.** Four distinct techniques across M3:

### a) Parameterize Constructor — `LinterService`

- Commit: `986063e` ("Refactoring + Feature: Extract LinterService and add CLI")
- File: [src/main/java/.../presentation/LinterService.java](../src/main/java/rhit/csse/csse374/linter/presentation/LinterService.java)
- The pipeline used to instantiate `ConvertToASM` directly. The new
  constructor accepts a `Function<String, ASMProject>` projectLoader,
  defaulting to the real loader. Tests pass a fake function that
  returns synthetic projects without touching disk:

  ```java
  LinterService service = new LinterService(p -> new ASMProject(p, List.of()));
  ```

### b) Subclass and Override — `LinterService` is non-final

- File: [src/main/java/.../presentation/LinterService.java](../src/main/java/rhit/csse/csse374/linter/presentation/LinterService.java) (note `// Left non-final so tests can use Feathers' "Subclass and Override" seam`)
- Used in [src/test/java/system/UseCaseDriverTest.java](../src/test/java/system/UseCaseDriverTest.java)
  as `RecordingService extends LinterService` to record every Request and
  return canned Responses for use-case scenarios.

### c) Extract Interface — `LinterConfig`

- Commit: `dfd84a6` ("Milestone 3 done")
- Files: [LinterConfig](../src/main/java/rhit/csse/csse374/linter/data/LinterConfig.java) (interface),
  [MapLinterConfig](../src/main/java/rhit/csse/csse374/linter/data/MapLinterConfig.java) (concrete impl).
- Domain checks consume the interface; the loader returns the
  concrete impl. The interface has a `static allEnabled()` factory used as
  the default in CLI/GUI.

### d) Adapt Parameter — `JsonLinterConfigLoader.load(Path)` / `read(Reader)` / `parse(String)`

- Commit: `dfd84a6`
- File: [JsonLinterConfigLoader](../src/main/java/rhit/csse/csse374/linter/data/JsonLinterConfigLoader.java)
- The public `load(Path)` (file-system flavor) delegates to a
  testable `read(Reader)` (no I/O concerns) which delegates to
  `parse(String)`. Tests exercise `parse` directly — no temp files
  required.

---

## Summary — Six Chapters at a Glance

| # | Chapter | Technique | Lives in | Commit |
|---|---|---|---|---|
| 1 | Ch. 6 | Sprout Class | `ResultsSummary` | `b0279a6` |
| 2 | Ch. 13 | Characterization Tests | `JsonReportWriterTest` | `fd2a584` |
| 3 | Ch. 14 | Wrap external library | `JsonLinterConfigLoader` | `dfd84a6` |
| 4 | Ch. 22 | Extract Method | `CheckCatalog.toDescriptor` | `efc5b6f` |
| 5 | Ch. 23 | Preserve Signatures | `JsonReportWriter` extract | `fd2a584` |
| 6 | Ch. 25 | Dependency-Breaking (×4) | `LinterService`, `LinterConfig`, `JsonLinterConfigLoader` | `986063e`, `dfd84a6` |

---

## How AI Was Used (per Objective 1.5)

The AI tool helped in three ways during these refactorings:

1. **Identifying the seam opportunities.** When asked "where could a seam
   go in `LinterGuiFrame.onRun`?" the AI suggested Parameterize Constructor
   on the SwingWorker → LinterService pipeline. That suggestion shaped
   commit `986063e`.

2. **Generating characterization tests.** Before extracting
   `JsonReportWriter`, the AI was asked to enumerate every observable
   behavior of the inline JSON-emitting code, including the bugs.
   That list became `JsonReportWriterTest` and made the byte-for-byte
   refactoring safe.

3. **Recognizing chapter alignment.** Several refactorings were originally
   driven by "this method is too long" or "this dependency is hard to
   test" intuition; the AI then named which Feathers chapter each one
   matched, which is what this document captures.

**What worked well.** The AI was good at spotting test seams in tangled
code and at proposing the *minimal* refactoring that would unlock testing.

**What didn't.** The AI sometimes proposed seams in places that already
had structural answers (e.g., "wrap LinterService in a Facade") that
would have added layers without testing benefit. Those suggestions were
declined.
