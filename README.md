# CSSE374 Java Design Linter

This repository contains a **Maven-based Java linter skeleton** for detecting software design issues (principle violations, bad patterns, and cursory style problems) using ASM.

## Quick start

### Build

```bash
mvn test
```

### Run (skeleton)

```bash
mvn -q package
java -jar target/LinterProject-1.0-rc3-jar-with-dependencies.jar C:/path/to/project1
```

Or use VS Code: `.vscode/launch.json` → **Run Linter (Skeleton)**.

## Repository layout

- `src/main/java/rhit/csse/csse374/linter/presentation`: **presentation layer**
  - `Main`: CLI entry point (wires the system together)
- `src/main/java/rhit/csse/csse374/linter/domain`: **domain layer**
  - `LinterHandler`: central coordinator (holds lists of checks/detectors/projects)
  - `ConvertToASM`: converts project locations into `ProjectToCheck` stubs (later: real ASM parsing)
  - `Cursory`, `Principle`, `Pattern`: interfaces (currently empty to match UML)
  - `cursory1..4`, `principle1..4`, and pattern detector classes: placeholder implementations
- `src/main/java/rhit/csse/csse374/linter/data`: **data layer**
  - `ProjectToCheck`: represents a project/codebase to lint
  - `LinterOutputText`: report object (currently lines of text)
- `src/main/resources/projects-to-check.txt`: placeholder for listing projects to lint during dev/demo
- `docs/architecture`: architecture artifacts
  - `design.puml`: hand-authored PlantUML from the team’s design
  - `class-diagram.png`: exported diagram image (for quick viewing)
- `examples/asm`: preserved ASM sample code (not part of Maven build)

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
Current constraints are the use of ASM and the necessity of having it as a Maven project. As we discover more constraints, we will update this section.

### Reference Architecture
We are using the "Rich Client" Reference Architecture. The client-side part of the application will perform most of the data handling.
