## Architecture docs

- `design.puml`: your hand-authored PlantUML diagram (source of truth for the initial design).
- `class-diagram.png`: exported diagram image for quick viewing.

### Note on code vs diagram
The diagram captures the *initial skeleton*. The implementation has evolved, including:
- A runnable check contract (`LintCheck`) executed by `LinterHandler`
- ASM wrappers (`ASMProject` / `ASMClass` / `ASMMethod`) used by checks

