package rhit.csse.csse374.linter.domain;

import java.util.List;

import org.objectweb.asm.tree.ClassNode;

/**
 * Domain-layer interface for a design pattern detector.
 *
 * This skeleton keeps the interface empty to match the UML exactly.
 * Later, add a detection API (e.g., detect(...) returning findings) once your
 * project’s representation of code (ASM tree, source AST, etc.) is established.
 */
public interface Pattern {

    List<String> check(ClassNode classNode);
}

