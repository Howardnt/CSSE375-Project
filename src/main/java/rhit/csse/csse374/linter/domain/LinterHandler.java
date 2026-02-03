package rhit.csse.csse374.linter.domain;

import rhit.csse.csse374.linter.data.LinterOutputText;
import rhit.csse.csse374.linter.data.ProjectToCheck;

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
 * - projects (targets to lint)
 *
 * The skeleton implementation produces a basic report, but does not perform real analysis yet.
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
     * Skeleton behavior: return a simple report describing what would be run.
     */
    public LinterOutputText outputLinterResult() {
        LinterOutputText output = new LinterOutputText();
        output.addLine("Linter skeleton run");
        output.addLine("Projects: " + projects.size());
        output.addLine("Cursory checks: " + cursories.size());
        output.addLine("Principle checks: " + principles.size());
        output.addLine("Pattern detectors: " + patters.size());
        output.addLine("");
        output.addLine("Next step: implement check contracts and run them over ProjectToCheck representations.");
        return output;
    }
}

