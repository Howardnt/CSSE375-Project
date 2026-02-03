package rhit.csse.csse374.linter.data;

/**
 * Data-layer object representing a "project" (or codebase) to lint.
 *
 * In early milestones this may be just a filesystem path. Later, this can evolve to hold:
 * - discovered class files / source files
 * - parsed ASM trees
 * - metadata (build output directory, module name, owner, etc.)
 */
public class ProjectToCheck {

    private final String projectPath;

    public ProjectToCheck(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getProjectPath() {
        return projectPath;
    }
}

