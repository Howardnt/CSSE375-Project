package rhit.csse.csse374.linter.presentation.gui;

import rhit.csse.csse374.linter.domain.Cursory;
import rhit.csse.csse374.linter.domain.CheckResult;
import rhit.csse.csse374.linter.domain.LintCheck;
import rhit.csse.csse374.linter.domain.LinterResult;
import rhit.csse.csse374.linter.domain.Pattern;
import rhit.csse.csse374.linter.domain.Principle;
import rhit.csse.csse374.linter.domain.Violation;
import rhit.csse.csse374.linter.presentation.gui.CheckCatalog.CheckDescriptor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Swing window for running the linter.
 *
 * This class focuses on layout and UI wiring. The actual run logic is added via a SwingWorker later.
 */
public class LinterGuiFrame extends JFrame {

    private final JTextField targetPathField = new JTextField();
    private final JButton browseButton = new JButton("Browse…");
    private final JButton defaultButton = new JButton("Use default output");

    private final JButton runButton = new JButton("Run");
    private final JProgressBar progressBar = new JProgressBar();

    private JSplitPane mainSplitPane;

    // Check selection panels
    private final CheckTabPanel cursoryTab = new CheckTabPanel(CheckCatalog.cursoryChecks());
    private final CheckTabPanel principleTab = new CheckTabPanel(CheckCatalog.principleChecks());
    private final CheckTabPanel patternTab = new CheckTabPanel(CheckCatalog.patternChecks());

    // Results views (populated later)
    private final JTextArea summaryArea = new JTextArea();
    private final ResultsAccordionPanel accordionPanel = new ResultsAccordionPanel();
    private final JTextArea rawReportArea = new JTextArea();

    private volatile RunLinterWorker activeWorker;

