package rhit.csse.csse374.linter.domain;

import java.util.ArrayList;
import java.util.List;

import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMProject;

/**
 * Domain-layer interface for a design pattern detector.
 *
 * Pattern detectors analyze code to identify the presence (or absence)
 * of specific design patterns like Strategy, Template Method, Decorator, etc.
 *
 * All pattern detectors must implement the run() method from LintCheck,
 * which returns a CheckResult containing violations and metadata.
 */
public abstract class Pattern implements LintCheck {

    private final String patternName;

    protected Pattern(String patternName) {
        this.patternName = patternName;
    }

    public final CheckResult run(ASMProject project) {
        List<Violation> violations = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int totalMethods = 0;
        int totalClasses = project.getClasses().size();

        for (ASMClass cls : project.getClasses()) {
            totalMethods += cls.getMethods().size();

            try {
                if (isPattern(cls)) {
                    // Check if subclass provides detailed message
                    String message;
                    if (this instanceof TemplatePattern) {
                        message = ((TemplatePattern) this).getDetailedMessage(cls.getClassName());
                    } else {
                        message = patternName + " Pattern Detected in: " + cls.getClassName();
                    }
                    violations.add(new Violation(message));
                }
            } catch (Exception e) {
                errors.add("Error analyzing " + cls.getClassName() + ": " + e.getMessage());
            }
        }

        return new CheckResult(violations, totalClasses, totalMethods, errors, patternName + " Pattern");
    }

    public abstract boolean isPattern(ASMClass cls);
}