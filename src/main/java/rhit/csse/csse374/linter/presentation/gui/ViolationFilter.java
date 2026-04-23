package rhit.csse.csse374.linter.presentation.gui;

import rhit.csse.csse374.linter.domain.Violation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Pure-logic filter for violation lists.
 *
 * Extracted from ResultsAccordionPanel so the severity + text filtering can be
 * unit-tested without constructing a Swing component tree. This class has no
 * Swing dependencies and is safe to call from any thread.
 *
 * The filter matches a violation when:
 *   severity is "All" OR the violation's severity equals the chosen severity, AND
 *   the search query is empty OR is contained in the violation's message,
 *   location, or toString() (case-insensitive).
 */
public final class ViolationFilter {

    //Sentinel severity value meaning "no severity restriction"
    public static final String SEVERITY_ALL = "All";

    private final String severity;
    private final String query;

    public ViolationFilter(String severity, String query) {
        this.severity = (severity == null) ? SEVERITY_ALL : severity;
        this.query = (query == null) ? "" : query.trim().toLowerCase(Locale.ROOT);
    }

    public boolean isActive() {
        return !SEVERITY_ALL.equalsIgnoreCase(severity) || !query.isEmpty();
    }

    public List<Violation> apply(List<Violation> violations) {
        if (violations == null || violations.isEmpty()) {
            return List.of();
        }
        if (!isActive()) {
            return violations;
        }

        List<Violation> out = new ArrayList<>();
        for (Violation v : violations) {
            if (matches(v)) {
                out.add(v);
            }
        }
        return out;
    }

    private boolean matches(Violation v) {
        return matchesSeverity(v) && matchesQuery(v);
    }

    private boolean matchesSeverity(Violation v) {
        if (SEVERITY_ALL.equalsIgnoreCase(severity)) {
            return true;
        }
        String sev = (v.getSeverity() == null)
                ? ""
                : v.getSeverity().trim().toUpperCase(Locale.ROOT);
        return sev.equalsIgnoreCase(severity);
    }

    private boolean matchesQuery(Violation v) {
        if (query.isEmpty()) {
            return true;
        }
        String hay = (safe(v.getMessage()) + " " + safe(v.getLocation()) + " " + safe(v.toString()))
                .toLowerCase(Locale.ROOT);
        return hay.contains(query);
    }

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }
}
