package rhit.csse.csse374.linter.presentation;

import rhit.csse.csse374.linter.domain.CheckResult;
import rhit.csse.csse374.linter.domain.LinterResult;
import rhit.csse.csse374.linter.domain.SeverityLevel;
import rhit.csse.csse374.linter.domain.Violation;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Formats a compact one-line run-summary banner from a LinterResult.
 *
 * Example output:
 *   "3 errors · 12 warnings · 5 info · 20 total · ran in 1.23 s"
 *
 * Kept as a pure function so the GUI banner and any future CLI footer share
 * identical wording and can be tested without Swing.
 */
public final class ResultsSummary {

    private static final String SEPARATOR = " · ";

    public String format(LinterResult result, Duration runDuration) {
        if (result == null) {
            return "No run yet.";
        }

        Map<SeverityLevel, Integer> counts = countBySeverity(result);
        int total = result.getTotalViolationCount();

        StringBuilder sb = new StringBuilder();
        sb.append(pluralize(counts.get(SeverityLevel.ERROR), "error", "errors"));
        sb.append(SEPARATOR);
        sb.append(pluralize(counts.get(SeverityLevel.WARNING), "warning", "warnings"));
        sb.append(SEPARATOR);
        sb.append(counts.get(SeverityLevel.INFO)).append(" info");
        sb.append(SEPARATOR);
        sb.append(total).append(" total");
        if (runDuration != null) {
            sb.append(SEPARATOR).append("ran in ").append(formatDuration(runDuration));
        }
        return sb.toString();
    }

    private Map<SeverityLevel, Integer> countBySeverity(LinterResult result) {
        Map<SeverityLevel, Integer> counts = new EnumMap<>(SeverityLevel.class);
        for (SeverityLevel level : SeverityLevel.values()) {
            counts.put(level, 0);
        }
        addCounts(counts, result.getCursoryResults());
        addCounts(counts, result.getPrincipleResults());
        addCounts(counts, result.getPatternResults());
        return counts;
    }

    private void addCounts(Map<SeverityLevel, Integer> counts, List<CheckResult> results) {
        for (CheckResult r : results) {
            for (Violation v : r.getViolations()) {
                SeverityLevel level = v.getSeverityLevel();
                if (level == null) {
                    continue;
                }
                counts.merge(level, 1, Integer::sum);
            }
        }
    }

    private String pluralize(int count, String singular, String plural) {
        return count + " " + (count == 1 ? singular : plural);
    }

    String formatDuration(Duration d) {
        long totalMs = d.toMillis();
        if (totalMs < 1000) {
            return totalMs + " ms";
        }
        double seconds = totalMs / 1000.0;
        if (seconds < 60) {
            return String.format("%.2f s", seconds);
        }
        long wholeSeconds = totalMs / 1000;
        long minutes = wholeSeconds / 60;
        long remaining = wholeSeconds % 60;
        return minutes + " min " + remaining + " s";
    }
}
