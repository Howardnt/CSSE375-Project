package rhit.csse.csse374.linter.presentation.gui;

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

        String severity = (value == null) ? "" : value.toString().trim().toUpperCase();
        Color base = UIManager.getColor("Table.foreground");
        if (base == null) {
            base = Color.BLACK;
        }

        Color color = switch (severity) {
            case "ERROR" -> new Color(176, 0, 32);
            case "WARNING", "WARN" -> new Color(156, 92, 0);
            case "INFO" -> new Color(0, 92, 156);
            default -> base;
        };

        c.setForeground(color);
        return c;
    }
}

