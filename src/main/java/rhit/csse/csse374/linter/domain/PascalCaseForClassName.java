package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Cursory check that verifies class names follow PascalCase naming convention.
 *
 * PascalCase rules:
 * - First character must be an uppercase letter
 * - Name should only contain letters and digits (no underscores or special characters)
 * - Name should not be all uppercase (that's typically for constants)
 */
public class PascalCaseForClassName implements Cursory {

    @Override
    public List<String> check(ClassNode classNode) {
        List<String> violations = new ArrayList<>();

        // Get the simple class name (without package)
        // Raw data format: classNode.name is the internal JVM name using slashes for package separation.
        // Examples: "java/lang/String", "rhit/csse/csse374/linter/domain/PascalCaseForClassName"
        String fullName = classNode.name;
        String simpleName = getSimpleClassName(fullName);

        // Skip anonymous or synthetic classes (e.g., "ClassName$1")
        if (simpleName.contains("$")) {
            String outerClassName = simpleName.substring(0, simpleName.indexOf('$'));
            if (!isPascalCase(outerClassName)) {
                violations.add(buildViolationMessage(fullName, outerClassName,
                        "Outer class name does not follow PascalCase"));
            }
            return violations;
        }

        if (!isPascalCase(simpleName)) {
            violations.add(buildViolationMessage(fullName, simpleName,
                    describeViolation(simpleName)));
        }

        return violations;
    }

    /**
     * Extracts the simple class name from the full internal name.
     *
     * @param fullName the internal name (e.g., "com/example/MyClass")
     * @return the simple class name (e.g., "MyClass")
     */
    private String getSimpleClassName(String fullName) {
        // The raw data uses forward slashes '/' as package separators in internal names, not dots.
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

    /**
     * Builds a formatted violation message.
     *
     * @param fullName the full internal class name
     * @param simpleName the simple class name
     * @param reason the reason for the violation
     * @return a formatted violation message
     */
    private String buildViolationMessage(String fullName, String simpleName, String reason) {
        String packageName = getPackageName(fullName);
        String location = packageName.isEmpty() ? simpleName : packageName + "." + simpleName;
        return "PascalCase violation in class '" + location + "': " + reason;
    }

    /**
     * Extracts the package name from the full internal name.
     *
     * @param fullName the internal name (e.g., "com/example/MyClass")
     * @return the package name with dots (e.g., "com.example")
     */
    private String getPackageName(String fullName) {
        int lastSlash = fullName.lastIndexOf('/');
        if (lastSlash < 0) {
            return "";
        }
        return fullName.substring(0, lastSlash).replace('/', '.');
    }
}
