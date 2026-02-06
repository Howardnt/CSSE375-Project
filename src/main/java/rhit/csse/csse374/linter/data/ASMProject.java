package rhit.csse.csse374.linter.data;

import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Domain-friendly representation of a project's bytecode analysis results.
 *
 * This class wraps ASM ClassNode objects and provides a cleaner interface
 * for linters/checkers, abstracting away the complexities of ASM.
 */
public class ASMProject {

    private final String projectPath;
    private final List<ASMClass> classes;

    /**
     * Creates an ASMProject from a list of ClassNodes.
     *
     * @param projectPath The path to the project being analyzed
     * @param classNodes The ASM ClassNode objects to analyze
     */
    public ASMProject(String projectPath, List<ClassNode> classNodes) {
        this.projectPath = projectPath;
        this.classes = new ArrayList<>();

        for (ClassNode classNode : classNodes) {
            this.classes.add(new ASMClass(classNode));
        }
    }

    public String getProjectPath() {
        return projectPath;
    }

    public List<ASMClass> getClasses() {
        return Collections.unmodifiableList(classes);
    }
}