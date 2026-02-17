package rhit.csse.csse374.linter.presentation.gui;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model backing the digestible violations view.
 */
public final class ViolationTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = new String[]{
            "Category", "Check", "Severity", "Location", "Message"
    };

    private final List<ViolationRow> rows = new ArrayList<>();

    public void setRows(List<ViolationRow> newRows) {
        rows.clear();
        if (newRows != null) {
            rows.addAll(newRows);
        }
        fireTableDataChanged();
    }

    public ViolationRow getRowAt(int rowIndex) {
        return rows.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ViolationRow row = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> row.category();
            case 1 -> row.checkName();
            case 2 -> row.severity();
            case 3 -> row.location();
            case 4 -> row.message();
            default -> "";
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}

