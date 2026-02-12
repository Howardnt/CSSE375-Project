package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.Type;
import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMMethod;
import rhit.csse.csse374.linter.data.Instruction;
import rhit.csse.csse374.linter.data.StackValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks for problematic use of == operator on reference types.
 * Refactored to use ASMProject for cleaner separation of concerns.
 *
 * Jack Traversa (with Claude assistance in accordance with the requirements document)
 */
public class equalsChecker implements Cursory {

    @Override
    public String name() {
        return "EqualsOperatorChecker";
    }

    @Override
    public CheckResult run(ASMProject project) {
        List<Violation> violations = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int totalMethods = 0;
        int totalClasses = project.getClasses().size();

        for (ASMClass cls : project.getClasses()) {
            totalMethods += cls.getMethods().size();

            try {
                violations.addAll(checkClass(cls));
            } catch (Exception e) {
                errors.add("Error analyzing " + cls.getClassName() + ": " + e.getMessage());
            }
        }

        return new CheckResult(violations, totalClasses, totalMethods, errors, name());
    }

    private List<Violation> checkClass(ASMClass cls) {
        List<Violation> violations = new ArrayList<>();

        for (ASMMethod method : cls.getMethods()) {
            if (!method.isAnalysisSuccessful()) {
                continue;
            }

            for (Instruction instruction : method.getInstructions()) {
                Violation violation = checkInstruction(instruction, method);
                if (violation != null) {
                    violations.add(violation);
                }
            }
        }

        return violations;
    }

    private Violation checkInstruction(Instruction instruction, ASMMethod method) {
        if (!instruction.isReferenceComparison()) {
            return null;
        }

        StackValuePair valuePair = instruction.getTopTwoStackValues();
        if (valuePair == null || valuePair.eitherIsNull() || !valuePair.bothHaveType()) {
            return null;
        }

        Type type1 = valuePair.getFirstType();
        Type type2 = valuePair.getSecondType();

        if (isProblematicType(type1) || isProblematicType(type2)) {
            String type1Name = type1 != null ? type1.getClassName() : "unknown";
            String type2Name = type2 != null ? type2.getClassName() : "unknown";

            String message = "Using == to compare " + type1Name + " and " + type2Name +
                    " (should use .equals() instead)";
            String location = method.getClassName() + "." + method.getMethodName();

            return new Violation(message, location, "WARNING");
        }

        return null;
    }

    private boolean isProblematicType(Type type) {
        if (type.getSort() != Type.OBJECT) {
            return false;
        }

        String typeName = type.getInternalName();
        return typeName.equals("java/lang/String") ||
                isWrapperType(typeName) ||
                isCollectionType(typeName);
    }

    private boolean isWrapperType(String typeName) {
        return typeName.equals("java/lang/Integer") ||
                typeName.equals("java/lang/Long") ||
                typeName.equals("java/lang/Double") ||
                typeName.equals("java/lang/Float") ||
                typeName.equals("java/lang/Boolean") ||
                typeName.equals("java/lang/Character") ||
                typeName.equals("java/lang/Byte") ||
                typeName.equals("java/lang/Short");
    }

    private boolean isCollectionType(String typeName) {
        return typeName.startsWith("java/util/List") ||
                typeName.startsWith("java/util/Set") ||
                typeName.startsWith("java/util/Map") ||
                typeName.startsWith("java/util/ArrayList") ||
                typeName.startsWith("java/util/HashMap") ||
                typeName.startsWith("java/util/HashSet");
    }
}