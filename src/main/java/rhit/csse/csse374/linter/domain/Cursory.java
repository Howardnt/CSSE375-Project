package rhit.csse.csse374.linter.domain;

import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.data.LinterOutputText;

/**
 * Domain-layer interface for a "cursory" check.
 *
 * A cursory check is intended to be lightweight (style/naming/obvious issues).
 * This is intentionally an empty interface in the initial skeleton to match the UML.
 *
 * Extension point: later you can add a method (e.g., `apply(ProjectToCheck, LinterOutputText)`)
 * once your team agrees on a uniform check contract.
 */
public interface Cursory extends LintCheck {
    /**
     * Cursory checks can return structured results.
     * Simple checks can skip this and just implement run() directly.
     */
    default CheckResult checkProject(ASMProject project) {
        return new CheckResult(
                java.util.Collections.emptyList(),
                project.getClasses().size(),
                0,
                java.util.Collections.emptyList(),
                "Cursory"
        );
    }

    @Override
    default void run(ASMProject project, LinterOutputText report) {
        CheckResult result = checkProject(project);
        if (result.getViolations().isEmpty() && result.getAnalysisErrors().isEmpty()) {
            return; // Simple checks just report directly, skip this
        }

        report.addLine("CURSORY: " + result.toString());
        for (Violation v : result.getViolations()) {
            report.addLine("         " + v.toString());
        }
        for (String err : result.getAnalysisErrors()) {
            report.addLine("         [analysis] " + err);
        }
    }
}