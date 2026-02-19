package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.Opcodes;
import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMMethod;
import rhit.csse.csse374.linter.data.Instruction;

import java.util.ArrayList;
import java.util.List;

/**
 * Open/Closed Principle (OCP) Linter
 * * Targets: Excessive type-checking (instanceof/switch) and "Closed" concrete classes
 * that lack extension points but perform type-specific logic.
 */
//Jack Traversa (with Claude assistance in accordance with the requirements document)
public class OpenClosedPrinciple extends Principle {

    // Threshold of 3 distinct checks helps avoid flagging simple logic
    private static final int METHOD_VIOLATION_THRESHOLD = 3;
    private static final int RIGID_CLASS_THRESHOLD = 2;

    @Override
    public List<Violation> checkClass(ASMClass cls) {
        List<Violation> violations = new ArrayList<>();

        checkTypeCheckingLogic(cls, violations);
        checkStructuralRigidity(cls, violations);

        return violations;
    }

    //checks for too much type branching
    private void checkTypeCheckingLogic(ASMClass cls, List<Violation> violations) {
        for (ASMMethod method : cls.getMethods()) {

            if (!method.isAnalysisSuccessful() ||
                    method.getMethodName().equals("equals") ||
                    method.getMethodName().equals("<init>")) {
                continue;
            }

            int typeCheckCount = countTypeCheckingOpcodes(method);

            if (typeCheckCount >= METHOD_VIOLATION_THRESHOLD) {
                String location = cls.getClassName() + "." + method.getMethodName();
                violations.add(new Violation(
                        String.format("Potential OCP violation: Method uses %d distinct type-checks. " +
                                "Consider replacing conditional logic with polymorphism.", typeCheckCount),
                        location,
                        "WARNING"
                ));
            }
        }
    }

    //checks classes that are naturally closed to extension (final classes, no interfaces/abstract/extends anything) + contains type-branching
    private void checkStructuralRigidity(ASMClass cls, List<Violation> violations) {
        int access = cls.getClassNode().access;

        boolean isAbstract  = (access & Opcodes.ACC_ABSTRACT)  != 0;
        boolean isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
        boolean isFinal     = (access & Opcodes.ACC_FINAL)     != 0;

        boolean hasInterfaces = !cls.getClassNode().interfaces.isEmpty();
        boolean extendsOther  = cls.getClassNode().superName != null
                && !cls.getClassNode().superName.equals("java/lang/Object");

        // If it's already an abstraction or designed for extension, it's fine
        if (isAbstract || isInterface || hasInterfaces || extendsOther) {
            return;
        }

        boolean hasTypeLogic = cls.getMethods().stream()
                .filter(m -> !m.getMethodName().equals("equals") && !m.getMethodName().equals("<init>"))
                .anyMatch(method -> countTypeCheckingOpcodes(method) >= RIGID_CLASS_THRESHOLD);

        if (hasTypeLogic) {
            String reason = isFinal ? "class is final" : "concrete class has no extension points";
            violations.add(new Violation(
                    "OCP Risk: This " + reason + " but contains type-branching logic. " +
                            "This makes the class difficult to extend without modifying existing source code.",
                    cls.getClassName(),
                    "WARNING"
            ));
        }
    }

    /**
     * Counts INSTANCEOF, TABLESWITCH, and LOOKUPSWITCH opcodes.
     * Note: A switch block (regardless of case count) counts as ONE distinct check point.
     */
    private int countTypeCheckingOpcodes(ASMMethod method) {
        int count = 0;
        for (Instruction instruction : method.getInstructions()) {
            int op = instruction.getOpcode();
            if (op == Opcodes.INSTANCEOF || op == Opcodes.TABLESWITCH || op == Opcodes.LOOKUPSWITCH) {
                count++;
            }
        }
        return count;
    }

    @Override
    public String name() {
        return "Open/Closed Principle";
    }
}