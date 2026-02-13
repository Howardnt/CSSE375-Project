package rhit.csse.csse374.linter.domain;

/**
 * Represents a single violation found by a linter check.
 *
 * This is a domain object that captures:
 * - What rule was violated
 * - Where it occurred
 * - How severe it is
 * - Any additional context
 */
public class Violation {
    private final String message;
    private final String location;
    private final String severity;

    public Violation(String message, String location, String severity) {
        this.message = message;
        this.location = location;
        this.severity = severity;
    }

    public Violation(String message, String location) {
        this(message, location, "WARNING");
    }

    public Violation(String message) {
        this(message, "", "WARNING");
    }

    public String getMessage() {
        return message;
    }

    public String getLocation() {
        return location;
    }

    public String getSeverity() {
        return severity;
    }

    @Override
    public String toString() {
        if (location != null && !location.isEmpty()) {
            return "[" + severity + "] " + message + " (at " + location + ")";
        }
        return "[" + severity + "] " + message;
    }
}