package rhit.csse.csse374.linter.presentation.gui;

import rhit.csse.csse374.linter.domain.SeverityLevel;
import rhit.csse.csse374.linter.domain.Violation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Locale;

/**
 * One expandable row inside the results accordion.
 *
 * Extracted from ResultsAccordionPanel so the row rendering and its bundled
 * ViolationCard can live on their own. The panel calls back into the parent
 * via the supplied Runnable when it's toggled — this is the only coupling
 * left between the row and the surrounding accordion.
 */
final class CollapsibleCheckPanel extends JPanel {

    private final JToggleButton headerButton = new JToggleButton();
    private final JPanel body = new JPanel();
    private final String category;
    private final String checkName;
    private final String countText;
    private final Runnable onToggled;

    CollapsibleCheckPanel(
            String category,
            String checkName,
            List<Violation> allViolations,
            List<Violation> visibleViolations,
            Runnable onToggled) {
        super(new BorderLayout());
        this.category = category;
        this.checkName = checkName;
        this.onToggled = (onToggled == null) ? () -> {} : onToggled;

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                new EmptyBorder(8, 10, 8, 10)));

        int total = (allViolations == null) ? 0 : allViolations.size();
        int shown = (visibleViolations == null) ? 0 : visibleViolations.size();
        boolean filtersActive = shown != total;
        this.countText = filtersActive
                ? (shown + " / " + total + " issues")
                : (total + (total == 1 ? " issue" : " issues"));

        //Start collapsed (user-requested default)
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
        //Critical: prevent BoxLayout from vertically stretching accordion rows
        Dimension pref = getPreferredSize();
        return new Dimension(Integer.MAX_VALUE, pref.height);
    }

    private void toggle() {
        boolean open = headerButton.isSelected();
        headerButton.setText(buildHeaderText(open, category, checkName, countText));
        body.setVisible(open);
        body.revalidate();
        body.repaint();
        onToggled.run();
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

    private static final class ViolationCard extends JPanel {
        ViolationCard(Violation v) {
            super(new BorderLayout(10, 4));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                    new EmptyBorder(8, 10, 8, 10)));

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
            setAlignmentX(LEFT_ALIGNMENT);
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension pref = getPreferredSize();
            return new Dimension(Integer.MAX_VALUE, pref.height);
        }

        private Color severityColor(String severity) {
            return SeverityLevel.fromString(severity).getColor();
        }
    }
}
