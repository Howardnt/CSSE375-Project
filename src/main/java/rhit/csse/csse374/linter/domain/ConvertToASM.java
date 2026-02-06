package rhit.csse.csse374.linter.domain;

import rhit.csse.csse374.linter.data.ASMProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

/**
 * Domain-layer class responsible for converting a project location into a representation the linter can analyze.
 *
 * This class:
 * - Locates compiled `.class` files in the given directory (recursively)
 * - Parses bytecode using ASM into ClassNode objects
 * - Produces `ASMProject` instances containing the parsed ClassNodes
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
     * Converts project locations to ASMProject objects with parsed ClassNodes.
     * Each location should point to a directory containing compiled .class files.
     */
    public List<ASMProject> toASMProjects() {
        List<ASMProject> projects = new ArrayList<>();
        for (String location : projectLocation) {
            List<ClassNode> classNodes = loadClassesFromPath(location);
            projects.add(new ASMProject(location, classNodes));
        }
        return projects;
    }

    /**
     * Loads all .class files from the given path and parses them into ClassNodes.
     *
     * @param path Directory path containing .class files, or a single .class file
     * @return List of parsed ClassNode objects
     */
    private List<ClassNode> loadClassesFromPath(String path) {
        List<ClassNode> classNodes = new ArrayList<>();
        File file = new File(path);

        if (!file.exists()) {
            System.err.println("Warning: Path does not exist: " + path);
            return classNodes;
        }

        if (file.isDirectory()) {
            findAndLoadClassFiles(file, classNodes);
        } else if (file.getName().endsWith(".class")) {
            ClassNode node = loadClassFile(file);
            if (node != null) {
                classNodes.add(node);
            }
        }

        return classNodes;
    }

    /**
     * Recursively finds all .class files in the directory and loads them.
     */
    private void findAndLoadClassFiles(File directory, List<ClassNode> classNodes) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                findAndLoadClassFiles(file, classNodes);
            } else if (file.getName().endsWith(".class")) {
                ClassNode node = loadClassFile(file);
                if (node != null) {
                    classNodes.add(node);
                }
            }
        }
    }

    /**
     * Loads a single .class file and parses it into a ClassNode.
     * Uses the same ASM pattern as MyFirstLinter example.
     */
    private ClassNode loadClassFile(File classFile) {
        try (InputStream inputStream = new FileInputStream(classFile)) {
            // Step 1: ASM's ClassReader parses the compiled Java class
            ClassReader reader = new ClassReader(inputStream);
            // Step 2: ClassNode is the data container for the parsed class
            ClassNode classNode = new ClassNode();
            // Step 3: Tell the Reader to parse and store data in ClassNode
            // EXPAND_FRAMES is required for proper analysis
            reader.accept(classNode, ClassReader.EXPAND_FRAMES);
            return classNode;
        } catch (IOException e) {
            System.err.println("Warning: Failed to load class file: " + classFile.getPath());
            e.printStackTrace();
            return null;
        }
    }
}