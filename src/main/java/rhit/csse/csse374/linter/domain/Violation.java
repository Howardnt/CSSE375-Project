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
    private final SeverityLevel severity;

    public Violation(String message, String location, SeverityLevel severity) {
        this.message = message;
        this.location = location;
        this.severity = severity;
    }

    public Violation(String message, String location, String severity) {
        this(message, location, SeverityLevel.fromString(severity));
    }

    public Violation(String message, String location) {
        this(message, location, SeverityLevel.WARNING);
    }

    public Violation(String message) {
        this(message, "", SeverityLevel.WARNING);
    }

    public String getMessage() {
        return message;
    }

    public String getLocation() {
        return location;
    }

    public SeverityLevel getSeverityLevel() {
        return severity;
    }

    public String getSeverity() {
        return severity.name();
    }

    @Override
    public String toString() {
        if (location != null && !location.isEmpty()) {
            return "[" + severity.name() + "] " + message + " (at " + location + ")";
        }
        return "[" + severity.name() + "] " + message;
    }
}