    public LinterGuiFrame() {
        super("CSSE374 Design Linter");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 650));
        setSize(new Dimension(1250, 720)); // start wide enough for results readability
        setLocationByPlatform(true);

        setContentPane(buildRoot());
        wireActions();

        // Make the results pane start wider than the check selection pane.
        SwingUtilities.invokeLater(() -> {
            if (mainSplitPane != null) {
                mainSplitPane.setDividerLocation(0.28);
            }
        });

        // Seed a reasonable default, if any
        String defaultPath = DefaultTargetLocator.findDefaultOutputDir();
        if (defaultPath != null) {
            targetPathField.setText(defaultPath);
        }
    }

    private JComponent buildRoot() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        root.add(buildTargetBar(), BorderLayout.NORTH);
        root.add(buildMainSplitPane(), BorderLayout.CENTER);
        root.add(buildBottomBar(), BorderLayout.SOUTH);

        return root;
    }

    private JComponent buildTargetBar() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.add(new JLabel("Target (.class folder or file):"), BorderLayout.WEST);
        left.add(targetPathField, BorderLayout.CENTER);
        panel.add(left, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.add(defaultButton);
        right.add(browseButton);
        panel.add(right, BorderLayout.EAST);

        return panel;
    }

    private JComponent buildMainSplitPane() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.25);
        split.setContinuousLayout(true);

        split.setLeftComponent(buildCheckSelectionPane());
        split.setRightComponent(buildResultsPane());

        mainSplitPane = split;
        return split;
    }

    private JComponent buildCheckSelectionPane() {
        JPanel root = new JPanel(new BorderLayout(8, 8));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Cursory", cursoryTab);
        tabs.addTab("Principles", principleTab);
        tabs.addTab("Patterns", patternTab);
        root.add(tabs, BorderLayout.CENTER);

        // Global selection buttons (apply across all categories)
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton selectAll = new JButton("Select all");
        JButton selectNone = new JButton("Select none");
        JButton resetDefaults = new JButton("Reset defaults");

        selectAll.addActionListener(e -> {
            cursoryTab.setAll(true);
            principleTab.setAll(true);
            patternTab.setAll(true);
        });
        selectNone.addActionListener(e -> {
            cursoryTab.setAll(false);
            principleTab.setAll(false);
            patternTab.setAll(false);
        });
        resetDefaults.addActionListener(e -> {
            cursoryTab.resetToDefaults();
            principleTab.resetToDefaults();
            patternTab.resetToDefaults();
        });

        buttons.add(selectAll);
        buttons.add(selectNone);
        buttons.add(resetDefaults);
        root.add(buttons, BorderLayout.SOUTH);

        return root;
    }

    private JComponent buildResultsPane() {
        JTabbedPane tabs = new JTabbedPane();

        summaryArea.setEditable(false);
        summaryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        summaryArea.setText("Select a target path and checks, then click Run.");
        tabs.addTab("Summary", new JScrollPane(summaryArea));

        tabs.addTab("Results", accordionPanel);

        rawReportArea.setEditable(false);
        rawReportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        tabs.addTab("Raw report", new JScrollPane(rawReportArea));

        return tabs;
    }

    private JComponent buildBottomBar() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        progressBar.setString("Idle");

        panel.add(progressBar, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.add(runButton);
        panel.add(right, BorderLayout.EAST);

        return panel;
    }

    private void wireActions() {
        browseButton.addActionListener(e -> onBrowse());
        defaultButton.addActionListener(e -> onUseDefault());
        runButton.addActionListener(e -> onRun());
    }

    private void onBrowse() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select compiled output folder (or a .class file)");
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setAcceptAllFileFilterUsed(true);

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selected = chooser.getSelectedFile();
        if (selected != null) {
            targetPathField.setText(selected.getAbsolutePath());
        }
    }

    private void onUseDefault() {
        String defaultPath = DefaultTargetLocator.findDefaultOutputDir();
        if (defaultPath == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "No default compiled-classes output directory found.\n" +
                            "Try Browse… and select a folder like target/classes or out.",
                    "No default output found",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        targetPathField.setText(new File(defaultPath).getAbsolutePath());
    }

    private void onRun() {
        if (activeWorker != null) {
            return; // already running
        }

        String path = targetPathField.getText().trim();
        if (path.isEmpty()) {
            showWarning("Missing target", "Please choose a folder (or .class file) containing compiled classes.");
            return;
        }

        File target = new File(path);
        if (!target.exists()) {
            showWarning("Target not found", "Path does not exist:\n" + target.getAbsolutePath());
            return;
        }
        if (target.isFile() && !target.getName().endsWith(".class")) {
            showWarning("Invalid target", "Please select a directory of .class files or a single .class file.");
            return;
        }
        if (target.isDirectory() && !DefaultTargetLocator.containsClassFiles(target)) {
            showWarning("No compiled classes found", "Selected directory contains no .class files:\n" + target.getAbsolutePath());
            return;
        }

        // Gather selected checks and instantiate them in a stable order.
        List<CheckDescriptor> selected = getSelectedCheckDescriptors();
        if (selected.isEmpty()) {
            showWarning("No checks selected", "Select at least one check, then click Run.");
            return;
        }

        List<Cursory> cursories = new ArrayList<>();
        List<Principle> principles = new ArrayList<>();
        List<Pattern> patterns = new ArrayList<>();

        List<String> cursoryNames = new ArrayList<>();
        List<String> principleNames = new ArrayList<>();
        List<String> patternNames = new ArrayList<>();

        for (CheckDescriptor d : selected) {
            LintCheck check = d.create();
            switch (d.category()) {
                case CURSORY -> {
                    cursories.add((Cursory) check);
                    cursoryNames.add(check.name());
                }
                case PRINCIPLE -> {
                    principles.add((Principle) check);
                    principleNames.add(check.name());
                }
                case PATTERN -> {
                    patterns.add((Pattern) check);
                    patternNames.add(check.name());
                }
            }
        }

        setRunning(true);
        summaryArea.setText("Running analysis...\n\nTarget: " + target.getAbsolutePath());
        rawReportArea.setText("");

        RunLinterWorker worker = new RunLinterWorker(
                target.getAbsolutePath(),
                cursories,
                principles,
                patterns,
                cursoryNames,
                principleNames,
                patternNames
        );
        activeWorker = worker;

        worker.addPropertyChangeListener(evt -> {
            if (!"state".equals(evt.getPropertyName())) {
                return;
            }
            if (evt.getNewValue() != SwingWorker.StateValue.DONE) {
                return;
            }

            try {
                RunLinterWorker.RunResult runResult = worker.get();
                onRunCompleted(runResult);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                showWarning("Run interrupted", "The linter run was interrupted.");
            } catch (ExecutionException ex) {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                showWarning("Run failed", "The linter failed:\n" + cause.getMessage());
            } finally {
                activeWorker = null;
                setRunning(false);
            }
        });

        worker.execute();
    }

    /**
     * Returns instantiated checks for the user's current selection.
     * Used later by the background runner.
     */
    public List<CheckDescriptor> getSelectedCheckDescriptors() {
        List<CheckDescriptor> selected = new ArrayList<>();
        selected.addAll(cursoryTab.getSelectedDescriptors());
        selected.addAll(principleTab.getSelectedDescriptors());
        selected.addAll(patternTab.getSelectedDescriptors());
        return selected;
    }

    private List<CheckDescriptor> getSelectedChecks() {
        return getSelectedCheckDescriptors();
    }

    private void setRunning(boolean running) {
        browseButton.setEnabled(!running);
        defaultButton.setEnabled(!running);
        runButton.setEnabled(!running);

        cursoryTab.setEnabledAll(!running);
        principleTab.setEnabledAll(!running);
        patternTab.setEnabledAll(!running);

        progressBar.setIndeterminate(running);
        progressBar.setString(running ? "Running…" : "Idle");
    }

    private void showWarning(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private void onRunCompleted(RunLinterWorker.RunResult runResult) {
        LinterResult result = runResult.result();

        summaryArea.setText(
                "Run complete.\n\n" +
                        "Project: " + result.getProjectPath() + "\n" +
                        "Classes loaded: " + result.getTotalClasses() + "\n" +
                        "Checks run: " + (result.getCursoryCheckCount() + result.getPrincipleCheckCount() + result.getPatternCheckCount()) + "\n" +
                        "Violations: " + result.getTotalViolationCount() + "\n"
        );

        rawReportArea.setText(runResult.rawReport());
        rawReportArea.setCaretPosition(0);

        populateAccordionResults(result, runResult);
    }

    private void populateAccordionResults(LinterResult result, RunLinterWorker.RunResult runResult) {
        List<ResultsAccordionPanel.CheckGroup> groups = new ArrayList<>();
        groups.addAll(buildGroups("Cursory", result.getCursoryResults(), runResult.cursoryCheckNames()));
        groups.addAll(buildGroups("Principle", result.getPrincipleResults(), runResult.principleCheckNames()));
        groups.addAll(buildGroups("Pattern", result.getPatternResults(), runResult.patternCheckNames()));
        accordionPanel.setGroups(groups);
    }

    private List<ResultsAccordionPanel.CheckGroup> buildGroups(
            String category,
            List<CheckResult> results,
            List<String> checkNames
    ) {
        List<ResultsAccordionPanel.CheckGroup> groups = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            CheckResult cr = results.get(i);
            String checkName = (i < checkNames.size()) ? checkNames.get(i) : ("Check#" + (i + 1));
            List<Violation> violations = cr.getViolations();
            groups.add(new ResultsAccordionPanel.CheckGroup(category, checkName, violations));
        }
        return groups;
    }

    /**
     * UI component for one category's check list + selection buttons.
     */
    private static final class CheckTabPanel extends JPanel {
        private final List<CheckDescriptor> descriptors;
        private final List<JCheckBox> checkBoxes = new ArrayList<>();

        CheckTabPanel(List<CheckDescriptor> descriptors) {
            super(new BorderLayout(8, 8));
            this.descriptors = new ArrayList<>(descriptors);

            add(buildList(), BorderLayout.CENTER);

            resetToDefaults();
        }

        private JComponent buildList() {
            JPanel listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            listPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

            for (CheckDescriptor d : descriptors) {
                JCheckBox cb = new JCheckBox(d.displayName());
                cb.putClientProperty("descriptor", d);
                checkBoxes.add(cb);
                listPanel.add(cb);
            }

            JScrollPane scroll = new JScrollPane(listPanel);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            return scroll;
        }

        void setAll(boolean selected) {
            for (JCheckBox cb : checkBoxes) {
                cb.setSelected(selected);
            }
        }

        void resetToDefaults() {
            for (int i = 0; i < descriptors.size(); i++) {
                checkBoxes.get(i).setSelected(descriptors.get(i).defaultSelected());
            }
        }

        List<CheckDescriptor> getSelectedDescriptors() {
            List<CheckDescriptor> selected = new ArrayList<>();
            for (int i = 0; i < descriptors.size(); i++) {
                if (checkBoxes.get(i).isSelected()) {
                    selected.add(descriptors.get(i));
                }
            }
            return selected;
        }

        void setEnabledAll(boolean enabled) {
            for (JCheckBox cb : checkBoxes) {
                cb.setEnabled(enabled);
            }
        }
    }
}

