package rhit.csse.csse374.linter.domain;

import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMProject;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain-layer interface for a "cursory" check.
 *
 * A cursory check is intended to be lightweight (style/naming/obvious issues).
 *
 * All cursory checks must implement the run() method from LintCheck,
 * which returns a CheckResult containing violations and metadata.
 */
public abstract class Cursory implements LintCheck {

    public final CheckResult run(ASMProject project) {
        List<Violation> violations = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int totalMethods = 0;
        int totalClasses = project.getClasses().size();

        for (ASMClass cls : project.getClasses()) {
            totalMethods += cls.getMethods().size();

            try {
                violations.addAll(checkClass(cls));
            } catch (Exception e) {
                errors.add("Error analyzing " + cls.getClassName() + ": " + e.getMessage());
            }
        }

        return new CheckResult(violations, totalClasses, totalMethods, errors, name());
    }

    public abstract List<Violation> checkClass(ASMClass cls);
}