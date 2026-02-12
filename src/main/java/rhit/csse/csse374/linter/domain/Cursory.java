package rhit.csse.csse374.linter.domain;

/**
 * Domain-layer interface for a "cursory" check.
 *
 * A cursory check is intended to be lightweight (style/naming/obvious issues).
 *
 * All cursory checks must implement the run() method from LintCheck,
 * which returns a CheckResult containing violations and metadata.
 */
public interface Cursory extends LintCheck {
    // No additional methods needed - inherits from LintCheck
    // CheckResult run(ASMProject project);
}