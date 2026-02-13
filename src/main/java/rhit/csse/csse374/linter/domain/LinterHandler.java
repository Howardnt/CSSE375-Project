package rhit.csse.csse374.linter.domain;

import rhit.csse.csse374.linter.data.ASMProject;

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
 * - project (target to lint, containing parsed ClassNodes)
 */
public class LinterHandler {

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

    public ASMProject getProject() {
        return project;
    }

    /**
     * Runs all configured checks over all loaded classes and returns the results.
     *
     * This is the main domain-layer operation. It executes all checks and aggregates
     * their results into a LinterResult object that the presentation layer can format.
     *
     * @return LinterResult containing all check results and analysis metadata
     */
    public LinterResult runLinterAnalysis() {
        List<CheckResult> cursoryResults = runChecks(cursories);
        List<CheckResult> principleResults = runChecks(principles);
        List<CheckResult> patternResults = runChecks(patterns);

        return new LinterResult(
                cursoryResults,
                principleResults,
                patternResults,
                project.getClasses().size(),
                cursories.size(),
                principles.size(),
                patterns.size(),
                project.getProjectPath()
        );
    }

    /**
     * Executes a list of checks and collects their results.
     */
    private List<CheckResult> runChecks(List<? extends LintCheck> checks) {
        List<CheckResult> results = new ArrayList<>();
        for (LintCheck check : checks) {
            CheckResult result = check.run(project);
            results.add(result);
        }
        return results;
    }
}