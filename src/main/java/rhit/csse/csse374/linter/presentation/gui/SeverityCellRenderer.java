package rhit.csse.csse374.linter.presentation.gui;

import rhit.csse.csse374.linter.domain.SeverityLevel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Simple renderer to make severities visually scannable.
 */
public final class SeverityCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
    ) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (isSelected) {
            return c; // keep selection colors
        }

        String severityStr = (value == null) ? "" : value.toString().trim();
        SeverityLevel level = SeverityLevel.fromString(severityStr);
        Color color = level.getColor();

        c.setForeground(color);
        return c;
    }
}

