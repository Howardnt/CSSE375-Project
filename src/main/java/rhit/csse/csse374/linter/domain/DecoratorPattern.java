package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMProject;

import java.util.ArrayList;
import java.util.List;

// Noah Howard
public class DecoratorPattern implements Pattern {

    @Override
    public CheckResult runPatternCheck(ASMProject project) {
        List<Violation> violations = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int totalMethods = 0;
        int totalClasses = project.getClasses().size();

        for (ASMClass cls : project.getClasses()) {
            totalMethods += cls.getMethods().size();
            
            try {
                if (isDecorator(cls)) {
                    violations.add(new PatternViolation("Decorator Pattern detected in class: " + cls.getClassName()));
                }
            } catch (Exception e) {
                errors.add("Error analyzing " + cls.getClassName() + ": " + e.getMessage());
            }
        }

        return new CheckResult(violations, totalClasses, totalMethods, errors, "Decorator Pattern");
    }

    private boolean isDecorator(ASMClass cls) {
        ClassNode node = cls.getClassNode();
        
        String superName = node.superName;
        List<String> interfaces = node.interfaces;

        for (FieldNode field : node.fields) {
            String fieldType = getCleanType(field.desc);

            boolean matchesSuper = fieldType.equals(superName);
            boolean matchesInterface = interfaces != null && interfaces.contains(fieldType);

            if (matchesSuper || matchesInterface) {
                if (!fieldType.equals("java/lang/Object")) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getCleanType(String desc) {
        if (desc.startsWith("L") && desc.endsWith(";")) {
            return desc.substring(1, desc.length() - 1);
        }
        return desc;
    }

    public class PatternViolation implements Violation {
        private final String message;

        public PatternViolation(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }
    }

}

