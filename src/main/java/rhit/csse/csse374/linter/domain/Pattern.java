package rhit.csse.csse374.linter.domain;

import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.data.LinterOutputText;

/**
 * Domain-layer base class for a design pattern detector.
 *
 * Pattern is now modeled as an abstract class to provide a shared execution/reporting
 * pipeline while still requiring concrete detectors to implement the analysis logic.
 */
public abstract class Pattern implements LintCheck {

    /**
     * Pattern detectors typically return a structured result (violations + stats).
     */
    public abstract CheckResult runPatternCheck(ASMProject project);

    /**
     * Bridge into the common {@link LintCheck} contract so {@link LinterHandler} can run patterns
     * the same way as other checks.
     */
    @Override
    public final void run(ASMProject project, LinterOutputText report) {
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

