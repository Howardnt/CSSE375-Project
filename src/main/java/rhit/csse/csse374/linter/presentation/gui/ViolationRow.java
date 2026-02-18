package rhit.csse.csse374.linter.presentation.gui;

/**
 * Row model for the Violations table.
 */
public record ViolationRow(
        String category,
        String checkName,
        String severity,
        String location,
        String message
) {
}

