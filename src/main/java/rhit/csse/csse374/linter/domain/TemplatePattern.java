package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMProject;

import java.util.ArrayList;
import java.util.List;

// Enhanced Template Pattern analyzer with detailed method tracking
public class TemplatePattern extends Pattern {

    private List<TemplateMethodInfo> detectedPatterns = new ArrayList<>();

    public TemplatePattern() {
        // No super() call needed - Pattern base class doesn't require constructor args
    }

    @Override
    public String name() {
        return "Template Method";
    }

    @Override
    protected CheckResult runPatternCheck(ASMProject project) {
        List<Violation> violations = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int totalMethods = 0;
        int totalClasses = project.getClasses().size();

        for (ASMClass cls : project.getClasses()) {
            totalMethods += cls.getMethods().size();

            try {
                if (isPattern(cls)) {
                    // Create detailed violation message with template method info
                    String detailedMessage = getDetailedMessage(cls.getClassName());
                    violations.add(new Violation(
                            detailedMessage,
                            cls.getClassName(),
                            "INFO"));
                }
            } catch (Exception e) {
                errors.add("Error analyzing " + cls.getClassName() + ": " + e.getMessage());
            }
        }

        return new CheckResult(violations, totalClasses, totalMethods, errors, name());
    }

    @Override
    protected boolean isPattern(ASMClass cls) {
        ClassNode classNode = cls.getClassNode();
        detectedPatterns.clear(); // Reset for each class

        // Only check abstract classes for template method pattern
        if ((classNode.access & Opcodes.ACC_ABSTRACT) == 0) {
            return false;
        }

        // Check each non-abstract method in the abstract class
        for (MethodNode method : classNode.methods) {
            // Skip abstract methods and constructors
            if ((method.access & Opcodes.ACC_ABSTRACT) != 0) {
                continue;
            }
            if (method.name.startsWith("<")) {
                continue;
            }

            // Check if this method is a template method and collect details
            TemplateMethodInfo info = analyzeTemplateMethod(classNode, method);
            if (info != null && !info.getAbstractMethodsCalled().isEmpty()) {
                detectedPatterns.add(info);
            }
        }

        return !detectedPatterns.isEmpty();
    }

    /**
     * Analyzes a method to determine if it's a template method and collects
     * detailed information about which abstract methods it calls.
     * 
     * @return TemplateMethodInfo if it's a template method, null otherwise
     */
    private TemplateMethodInfo analyzeTemplateMethod(ClassNode classNode, MethodNode method) {
        TemplateMethodInfo info = new TemplateMethodInfo(classNode.name, method.name);

        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode) {
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                // Check if the method call is to the same class
                if (methodInsn.owner.equals(classNode.name)) {
                    // Check if the called method is abstract
                    for (MethodNode m : classNode.methods) {
                        if (m.name.equals(methodInsn.name) && m.desc.equals(methodInsn.desc)
                                && (m.access & Opcodes.ACC_ABSTRACT) != 0) {
                            info.addAbstractMethodCall(m.name);
                        }
                    }
                }
            }
        }

        return info.getAbstractMethodsCalled().isEmpty() ? null : info;
    }

    /**
     * Returns detailed information about all detected template method patterns.
     */
    public List<TemplateMethodInfo> getDetectedPatterns() {
        return new ArrayList<>(detectedPatterns);
    }

    /**
     * Generates a detailed violation message including all template methods found.
     */
    public String getDetailedMessage(String className) {
        if (detectedPatterns.isEmpty()) {
            return "Template Method Pattern detected in: " + className.replace('/', '.');
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Template Method Pattern detected in: ").append(className.replace('/', '.')).append("\n");

        for (TemplateMethodInfo info : detectedPatterns) {
            sb.append("  ").append(info.formatDetails().replace("\n", "\n  ")).append("\n");
        }

        return sb.toString().trim();
    }
}
