package rhit.csse.csse374.linter.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Domain-layer result object containing all check results from a linter analysis.
 *
 * This object encapsulates:
 * - Results from all checks (cursory, principle, pattern)
 * - Metadata about the analysis (total classes, number of each check type)
 * - Project information
 */
public class LinterResult {
    private final List<CheckResult> cursoryResults;
    private final List<CheckResult> principleResults;
    private final List<CheckResult> patternResults;
    private final int totalClasses;
    private final int cursoryCheckCount;
    private final int principleCheckCount;
    private final int patternCheckCount;
    private final String projectPath;

    public LinterResult(
            List<CheckResult> cursoryResults,
            List<CheckResult> principleResults,
            List<CheckResult> patternResults,
            int totalClasses,
            int cursoryCheckCount,
            int principleCheckCount,
            int patternCheckCount,
            String projectPath
    ) {
        this.cursoryResults = new ArrayList<>(cursoryResults);
        this.principleResults = new ArrayList<>(principleResults);
        this.patternResults = new ArrayList<>(patternResults);
        this.totalClasses = totalClasses;
        this.cursoryCheckCount = cursoryCheckCount;
        this.principleCheckCount = principleCheckCount;
        this.patternCheckCount = patternCheckCount;
        this.projectPath = projectPath;
    }

    public List<CheckResult> getCursoryResults() {
        return Collections.unmodifiableList(cursoryResults);
    }

    public List<CheckResult> getPrincipleResults() {
        return Collections.unmodifiableList(principleResults);
    }

    public List<CheckResult> getPatternResults() {
        return Collections.unmodifiableList(patternResults);
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    public int getCursoryCheckCount() {
        return cursoryCheckCount;
    }

    public int getPrincipleCheckCount() {
        return principleCheckCount;
    }

    public int getPatternCheckCount() {
        return patternCheckCount;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public boolean hasAnyViolations() {
        return hasViolationsIn(cursoryResults) ||
                hasViolationsIn(principleResults) ||
                hasViolationsIn(patternResults);
    }

    public int getTotalViolationCount() {
        return countViolations(cursoryResults) +
                countViolations(principleResults) +
                countViolations(patternResults);
    }

    private boolean hasViolationsIn(List<CheckResult> results) {
        return results.stream().anyMatch(CheckResult::hasViolations);
    }

    private int countViolations(List<CheckResult> results) {
        return results.stream()
                .mapToInt(r -> r.getViolations().size())
                .sum();
    }
}
