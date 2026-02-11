package rhit.csse.csse374.linter.domain;

import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.data.LinterOutputText;

/**
 * Domain-layer interface for a design pattern detector.
 *
 * This skeleton keeps the interface empty to match the UML exactly.
 * Later, add a detection API (e.g., detect(...) returning findings) once your
 * project’s representation of code (ASM tree, source AST, etc.) is established.
 */
public interface Pattern extends LintCheck {

    /**
     * Pattern detectors typically return a structured result (violations + stats).
     * Implementors may override this; the default implementation reports "not implemented".
     */
    default CheckResult runPatternCheck(ASMProject project) {
        return new CheckResult(
                java.util.Collections.emptyList(),
                project.getClasses().size(),
                0,
                java.util.Collections.singletonList("runPatternCheck not implemented for " + name()),
                "Pattern"
        );
    }

    /**
     * Bridge into the common {@link LintCheck} contract so {@link LinterHandler} can run patterns
     * the same way as other checks.
     */
    @Override
    default void run(ASMProject project, LinterOutputText report) {
        CheckResult result = runPatternCheck(project);
        if (result == null) {
            report.addLine("PATTERN: " + name() + " returned null result");
            return;
        }

        report.addLine("PATTERN: " + result.toString());
        for (Violation v : result.getViolations()) {
            report.addLine("         " + v.toString());
        }
        for (String err : result.getAnalysisErrors()) {
            report.addLine("         [analysis] " + err);
        }
    }
}

