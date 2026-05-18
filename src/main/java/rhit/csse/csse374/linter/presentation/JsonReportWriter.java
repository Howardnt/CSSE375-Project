package rhit.csse.csse374.linter.presentation;

import rhit.csse.csse374.linter.domain.CheckResult;
import rhit.csse.csse374.linter.domain.LinterResult;
import rhit.csse.csse374.linter.domain.Violation;

import java.util.ArrayList;
import java.util.List;

/**
 * Serializes a LinterResult into a JSON string.
 *
 * Renders the project path, total violation count, and the full list of
 * violations grouped flat. All string fields are JSON-escaped so the output
 * is parseable by any strict JSON consumer.
 *
 * Sibling to LinterOutputText (plain text) and HtmlReportWriter (HTML).
 */
public final class JsonReportWriter {

    public String toJson(LinterResult result) {
        if (result == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"project\": \"").append(escape(result.getProjectPath())).append("\",\n");
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
        sb.append("      \"location\": \"").append(escape(v.getLocation())).append("\",\n");
        sb.append("      \"severity\": \"").append(escape(v.getSeverity())).append("\",\n");
        sb.append("      \"message\": \"").append(escape(v.getMessage())).append("\"\n");
        sb.append("    }").append(isLast ? "" : ",").append("\n");
    }

    static String escape(String raw) {
        if (raw == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(raw.length() + 8);
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
