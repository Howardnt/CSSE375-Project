package rhit.csse.csse374.linter.presentation;

import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.domain.CheckResult;
import rhit.csse.csse374.linter.domain.LinterResult;
import rhit.csse.csse374.linter.domain.Violation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Presentation-layer formatter for linter results.
 *
 * This class is responsible for taking domain-layer LinterResult objects
 * and formatting them into human-readable text output.
 *
 * Responsibilities:
 * - Format check results into readable text
 * - Display metadata (counts, project info)
 * - Present violations in a user-friendly way
 */
public class LinterOutputText {

    private final List<String> lines = new ArrayList<>();

    /**
     * Add a raw line to the output (for custom formatting if needed).
     */
    public void addLine(String line) {
        lines.add(line);
    }

    /**
     * Format a complete LinterResult into readable output.
     * This is the main presentation-layer operation.
     */
    public void formatResult(LinterResult result) {
        lines.clear(); // Reset any previous content

        addHeader();
        addProjectSummary(result);
        addCheckSummary(result);

        addSection("Cursory Checks", result.getCursoryResults());
        addSection("Principle Checks", result.getPrincipleResults());
        addSection("Pattern Detectors", result.getPatternResults());

        addFooter(result);
    }

    private void addHeader() {
        addLine("=== Linter Analysis Report ===");
        addLine("");
    }

    private void addProjectSummary(LinterResult result) {
        addLine("Project analyzed: " + result.getProjectPath());
        addLine("Total classes loaded: " + result.getTotalClasses());
        addLine("");
    }

    private void addCheckSummary(LinterResult result) {
        addLine("Checks configured:");
        addLine("  - Cursory checks: " + result.getCursoryCheckCount());
        addLine("  - Principle checks: " + result.getPrincipleCheckCount());
        addLine("  - Pattern detectors: " + result.getPatternCheckCount());
        addLine("");
    }

    private void addSection(String sectionTitle, List<CheckResult> results) {
        if (results.isEmpty()) {
            return;
        }

        addLine("=== " + sectionTitle + " ===");
        for (CheckResult checkResult : results) {
            formatCheckResult(checkResult);
        }
        addLine("");
    }

    private void formatCheckResult(CheckResult checkResult) {
        addLine("[Check Result] " + checkResult.toString());

        // Display violations if any
        if (checkResult.hasViolations()) {
            for (Violation violation : checkResult.getViolations()) {
                addLine("  ! " + violation.getMessage());
                if (violation.getLocation() != null && !violation.getLocation().isEmpty()) {
                    addLine("    Location: " + violation.getLocation());
                }
            }
        }

        // Display analysis errors if any
        if (checkResult.hasAnalysisErrors()) {
            addLine("  Analysis errors:");
            for (String error : checkResult.getAnalysisErrors()) {
                addLine("    - " + error);
            }
        }

        addLine("");
    }

    private void addFooter(LinterResult result) {
        addLine("=== Summary ===");
        if (result.hasAnyViolations()) {
            addLine("Total violations found: " + result.getTotalViolationCount());
        } else {
            addLine("No violations found. Great work!");
        }
    }

    public List<String> getLines() {
        return Collections.unmodifiableList(lines);
    }

    @Override
    public String toString() {
        if (lines.isEmpty()) {
            return "(no output generated)";
        }
        return String.join(System.lineSeparator(), lines);
    }
}
