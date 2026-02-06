package rhit.csse.csse374.linter.domain;

import rhit.csse.csse374.linter.data.LinterOutputText;
import rhit.csse.csse374.linter.data.ProjectToCheck;

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
    private final List<Pattern> patters;
    private final List<Principle> principles;
    private final List<Cursory> cursories;
    private final List<ProjectToCheck> projects;

    public LinterHandler(
            List<Pattern> patters,
            List<Principle> principles,
            List<Cursory> cursories,
            List<ProjectToCheck> projects
    ) {
        this.patters = new ArrayList<>(patters);
        this.principles = new ArrayList<>(principles);
        this.cursories = new ArrayList<>(cursories);
        this.projects = new ArrayList<>(projects);
    }

    public List<Pattern> getPatters() {
        return Collections.unmodifiableList(patters);
    }

    public List<Principle> getPrinciples() {
        return Collections.unmodifiableList(principles);
    }

    public List<Cursory> getCursories() {
        return Collections.unmodifiableList(cursories);
    }

    public List<ProjectToCheck> getProjects() {
        return Collections.unmodifiableList(projects);
    }

    /**
     * Matches UML operation: OutputLinterResult():LinterOutputText
     *
     * Runs all configured checks over all loaded classes and returns the results.
     */
    public LinterOutputText outputLinterResult() {
        LinterOutputText output = new LinterOutputText();
        output.addLine("=== Linter Analysis Report ===");
        output.addLine("");

        // Count total classes loaded
        int totalClasses = 0;
        for (ProjectToCheck project : projects) {
            totalClasses += project.getClassNodes().size();
        }

        output.addLine("Projects analyzed: " + projects.size());
        output.addLine("Total classes loaded: " + totalClasses);
        output.addLine("Cursory checks: " + cursories.size());
        output.addLine("Principle checks: " + principles.size());
        output.addLine("Pattern detectors: " + patters.size());
        output.addLine("");

        // List loaded classes for each project
        for (ProjectToCheck project : projects) {
            output.addLine("Project: " + project.getProjectPath());
            List<ClassNode> classNodes = project.getClassNodes();
            if (classNodes.isEmpty()) {
                output.addLine("  No .class files found");
            } else {
                output.addLine("  Classes loaded: " + classNodes.size());
                for (ClassNode classNode : classNodes) {
                    String className = Type.getObjectType(classNode.name).getClassName();
                    output.addLine("    - " + className);
                }
            }
            output.addLine("");
        }

        // Run cursory checks and collect violations
        List<String> allViolations = new ArrayList<>();
        for (ProjectToCheck project : projects) {
            for (ClassNode classNode : project.getClassNodes()) {
                for (Cursory cursory : cursories) {
                    List<String> violations = cursory.check(classNode);
                    allViolations.addAll(violations);
                }
            }
        }

        // Output violations
        output.addLine("=== Cursory Check Results ===");
        if (allViolations.isEmpty()) {
            output.addLine("No violations found.");
        } else {
            output.addLine("Violations found: " + allViolations.size());
            for (String violation : allViolations) {
                output.addLine("  - " + violation);
            }
        }
        output.addLine("");

        return output;
    }
}

