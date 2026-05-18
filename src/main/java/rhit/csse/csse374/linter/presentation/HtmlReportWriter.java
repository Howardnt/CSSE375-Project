package rhit.csse.csse374.linter.presentation;

import rhit.csse.csse374.linter.domain.CheckResult;
import rhit.csse.csse374.linter.domain.LinterResult;
import rhit.csse.csse374.linter.domain.SeverityLevel;
import rhit.csse.csse374.linter.domain.Violation;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Renders a LinterResult as a self-contained HTML document.
 *
 * The output is a single .html file with inline CSS — no external
 * stylesheets, no JavaScript, no images. It opens in any modern browser
 * and is safe to drop into a directory and share.
 *
 * Sibling to JsonReportWriter and LinterOutputText. Pure function; no I/O.
 */
public final class HtmlReportWriter {

    public String toHtml(LinterResult result) {
        if (result == null) {
            return "";
        }

        Map<SeverityLevel, Integer> counts = countBySeverity(result);
        int total = result.getTotalViolationCount();

        StringBuilder sb = new StringBuilder(8192);
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"en\">\n");
        sb.append("<head>\n");
        sb.append("<meta charset=\"UTF-8\">\n");
        sb.append("<title>Linter Report — ").append(escape(result.getProjectPath())).append("</title>\n");
        appendStyles(sb);
        sb.append("</head>\n<body>\n");

        appendHeader(sb, result, counts, total);
        appendSection(sb, "Cursory", result.getCursoryResults());
        appendSection(sb, "Principle", result.getPrincipleResults());
        appendSection(sb, "Pattern", result.getPatternResults());

        sb.append("</body>\n</html>\n");
        return sb.toString();
    }

    private void appendStyles(StringBuilder sb) {
        sb.append("<style>\n");
        sb.append("  body { font-family: -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif;");
        sb.append(" margin: 2rem; color: #1a1a1a; background: #fafafa; }\n");
        sb.append("  h1 { margin-bottom: 0.25rem; }\n");
        sb.append("  h2 { margin-top: 2rem; border-bottom: 2px solid #ddd; padding-bottom: 0.25rem; }\n");
        sb.append("  h3 { margin-top: 1.25rem; color: #333; font-size: 1.05rem; }\n");
        sb.append("  .meta { color: #555; margin-bottom: 1rem; }\n");
        sb.append("  .summary-row { display: flex; gap: 0.5rem; flex-wrap: wrap; margin: 0.5rem 0 1.5rem; }\n");
        sb.append("  .badge { display: inline-block; padding: 0.15rem 0.55rem; border-radius: 999px;");
        sb.append(" font-size: 0.85rem; font-weight: 600; color: white; }\n");
        sb.append("  .badge-error   { background: #d32f2f; }\n");
        sb.append("  .badge-warning { background: #f57c00; }\n");
        sb.append("  .badge-info    { background: #1976d2; }\n");
        sb.append("  .badge-total   { background: #555; }\n");
        sb.append("  .empty-state { color: #888; font-style: italic; padding: 0.5rem 0; }\n");
        sb.append("  table { border-collapse: collapse; width: 100%; margin: 0.5rem 0 1rem;");
        sb.append(" background: white; box-shadow: 0 1px 2px rgba(0,0,0,0.05); }\n");
        sb.append("  th, td { padding: 0.45rem 0.7rem; text-align: left; border-bottom: 1px solid #eee; }\n");
        sb.append("  th { background: #f0f0f0; font-weight: 600; }\n");
        sb.append("  tbody tr:nth-child(even) { background: #f9f9f9; }\n");
        sb.append("  td.severity-cell { width: 90px; }\n");
        sb.append("  td.location-cell { font-family: ui-monospace, Menlo, Consolas, monospace;");
        sb.append(" font-size: 0.9rem; color: #555; }\n");
        sb.append("</style>\n");
    }

    private void appendHeader(
            StringBuilder sb, LinterResult result, Map<SeverityLevel, Integer> counts, int total) {
        sb.append("<h1>Linter Report</h1>\n");
        sb.append("<div class=\"meta\"><strong>Project:</strong> ")
                .append(escape(result.getProjectPath())).append("</div>\n");
        sb.append("<div class=\"summary-row\">\n");
        sb.append("  <span class=\"badge badge-error\">")
                .append(counts.get(SeverityLevel.ERROR)).append(" errors</span>\n");
        sb.append("  <span class=\"badge badge-warning\">")
                .append(counts.get(SeverityLevel.WARNING)).append(" warnings</span>\n");
        sb.append("  <span class=\"badge badge-info\">")
                .append(counts.get(SeverityLevel.INFO)).append(" info</span>\n");
        sb.append("  <span class=\"badge badge-total\">")
                .append(total).append(" total</span>\n");
        sb.append("</div>\n");
    }

    private void appendSection(StringBuilder sb, String category, List<CheckResult> results) {
        sb.append("<h2>").append(escape(category)).append("</h2>\n");
        if (results == null || results.isEmpty()) {
            sb.append("<p class=\"empty-state\">No checks in this category were run.</p>\n");
            return;
        }
        boolean anyViolation = false;
        for (CheckResult cr : results) {
            if (cr.getViolations() != null && !cr.getViolations().isEmpty()) {
                anyViolation = true;
                appendCheckGroup(sb, cr);
            }
        }
        if (!anyViolation) {
            sb.append("<p class=\"empty-state\">No violations found.</p>\n");
        }
    }

    private void appendCheckGroup(StringBuilder sb, CheckResult cr) {
        String checkName = (cr.getType() == null || cr.getType().isBlank())
                ? "(unnamed check)" : cr.getType();
        sb.append("<h3>").append(escape(checkName))
                .append(" <span style=\"color:#888;font-weight:400;\">(")
                .append(cr.getViolations().size()).append(")</span></h3>\n");
        sb.append("<table>\n<thead><tr>");
        sb.append("<th>Severity</th><th>Location</th><th>Message</th>");
        sb.append("</tr></thead>\n<tbody>\n");
        for (Violation v : cr.getViolations()) {
            sb.append("<tr>");
            sb.append("<td class=\"severity-cell\">").append(severityBadge(v.getSeverityLevel())).append("</td>");
            sb.append("<td class=\"location-cell\">").append(escape(v.getLocation())).append("</td>");
            sb.append("<td>").append(escape(v.getMessage())).append("</td>");
            sb.append("</tr>\n");
        }
        sb.append("</tbody></table>\n");
    }

    private String severityBadge(SeverityLevel level) {
        if (level == null) {
            return "<span class=\"badge badge-info\">UNKNOWN</span>";
        }
        return switch (level) {
            case ERROR -> "<span class=\"badge badge-error\">ERROR</span>";
            case WARNING -> "<span class=\"badge badge-warning\">WARNING</span>";
            case INFO -> "<span class=\"badge badge-info\">INFO</span>";
        };
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
                if (level != null) {
                    counts.merge(level, 1, Integer::sum);
                }
            }
        }
    }

    static String escape(String raw) {
        if (raw == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(raw.length() + 16);
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
