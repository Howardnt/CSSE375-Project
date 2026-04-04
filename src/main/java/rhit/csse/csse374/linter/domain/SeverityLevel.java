package rhit.csse.csse374.linter.domain;

import java.awt.Color;

/**
 * Defines the severity levels a Violation can carry.
 * Each level has an associated display color for the GUI.
 */
public enum SeverityLevel {
    ERROR(new Color(176, 0, 32)),
    WARNING(new Color(156, 92, 0)),
    INFO(new Color(0, 92, 156));

    private final Color color;

    SeverityLevel(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    /**
     * Parses a string to a SeverityLevel, defaulting to WARNING for
     * null, empty, or unrecognized values.
     */
    public static SeverityLevel fromString(String s) {
        if (s == null || s.isBlank()) {
            return WARNING;
        }
        String upper = s.trim().toUpperCase();
        if ("WARN".equals(upper)) {
            return WARNING;
        }
        try {
            return SeverityLevel.valueOf(upper);
        } catch (IllegalArgumentException e) {
            return WARNING;
        }
    }
}
