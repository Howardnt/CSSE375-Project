package rhit.csse.csse374.linter.domain;

/**
 * Domain-layer interface for a SOLID principle checker.
 *
 * Principle checks analyze code for violations of SOLID principles like:
 * - Single Responsibility Principle (SRP)
 * - Open/Closed Principle (OCP)
 * - Liskov Substitution Principle (LSP)
 * - Interface Segregation Principle (ISP)
 * - Dependency Inversion Principle (DIP)
 *
 * All principle checks must implement the run() method from LintCheck,
 * which returns a CheckResult containing violations and metadata.
 */
public interface Principle extends LintCheck {
    // No additional methods needed - inherits from LintCheck
    // CheckResult run(ASMProject project);
}