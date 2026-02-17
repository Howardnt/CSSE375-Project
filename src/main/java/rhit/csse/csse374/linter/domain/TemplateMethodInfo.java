package rhit.csse.csse374.linter.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds detailed information about a detected Template Method Pattern.
 * 
 * This class captures:
 * - The class containing the template method
 * - The name of the template method
 * - All abstract methods called by the template method
 */
public class TemplateMethodInfo {
    private final String className;
    private final String templateMethodName;
    private final List<String> abstractMethodsCalled;

    public TemplateMethodInfo(String className, String templateMethodName) {
        this.className = className;
        this.templateMethodName = templateMethodName;
        this.abstractMethodsCalled = new ArrayList<>();
    }

    public void addAbstractMethodCall(String methodName) {
        if (!abstractMethodsCalled.contains(methodName)) {
            abstractMethodsCalled.add(methodName);
        }
    }

    public String getClassName() {
        return className;
    }

    public String getTemplateMethodName() {
        return templateMethodName;
    }

    public List<String> getAbstractMethodsCalled() {
        return new ArrayList<>(abstractMethodsCalled);
    }

    /**
     * Formats the template method information as a readable string.
     */
    public String formatDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("Template Method: ").append(templateMethodName).append("()\n");

        if (abstractMethodsCalled.isEmpty()) {
            sb.append("    (no abstract methods called)");
        } else {
            for (String method : abstractMethodsCalled) {
                sb.append("    |-- calls abstract: ").append(method).append("()\n");
            }
        }

        return sb.toString().trim();
    }

    @Override
    public String toString() {
        return String.format("Template Method '%s' in %s calls %d abstract method(s): %s",
                templateMethodName,
                className.replace('/', '.'),
                abstractMethodsCalled.size(),
                abstractMethodsCalled.isEmpty() ? "none" : String.join(", ", abstractMethodsCalled));
    }
}
