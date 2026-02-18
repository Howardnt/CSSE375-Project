# CSSE374 Java Design Linter

This repository contains a Java **design linter** for detecting software design issues (principle violations, bad patterns, and cursory style problems) using ASM.

## Quick start

### Run the linter checks (no Maven)

We added a manual compile+run script that:
- downloads ASM jars into `lib/` if missing
- compiles `src/main/java` + `src/test/java` into `out/`
- runs the smoke-test runner (`ManualLintRunner`) against `out/`

From the repo root (Windows):

```powershell
.\run-manual-tests.cmd
```

To lint a different compiled project (directory containing `.class` files):

```powershell
.\run-manual-tests.cmd "C:\path\to\someProject\target\classes"
```

### Run from VS Code / Cursor
- Open `src/test/java/ManualLintRunner.java`
- Click **Run** above `main()`
- Optionally provide a program argument pointing at a compiled-classes directory

### Run the GUI (Swing)
The GUI is a small Swing app that sits on top of the same linter pipeline, but provides:
- a file/folder picker for compiled `.class` outputs
- checkboxes to select which checks to run
- a sortable, filterable violations table + raw report view

To run from VS Code / Cursor:
- Open `src/main/java/rhit/csse/csse374/linter/presentation/gui/LinterGuiMain.java`
- Click **Run** above `main()`
- In the GUI, select a **compiled output** folder (e.g. `out/` from the script, or `target/classes` from Maven)

Note: this linter analyzes **compiled bytecode** (`.class` files), not `.java` source.

### Maven (optional)
The repo still contains a `pom.xml`, but the recommended dev workflow is the **non-Maven** script above.

## Repository layout

- `src/main/java/rhit/csse/csse374/linter/presentation`: **presentation layer**
  - `Main`: CLI entry point (wires the system together)
- `src/main/java/rhit/csse/csse374/linter/presentation/gui`: **GUI (Swing)**
  - `LinterGuiMain`: GUI entry point
- `src/main/java/rhit/csse/csse374/linter/domain`: **domain layer**
  - `LinterHandler`: central coordinator (holds lists of checks/detectors and runs them)
  - `ConvertToASM`: loads compiled `.class` files and parses them into ASM `ClassNode`s
  - `LintCheck`: common check contract used by `LinterHandler`
  - `Cursory`, `Principle`, `Pattern`: check groupings (extend `LintCheck`)
  - `MethodTooLongPattern`, `singleResponsibilityPrinciple`, `StrategyPattern`: implemented checks (see “Implemented checks” below)
- `src/main/java/rhit/csse/csse374/linter/data`: **data layer**
  - `ASMProject` / `ASMClass` / `ASMMethod`: wrappers around ASM nodes for easier analysis
  - `LinterOutputText`: report object (currently lines of text)
- `src/main/resources/projects-to-check.txt`: placeholder for listing projects to lint during dev/demo
- `docs/architecture`: architecture artifacts
  - `design.puml`: hand-authored PlantUML from the team’s design
  - `class-diagram.png`: exported diagram image (for quick viewing)
- `examples/asm`: preserved ASM sample code (not part of Maven build)

## Implemented checks (ASM-based)

- **Cursory**
  - `equalsChecker`: flags suspicious `==` comparisons on reference types like `String`, wrappers, and common collections
- **Patterns**
  - `StrategyPattern`: detects **Strategy-missing hotspots** (large switch / if-else behavior selection)
  - `singleResponsibilityPrinciple`: SRP heuristic (size + low cohesion via field-sharing + dependency fan-out)
  - `MethodTooLongPattern`: flags **too many parameters** (>5) and **method too long** (>40 source lines, fallback to bytecode instruction count)

## Test fixtures (compiled into `out/` by the script)
We keep small “good/bad” example classes under `src/test/java/fixtures/` to exercise the checks.
Run them via `run-manual-tests.cmd` or `ManualLintRunner`.

## Planned architecture

### Design Purpose
The primary purpose of this architecture design is to create a functional, maintainable, and flexible Java Linter to assist the instructor in efficiently grading CSSE374 assignments by detecting specific design anti-patterns and principle violations.

Factor 1: Project Cycle

Architectural design is being performed during development. The design focus is on performing enough structural work to satisfy the core requirements—such as implementing cursory style checks, principle violation checks, and pattern detectors—while preparing for a stable final release and video demonstration.

Factor 2: Greenfield/Brownfield System

This is a brownfield system. Because the project relies on the provided ASM sample code and embedded JAR files via Maven, the design focus is on understanding the existing system (ASM's visitor/node structure) and extending it to accommodate custom linter logic without breaking the provided integration.

Factor 3: Novel Domain/Mature Domain

This project exists within a mature domain. Linters are well-established tools. The team will use these existing systems as guidance to research high-quality features, common UX considerations, and standard configuration settings to inform the project's own architecture.

### Primary Quality Attributes
Efficiency: Users will be able to easily identify how to select design patterns, principle violations, and cursory checks to apply on their written code. It will be made to support repetitive use.

Consistency: The project will maintain both internal and external consistency through the usage of the three-layer design and adequate affordance of our UI elements.

Learnability: The project will be inuitive; users can utilize the UI without confusion or frustration. It is made to be repetitive, so they won't run into anything unexpected after the initial learning curve.

Error Minimizing: The project will provide detailed error messages, should it receive invalid user input or runs into other unexpected errors. Providing the users with accurate feedback is crucial.

Flexibility: The project will be built to allow for extension and enhancement while maintaining the existing code.

Regression: When testing, all tests can be run easily from the same interface.

Isolation: When testing, specific tests can be selected for more precise testing.

### How our primary functionality works and how it will interact with users

Our system takes a Java project and scans it for design problems using a set of linter checks, where each check looks for a specific issue like a design principle violation or a bad pattern. Users run the tool and choose which project and checks they want to use, and the system then executes those checks on the code. As the checks run, the system collects any issues it finds. Once finished, it generates a simple report. This report explains what design problems were found in a clear manner.

### Constraints
Current constraints are the use of ASM and the need to analyze compiled `.class` files. Maven is optional; the repo includes a non-Maven compile+run script for local testing.

### Reference Architecture
We are using the "Rich Client" Reference Architecture. The client-side part of the application will perform most of the data handling.
