package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.tree.ClassNode;
import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMProject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cursory check that verifies class names follow PascalCase naming convention.
 *
 * PascalCase rules:
 * - First character must be an uppercase letter
 * - Name should only contain letters and digits (no underscores or special
 * characters)
 * - Name should not be all uppercase (that's typically for constants)
 */
public class PascalClassName implements Cursory {

    public static class PascalClassNameViolation extends Violation {
        private final String fullName;
        private final String simpleName;
        private final String reason;

        public PascalClassNameViolation(String fullName, String simpleName, String reason) {
            super(buildMessage(fullName, simpleName, reason));
            this.fullName = fullName;
            this.simpleName = simpleName;
            this.reason = reason;
        }

        private static String buildMessage(String fullName, String simpleName, String reason) {
            String packageName = getPackageName(fullName);
            String location = packageName.isEmpty() ? simpleName : packageName + "." + simpleName;
            return "PascalCase violation in class '" + location + "': " + reason;
        }

        private static String getPackageName(String fullName) {
            int lastSlash = fullName.lastIndexOf('/');
            if (lastSlash < 0) {
                return "";
            }
            return fullName.substring(0, lastSlash).replace('/', '.');
        }
    }

    @Override
    public String name() {
        return "PascalCase Class Name";
    }

    @Override
    public CheckResult run(ASMProject project) {
        List<Violation> violations = new ArrayList<>();
        int classesChecked = 0;
        int methodsChecked = 0;
        List<String> errors = new ArrayList<>();

        for (ASMClass asmClass : project.getClasses()) {
            classesChecked++;
            ClassNode classNode = asmClass.getClassNode();
            methodsChecked += classNode.methods.size();

            // Get the simple class name (without package)
            String fullName = classNode.name;
            String simpleName = getSimpleClassName(fullName);

            // Skip anonymous or synthetic classes (e.g., "ClassName$1")
            if (simpleName.contains("$")) {
                String outerClassName = simpleName.substring(0, simpleName.indexOf('$'));
                if (!isPascalCase(outerClassName)) {
                    violations.add(new PascalClassNameViolation(fullName, outerClassName,
                            "Outer class name does not follow PascalCase"));
                }
                continue;
            }

            if (!isPascalCase(simpleName)) {
                violations.add(new PascalClassNameViolation(fullName, simpleName,
                        describeViolation(simpleName)));
            }
        }

        return new CheckResult(violations, classesChecked, methodsChecked, errors, "PascalCase Class Name");
    }

    /**
     * Extracts the simple class name from the full internal name.
     *
     * @param fullName the internal name (e.g., "com/example/MyClass")
     * @return the simple class name (e.g., "MyClass")
     */
    private String getSimpleClassName(String fullName) {
        // The raw data uses forward slashes '/' as package separators in internal
        // names, not dots.
        int lastSlash = fullName.lastIndexOf('/');
        return lastSlash >= 0 ? fullName.substring(lastSlash + 1) : fullName;
    }

    /**
     * Checks if a class name follows PascalCase convention.
     *
     * @param name the simple class name to check
     * @return true if the name follows PascalCase, false otherwise
     */
    private boolean isPascalCase(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        // First character must be uppercase letter
        if (!Character.isUpperCase(name.charAt(0))) {
            return false;
        }

        // Name should only contain letters and digits
        for (char c : name.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }

        // Name should not be all uppercase (unless it's a single character)
        if (name.length() > 1 && name.equals(name.toUpperCase())) {
            // Allow acronym-style names that are common (e.g., "URL", "HTTP")
            // But flag names that look like constants (e.g., "MYCLASS")
            // A simple heuristic: if length > 3 and all caps, it's likely a constant
            if (name.length() > 3) {
                return false;
            }
        }

        return true;
    }

    /**
     * Describes why a class name violates PascalCase convention.
     *
     * @param name the class name that violates the convention
     * @return a description of the violation
     */
    private String describeViolation(String name) {
        if (name == null || name.isEmpty()) {
            return "Class name is empty";
        }

        if (!Character.isUpperCase(name.charAt(0))) {
            return "Class name must start with an uppercase letter";
        }

        for (char c : name.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return "Class name contains invalid character: '" + c + "'";
            }
        }

        if (name.length() > 3 && name.equals(name.toUpperCase())) {
            return "Class name appears to be all uppercase (use PascalCase instead)";
        }

        return "Class name does not follow PascalCase convention";
    }
}
