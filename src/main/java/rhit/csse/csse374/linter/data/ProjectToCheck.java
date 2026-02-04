package rhit.csse.csse374.linter.data;

import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data-layer object representing a "project" (or codebase) to lint.
 *
 * Holds:
 * - The original project path
 * - Parsed ASM ClassNode objects for all .class files found
 */
public class ProjectToCheck {

    private final String projectPath;
    private final List<ClassNode> classNodes;

    public ProjectToCheck(String projectPath) {
        this.projectPath = projectPath;
        this.classNodes = new ArrayList<>();
    }

    public ProjectToCheck(String projectPath, List<ClassNode> classNodes) {
        this.projectPath = projectPath;
        this.classNodes = new ArrayList<>(classNodes);
    }

    public String getProjectPath() {
        return projectPath;
    }

    public List<ClassNode> getClassNodes() {
        return Collections.unmodifiableList(classNodes);
    }

    public void addClassNode(ClassNode classNode) {
        this.classNodes.add(classNode);
    }
}

