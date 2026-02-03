package rhit.csse.csse374.linter.domain;

import rhit.csse.csse374.linter.data.ProjectToCheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Domain-layer class responsible for converting a project location into a representation the linter can analyze.
 *
 * In later iterations, this will likely:
 * - locate compiled `.class` files (or source `.java` files)
 * - parse bytecode using ASM into visitors / tree nodes
 * - produce `ProjectToCheck` instances enriched with parsed structures
 *
 * For the skeleton, this just wraps the configured locations into {@link ProjectToCheck} objects.
 */
public class ConvertToASM {

    // Field name matches the UML attribute: "ProjectLocation: List<String>"
    private final List<String> projectLocation;

    public ConvertToASM(List<String> projectLocation) {
        this.projectLocation = new ArrayList<>(projectLocation);
    }

    public List<String> getProjectLocation() {
        return Collections.unmodifiableList(projectLocation);
    }

    /**
     * Skeleton conversion step used by {@code presentation.Main}.
     */
    public List<ProjectToCheck> toProjectsToCheck() {
        List<ProjectToCheck> projects = new ArrayList<>();
        for (String location : projectLocation) {
            projects.add(new ProjectToCheck(location));
        }
        return projects;
    }
}

