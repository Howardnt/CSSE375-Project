package rhit.csse.csse374.linter.domain;

import rhit.csse.csse374.linter.data.ASMProject;

/**
 * Domain-layer interface for a design pattern detector.
 *
 * This skeleton keeps the interface empty to match the UML exactly.
 * Later, add a detection API (e.g., detect(...) returning findings) once your
 * project’s representation of code (ASM tree, source AST, etc.) is established.
 */
public interface Pattern {
    public CheckResult runPatternCheck(ASMProject project);
}

