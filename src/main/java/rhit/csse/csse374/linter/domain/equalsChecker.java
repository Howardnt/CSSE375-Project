package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.Type;
import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMMethod;
import rhit.csse.csse374.linter.data.Instruction;
import rhit.csse.csse374.linter.data.LinterOutputText;
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

    private int classesChecked = 0;
    private int methodsChecked = 0;
    private List<String> analysisErrors = new ArrayList<>();

    // Inner class to represent a violation
    public static class EqualsViolation implements Violation{
        private final String className;
        private final String methodName;
        private final String type1;
        private final String type2;

        public EqualsViolation(String className, String methodName, String type1, String type2) {
            this.className = className;
            this.methodName = methodName;
            this.type1 = type1;
            this.type2 = type2;
        }

        public String getClassName() {
            return className;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getType1() {
            return type1;
        }

        public String getType2() {
            return type2;
        }

        @Override
        public String toString() {
            return "== comparison in " + className + "." + methodName +
                    " (comparing " + type1 + " and " + type2 + ")";
        }
    }

    /**
     * Checks a project for == comparison violations.
     *
     * @param ASMProject project
     * @return CheckResult containing violations and statistics
     */
    public CheckResult checkProject(ASMProject project) {

        classesChecked = 0;
        methodsChecked = 0;
        analysisErrors = new ArrayList<>();

        List<Violation> violations = new ArrayList<>();

        for (ASMClass clazz : project.getClasses()) {
            violations.addAll(checkClass(clazz));
        }

        return new CheckResult(violations, classesChecked, methodsChecked, analysisErrors, "== comparison");
    }

    @Override
    public String name() {
        return "EqualsReferenceComparison";
    }

    @Override
    public void run(ASMProject project, LinterOutputText report) {
        classesChecked = 0;
        methodsChecked = 0;
        analysisErrors = new ArrayList<>();

        List<Violation> violations = new ArrayList<>();
        for (ASMClass clazz : project.getClasses()) {
            violations.addAll(checkClass(clazz));
        }

        CheckResult result = new CheckResult(violations, classesChecked, methodsChecked, analysisErrors, "== comparison");
        report.addLine("CURSORY: " + result.toString());
        for (Violation v : violations) {
            report.addLine("         " + v.toString());
        }
        for (String err : analysisErrors) {
            report.addLine("         [analysis] " + err);
        }
    }

    private List<EqualsViolation> checkClass(ASMClass clazz) {
        classesChecked++;
        List<EqualsViolation> violations = new ArrayList<>();

        for (ASMMethod method : clazz.getMethods()) {
            violations.addAll(checkMethod(method));
        }

        return violations;
    }

    private List<EqualsViolation> checkMethod(ASMMethod method) {
        methodsChecked++;
        List<EqualsViolation> violations = new ArrayList<>();

        // Check if analysis succeeded
        if (!method.isAnalysisSuccessful()) {
            analysisErrors.add("Analysis failed for " + method.getFullMethodName());
            return violations;
        }

        // Iterate through instructions
        for (Instruction instruction : method.getInstructions()) {
            // Check if this is a reference comparison instruction
            if (instruction.isReferenceComparison()) {
                // Get the two values being compared
                StackValuePair valuePair = instruction.getTopTwoStackValues();

                if (valuePair != null && !valuePair.eitherIsNull() && valuePair.bothHaveType()) {
                    Type type1 = valuePair.getFirstType();
                    Type type2 = valuePair.getSecondType();

                    // Check if either type is problematic
                    if (isProblematicType(type1) || isProblematicType(type2)) {
                        violations.add(createViolation(
                                method.getClassName(),
                                method.getMethodName(),
                                type1,
                                type2
                        ));
                    }
                }
            }
        }

        return violations;
    }

    private boolean isProblematicType(Type type) {
        if (type.getSort() != Type.OBJECT) {
            return false;
        }

        String typeName = type.getInternalName();

        if (typeName.equals("java/lang/String")) {
            return true;
        }

        if (isWrapperType(typeName)) {
            return true;
        }

        return isCollectionType(typeName);
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

    private EqualsViolation createViolation(String className, String methodName,
                                            Type type1, Type type2) {
        String type1Name = type1 != null ? type1.getClassName() : "unknown";
        String type2Name = type2 != null ? type2.getClassName() : "unknown";

        return new EqualsViolation(className, methodName, type1Name, type2Name);
    }
}