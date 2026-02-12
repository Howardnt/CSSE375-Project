package rhit.csse.csse374.linter.domain;

/**
 * Domain-layer interface for a design pattern detector.
 *
 * Pattern detectors analyze code to identify the presence (or absence)
 * of specific design patterns like Strategy, Template Method, Decorator, etc.
 *
 * All pattern detectors must implement the run() method from LintCheck,
 * which returns a CheckResult containing violations and metadata.
 */
public interface Pattern extends LintCheck {
    // No additional methods needed - inherits from LintCheck
    // CheckResult run(ASMProject project);
}