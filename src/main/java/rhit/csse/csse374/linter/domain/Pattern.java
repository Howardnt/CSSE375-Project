package rhit.csse.csse374.linter.domain;

import java.util.ArrayList;
import java.util.List;

import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMProject;

/**
 * Domain-layer base class for a design pattern detector.
 *
 * Pattern checks come in two shapes in this codebase:
 * - simple detectors: determine whether each class exhibits a pattern (override {@link #isPattern(ASMClass)})
 * - analysis checks: compute more detailed findings (override {@link #runPatternCheck(ASMProject)})
 *
 * The default implementation runs the simple detector over each class and reports a single violation
 * when the pattern is detected.
 */
public abstract class Pattern implements LintCheck {

    @Override
    public final CheckResult run(ASMProject project) {
        return runPatternCheck(project);
    }

    /**
     * Default pattern execution pipeline.
     *
     * Override this method for more detailed analysis that emits multiple violations per class/method.
     */
    protected CheckResult runPatternCheck(ASMProject project) {
        List<Violation> violations = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int totalMethods = 0;
        int totalClasses = project.getClasses().size();

        for (ASMClass cls : project.getClasses()) {
            totalMethods += cls.getMethods().size();
            try {
                if (isPattern(cls)) {
                    violations.add(new Violation(
                            name() + " pattern detected",
                            cls.getClassName(),
                            "INFO"
                    ));
                }
            } catch (Exception e) {
                errors.add("Error analyzing " + cls.getClassName() + ": " + e.getMessage());
            }
        }

        return new CheckResult(violations, totalClasses, totalMethods, errors, name());
    }

    /**
     * Simple pattern predicate. Override in detector-style patterns.
     */
    protected boolean isPattern(ASMClass cls) {
        return false;
    }
}
