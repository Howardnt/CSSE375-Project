package rhit.csse.csse374.linter.domain;

import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.data.LinterOutputText;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

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

        // Run cursory checks and collect violations
        List<String> cursoryViolations = new ArrayList<>();
        for (Cursory cursory : cursories) {
            CheckResult cursoryCheckResult = cursory.runCursoryCheck(project);
            for (Object v : cursoryCheckResult.getViolations()) {
                cursoryViolations.add(v.toString());
            }
        }

        // Run pattern checks and collect violations
        List<String> patternViolations = new ArrayList<>();
        for (Pattern pattern : patterns) {
            CheckResult patternCheckResult = pattern.runPatternCheck(project);
            for (Object v : patternCheckResult.getViolations()) {
                patternViolations.add(v.toString());
            }
        }

        // Output Cursory violations
        output.addLine("=== Cursory Check Results ===");
        if (cursoryViolations.isEmpty()) {
            output.addLine("No violations found.");
        } else {
            output.addLine("Violations found: " + cursoryViolations.size());
            for (String violation : cursoryViolations) {
                output.addLine("  - " + violation);
            }
        }
        output.addLine("");

        // Output Pattern violations
        output.addLine("=== Pattern Check Results ===");
        if (patternViolations.isEmpty()) {
            output.addLine("No violations found.");
        } else {
            output.addLine("Violations found: " + patternViolations.size());
            for (String violation : patternViolations) {
                output.addLine("  - " + violation);
            }
        }
        output.addLine("");

        return output;
    }
}
