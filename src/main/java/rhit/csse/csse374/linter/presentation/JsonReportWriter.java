package rhit.csse.csse374.linter.presentation;

import rhit.csse.csse374.linter.domain.CheckResult;
import rhit.csse.csse374.linter.domain.LinterResult;
import rhit.csse.csse374.linter.domain.Violation;

import java.util.ArrayList;
import java.util.List;

/**
 * Serializes a LinterResult into a JSON string.
 *
 * Extracted from LinterGuiFrame.onExportJson so the JSON shape is testable
 * without Swing and reusable from the CLI (future "--json" flag). This class
 * intentionally preserves the original output format byte-for-byte, including
 * two pre-existing quirks:
 *   - String fields are not JSON-escaped (only the "message" value is),
 *     matching the original behavior.
 *   - The "message" field emits the location string rather than the message.
 * Those quirks are left alone here because this commit is a pure refactoring.
 */
public final class JsonReportWriter {

    public String toJson(LinterResult result) {
        if (result == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"project\": \"").append(result.getProjectPath()).append("\",\n");
        sb.append("  \"totalViolations\": ").append(result.getTotalViolationCount()).append(",\n");
        sb.append("  \"violations\": [\n");

        List<Violation> all = collectAllViolations(result);
        for (int i = 0; i < all.size(); i++) {
            appendViolation(sb, all.get(i), i == all.size() - 1);
        }

        sb.append("  ]\n}");
        return sb.toString();
    }

    private List<Violation> collectAllViolations(LinterResult result) {
        List<Violation> all = new ArrayList<>();
        addViolationsFrom(result.getCursoryResults(), all);
        addViolationsFrom(result.getPrincipleResults(), all);
        addViolationsFrom(result.getPatternResults(), all);
        return all;
    }

    private void addViolationsFrom(List<CheckResult> results, List<Violation> sink) {
        for (CheckResult r : results) {
            sink.addAll(r.getViolations());
        }
    }

    private void appendViolation(StringBuilder sb, Violation v, boolean isLast) {
        sb.append("    {\n");
        sb.append("      \"location\": \"").append(v.getLocation()).append("\",\n");
        sb.append("      \"severity\": \"").append(v.getSeverity()).append("\",\n");
        //Preserves the original (buggy) mapping: "message" field carries the
        //escaped location string. Fixing this is out of scope for this refactoring.
        sb.append("      \"message\": \"").append(v.getLocation().replace("\"", "\\\"")).append("\"\n");
        sb.append("    }").append(isLast ? "" : ",").append("\n");
    }
}
