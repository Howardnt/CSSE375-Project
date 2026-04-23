package rhit.csse.csse374.linter.presentation.gui;

import rhit.csse.csse374.linter.domain.Violation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Accordion-style results view.
 *
 * Groups violations by check. Each group can be expanded/collapsed to show
 * individual violations.
 *
 * Refactoring 9 split this God Class into three pieces:
 *   - ViolationFilter: severity + text filtering logic (pure, testable)
 *   - CollapsibleCheckPanel: one accordion row (with its ViolationCard)
 *   - ResultsAccordionPanel (this class): the container + filter bar
 */
public final class ResultsAccordionPanel extends JPanel {

    public record CheckGroup(String category, String checkName, List<Violation> violations) {
    }

    private final JComboBox<String> severityFilter = new JComboBox<>(new String[]{"All", "ERROR", "WARNING", "INFO"});
    private final JTextField searchField = new JTextField();
    private final JCheckBox showZeroChecks = new JCheckBox("Show checks with 0 issues", true);

    private final JPanel listPanel = new JPanel();
    private final JScrollPane scrollPane;

    private List<CheckGroup> allGroups = List.of();

    public ResultsAccordionPanel() {
        super(new BorderLayout(8, 8));

        add(buildFilterBar(), BorderLayout.NORTH);

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(new EmptyBorder(6, 6, 6, 6));

        scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        severityFilter.addActionListener(e -> rebuild());
        showZeroChecks.addActionListener(e -> rebuild());
        searchField.getDocument().addDocumentListener(new SimpleDocumentListener(this::rebuild));

        setGroups(List.of());
    }

    private JComponent buildFilterBar() {
        JPanel root = new JPanel(new BorderLayout(8, 8));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.add(new JLabel("Severity:"));
        left.add(severityFilter);
        left.add(showZeroChecks);
        root.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.add(new JLabel("Search:"), BorderLayout.WEST);
        right.add(searchField, BorderLayout.CENTER);
        root.add(right, BorderLayout.CENTER);

        return root;
    }

    public void setGroups(List<CheckGroup> groups) {
        this.allGroups = (groups == null) ? List.of() : new ArrayList<>(groups);
        rebuild();
    }

    private void rebuild() {
        listPanel.removeAll();

        String severity = String.valueOf(severityFilter.getSelectedItem());
        String query = searchField.getText();
        boolean includeZero = showZeroChecks.isSelected();

        ViolationFilter filter = new ViolationFilter(severity, query);
        int visibleGroups = 0;

        for (CheckGroup g : allGroups) {
            List<Violation> filtered = filter.apply(g.violations());
            if (!shouldShowGroup(g.violations(), filtered, includeZero, filter.isActive())) {
                continue;
            }
            visibleGroups++;
            listPanel.add(new CollapsibleCheckPanel(
                    g.category(), g.checkName(), g.violations(), filtered, this::repaintList));
            listPanel.add(Box.createVerticalStrut(8));
        }

        if (visibleGroups == 0) {
            JLabel empty = new JLabel("No results to display.");
            empty.setBorder(new EmptyBorder(12, 12, 12, 12));
            listPanel.add(empty);
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private boolean shouldShowGroup(
            List<Violation> all, List<Violation> filtered, boolean includeZero, boolean filtersActive) {
        if (filtersActive) {
            return !filtered.isEmpty();
        }
        boolean hasIssues = !all.isEmpty();
        return includeZero || hasIssues;
    }

    private void repaintList() {
        listPanel.revalidate();
        listPanel.repaint();
    }

    private static final class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final Runnable onChange;

        private SimpleDocumentListener(Runnable onChange) {
            this.onChange = onChange;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            onChange.run();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            onChange.run();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            onChange.run();
        }
    }
}
