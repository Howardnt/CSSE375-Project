package rhit.csse.csse374.linter.presentation.gui;

import rhit.csse.csse374.linter.domain.Violation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Accordion-style results view.
 *
 * Groups violations by check. Each group can be expanded/collapsed to show individual violations.
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
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        boolean includeZero = showZeroChecks.isSelected();

        int visibleGroups = 0;

        for (CheckGroup g : allGroups) {
            List<Violation> filtered = filterViolations(g.violations(), severity, q);
            boolean hasIssues = !g.violations().isEmpty();
            boolean showGroup = includeZero || hasIssues;

            // If filters are active, only show groups that have matches.
            boolean filtersActive = !"All".equalsIgnoreCase(severity) || !q.isEmpty();
            if (filtersActive) {
                showGroup = !filtered.isEmpty();
            }

            if (!showGroup) {
                continue;
            }

            visibleGroups++;
            listPanel.add(new CollapsibleCheckPanel(g.category(), g.checkName(), g.violations(), filtered));
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

    private List<Violation> filterViolations(List<Violation> violations, String severity, String q) {
        if (violations == null || violations.isEmpty()) {
            return List.of();
        }

        boolean severityAll = "All".equalsIgnoreCase(severity);
        boolean queryEmpty = q == null || q.isEmpty();
        if (severityAll && queryEmpty) {
            return violations;
        }

        List<Violation> out = new ArrayList<>();
        for (Violation v : violations) {
            String sev = v.getSeverity() == null ? "" : v.getSeverity().trim().toUpperCase(Locale.ROOT);
            if (!severityAll && !sev.equalsIgnoreCase(severity)) {
                continue;
            }
            if (!queryEmpty) {
                String hay = (safe(v.getMessage()) + " " + safe(v.getLocation()) + " " + safe(v.toString()))
                        .toLowerCase(Locale.ROOT);
                if (!hay.contains(q)) {
                    continue;
                }
            }
            out.add(v);
        }
        return out;
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }

    private final class CollapsibleCheckPanel extends JPanel {
        private final JToggleButton headerButton = new JToggleButton();
        private final JPanel body = new JPanel();
        private final String category;
        private final String checkName;
        private final String countText;

        CollapsibleCheckPanel(String category, String checkName, List<Violation> allViolations, List<Violation> visibleViolations) {
            super(new BorderLayout());
            this.category = category;
            this.checkName = checkName;
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                    new EmptyBorder(8, 10, 8, 10)
            ));

            int total = (allViolations == null) ? 0 : allViolations.size();
            int shown = (visibleViolations == null) ? 0 : visibleViolations.size();

            boolean filtersActive = shown != total;
            this.countText = filtersActive
                    ? (shown + " / " + total + " issues")
                    : (total + (total == 1 ? " issue" : " issues"));

            // Start collapsed (user requested)
            headerButton.setSelected(false);
            headerButton.setBorderPainted(false);
            headerButton.setContentAreaFilled(false);
            headerButton.setFocusPainted(false);
            headerButton.setHorizontalAlignment(SwingConstants.LEFT);
            headerButton.setFont(headerButton.getFont().deriveFont(Font.BOLD));
            headerButton.setText(buildHeaderText(false, category, checkName, this.countText));
            headerButton.setVerticalAlignment(SwingConstants.CENTER);
            headerButton.addActionListener(e -> toggle());
            add(headerButton, BorderLayout.NORTH);

            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setBorder(new EmptyBorder(8, 6, 0, 0));
            add(body, BorderLayout.CENTER);

            populateBody(visibleViolations == null ? List.of() : visibleViolations);
            body.setVisible(false);

            headerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, headerButton.getPreferredSize().height));
            setAlignmentX(LEFT_ALIGNMENT);
        }

        @Override
        public Dimension getMaximumSize() {
            // Critical: prevent BoxLayout from vertically stretching accordion rows.
            Dimension pref = getPreferredSize();
            return new Dimension(Integer.MAX_VALUE, pref.height);
        }

        private void toggle() {
            boolean open = headerButton.isSelected();
            headerButton.setText(buildHeaderText(open, category, checkName, countText));
            body.setVisible(open);
            body.revalidate();
            body.repaint();
            ResultsAccordionPanel.this.listPanel.revalidate();
            ResultsAccordionPanel.this.listPanel.repaint();
        }

        private String buildHeaderText(boolean open, String category, String checkName, String countText) {
            String arrow = open ? "▼" : "▶";
            return arrow + " [" + category + "] " + checkName + " — " + countText;
        }

        private void populateBody(List<Violation> violations) {
            body.removeAll();

            if (violations.isEmpty()) {
                JLabel none = new JLabel("No issues.");
                none.setBorder(new EmptyBorder(4, 4, 4, 4));
                body.add(none);
                return;
            }

            for (Violation v : violations) {
                body.add(new ViolationCard(v));
                body.add(Box.createVerticalStrut(6));
            }
        }
    }

    private static final class ViolationCard extends JPanel {
        ViolationCard(Violation v) {
            super(new BorderLayout(10, 4));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                    new EmptyBorder(8, 10, 8, 10)
            ));

            String sev = (v.getSeverity() == null) ? "" : v.getSeverity().trim().toUpperCase(Locale.ROOT);
            JLabel severity = new JLabel(sev.isEmpty() ? "WARNING" : sev);
            severity.setFont(severity.getFont().deriveFont(Font.BOLD));
            severity.setForeground(severityColor(sev));
            add(severity, BorderLayout.WEST);

            JPanel center = new JPanel();
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

            String locationText = (v.getLocation() == null || v.getLocation().isBlank())
                    ? ""
                    : v.getLocation();
            if (!locationText.isEmpty()) {
                JTextArea location = new JTextArea(locationText);
                location.setEditable(false);
                location.setOpaque(false);
                location.setLineWrap(true);
                location.setWrapStyleWord(true);
                location.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 12f));
                location.setForeground(UIManager.getColor("Label.disabledForeground"));
                location.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
                center.add(location);
                center.add(Box.createVerticalStrut(4));
            }

            JTextArea message = new JTextArea(v.getMessage() == null ? "" : v.getMessage());
            message.setEditable(false);
            message.setOpaque(false);
            message.setLineWrap(true);
            message.setWrapStyleWord(true);
            message.setFont(UIManager.getFont("Label.font"));
            message.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            center.add(message);

            add(center, BorderLayout.CENTER);

            // Allow wrapping and variable height.
            setAlignmentX(LEFT_ALIGNMENT);
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension pref = getPreferredSize();
            return new Dimension(Integer.MAX_VALUE, pref.height);
        }

        private Color severityColor(String severity) {
            return switch (severity) {
                case "ERROR" -> new Color(176, 0, 32);
                case "WARNING", "WARN" -> new Color(156, 92, 0);
                case "INFO" -> new Color(0, 92, 156);
                default -> UIManager.getColor("Label.foreground");
            };
        }
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

