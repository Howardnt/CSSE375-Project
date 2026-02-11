package rhit.csse.csse374.linter.domain;

import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.data.LinterOutputText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Domain-layer facade responsible for running the configured linter checks over the configured projects.
 *
 * This class is the central coordinator in the domain layer (per the UML). It holds lists of:
 * - patterns (pattern detectors)
 * - principles (principle violation checks)
 * - cursories (cursory/style checks)
 * - projects (targets to lint, containing parsed ClassNodes)
 */
public class LinterHandler {

    // Field names intentionally match the UML attribute labels (including "Patters" typo).
    private final List<Pattern> patterns;
    private final List<Principle> principles;
    private final List<Cursory> cursories;
    private final ASMProject project;

    public LinterHandler(
            List<Pattern> patters,
            List<Principle> principles,
            List<Cursory> cursories,
            ASMProject project
    ) {
        this.patterns = new ArrayList<>(patters);
        this.principles = new ArrayList<>(principles);
        this.cursories = new ArrayList<>(cursories);
        this.project = project;
    }

    public List<Pattern> getPatterns() {
        return Collections.unmodifiableList(patterns);
    }

    public List<Principle> getPrinciples() {
        return Collections.unmodifiableList(principles);
    }

    public List<Cursory> getCursories() {
        return Collections.unmodifiableList(cursories);
    }

    public ASMProject getProject() { return project; }

    /**
     * Matches UML operation: OutputLinterResult():LinterOutputText
     *
     * Runs all configured checks over all loaded classes and returns the results.
     */
    public LinterOutputText outputLinterResult() {
        LinterOutputText output = new LinterOutputText();
        output.addLine("=== Linter Analysis Report ===");
        output.addLine("");

        int totalClasses = project.getClasses().size();

        output.addLine("Project analyzed.");
        output.addLine("Total classes loaded: " + totalClasses);
        output.addLine("Cursory checks: " + cursories.size());
        output.addLine("Principle checks: " + principles.size());
        output.addLine("Pattern detectors: " + patterns.size());
        output.addLine("");

        // List loaded classes for each project
        output.addLine("Project: " + project.getProjectPath());
        List<ASMClass> classNodes = project.getClasses();
        if (classNodes.isEmpty()) {
            output.addLine("  No .class files found");
        } else {
            output.addLine("  Classes loaded: " + classNodes.size());
            for (ASMClass classNode : classNodes) {
                output.addLine("    - " + classNode.getClassName());
            }
        }
        output.addLine("");

        runChecksSection("Cursory checks", cursories, output);
        runChecksSection("Principle checks", principles, output);
        runChecksSection("Pattern detectors", patterns, output);

        return output;
    }

    private void runChecksSection(String sectionTitle, List<? extends LintCheck> checks, LinterOutputText output) {
        if (checks.isEmpty()) {
            return;
        }
        output.addLine("=== " + sectionTitle + " ===");
        for (LintCheck check : checks) {
            output.addLine("[Check] " + check.name());
            check.run(project, output);
            output.addLine("");
        }
    }
}

