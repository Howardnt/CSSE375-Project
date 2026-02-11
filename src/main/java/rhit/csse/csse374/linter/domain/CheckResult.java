package rhit.csse.csse374.linter.domain;

import java.util.List;

public class CheckResult {
    private final List<Violation> violations;
    private final int classesChecked;
    private final int methodsChecked;
    private final List<String> analysisErrors;
    private final String type;

    public CheckResult(List<Violation> violations, int classesChecked,
                       int methodsChecked, List<String> analysisErrors, String type) {
        this.violations = violations;
        this.classesChecked = classesChecked;
        this.methodsChecked = methodsChecked;
        this.analysisErrors = analysisErrors;
        this.type = type;
    }

    public boolean hasViolations() {
        return !violations.isEmpty();
    }

    public List<Violation> getViolations() {
        return violations;
    }

    public int getClassesChecked() {
        return classesChecked;
    }

    public int getMethodsChecked() {
        return methodsChecked;
    }

    public List<String> getAnalysisErrors() {
        return analysisErrors;
    }

    public boolean hasAnalysisErrors() {
        return !analysisErrors.isEmpty();
    }

    @Override
    public String toString() {
        if (violations.isEmpty()) {
            return "No " + type + " errors found in " + classesChecked +
                    " classes (" + methodsChecked + " methods checked)";
        } else {
            return "Found " + violations.size() +  " " + type + " error(s) in " +
                    classesChecked + " classes (" + methodsChecked + " methods checked)";
        }
    }
}