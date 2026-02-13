package rhit.csse.csse374.linter.domain;

import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.presentation.LinterOutputText;

/**
 * Base interface for all linter checks.
 *
 * Each check implementation (Cursory, Principle, Pattern) must:
 * 1. Return a CheckResult containing violations and metadata
 * 2. NOT handle output formatting
 */
public interface LintCheck {

    /**
     * Executes this check on the given project.
     *
     * @param project The ASM project to analyze
     * @return CheckResult containing violations, stats, and any errors
     */
    CheckResult run(ASMProject project);
}

