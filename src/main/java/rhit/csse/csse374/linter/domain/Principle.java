package rhit.csse.csse374.linter.domain;

import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMProject;

import java.util.ArrayList;
import java.util.List;

public abstract class Principle implements LintCheck {

    private final String principleName;

    protected Principle(String principleName) {
        this.principleName = principleName;
    }

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