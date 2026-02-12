package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMMethod;
import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.data.Instruction;

import java.util.ArrayList;
import java.util.List;

//Jack Traversa (with Claude assistance in accordance with the requirements document)
public class AdapterPattern implements Pattern {
    @Override
    public CheckResult runPatternCheck(ASMProject project) {
        List<Violation> violations = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int totalMethods = 0;
        int totalClasses = project.getClasses().size();

        for (ASMClass cls : project.getClasses()) {
            totalMethods += cls.getMethods().size();

            try {
                if (isAdapter(cls)) {
                    String adapteeField = getAdapteeFieldName(cls);
                    violations.add(new AdapterViolation(
                            cls.getClassName(),
                            adapteeField
                    ));
                }
            } catch (Exception e) {
                errors.add("Error analyzing " + cls.getClassName() + ": " + e.getMessage());
            }
        }

        return new CheckResult(violations, totalClasses, totalMethods, errors, "Adapter Pattern");
    }

    private boolean isAdapter(ASMClass asmClass) {
        // checking that the class implements something
        if (asmClass.getClassNode().interfaces.isEmpty()){
            return false;
        }

        // checking for the adaptee
        List<FieldNode> adapteeFields = findPotentialAdapteeFields(asmClass);
        if (adapteeFields.size() != 1) {
            return false;
        }

        // Most methods should utilize methods from that field
        double ratio = getRatio(asmClass);
        return ratio >= 60.0; // at least 60% of methods utilize the adaptee
    }

    private double getRatio(ASMClass asmClass) {
        List<FieldNode> adapteeFields = findPotentialAdapteeFields(asmClass);
        if (adapteeFields.isEmpty()) {
            return 0.0;
        }

        String adapteeFieldName = adapteeFields.get(0).name;
        String adapteeFieldOwner = asmClass.getClassName();

        int totalMethods = 0;
        int delegatingMethods = 0;

        for (ASMMethod method : asmClass.getMethods()) {
            //skips constructors and static initializers
            if (method.getMethodName().equals("<init>") ||
                    method.getMethodName().equals("<clinit>")) {
                continue;
            }

            totalMethods++;
            if (methodUsesField(method, adapteeFieldOwner, adapteeFieldName)) {
                delegatingMethods++;
            }
        }

        if (totalMethods == 0) {
            return 0.0;
        }

        return (delegatingMethods * 100.0) / totalMethods;
    }

    private boolean methodUsesField(ASMMethod method, String fieldOwner, String fieldName) {
        if (!method.isAnalysisSuccessful()) {
            return false;
        }

        for (Instruction insn : method.getInstructions()) {
            // Is this a GETFIELD instruction? (loading a field's value)
            if (insn.getOpcode() == Opcodes.GETFIELD) {
                FieldInsnNode fieldInsn = (FieldInsnNode) insn.getInstruction();

                if (fieldInsn.owner.equals(fieldOwner) &&   // Same class
                        fieldInsn.name.equals(fieldName)) {      // Same field name
                    return true;
                }
            }
        }

        return false;
    }

    //any adaptee should be private, final, and its own Object
    private List<FieldNode> findPotentialAdapteeFields(ASMClass asmClass) {
        List<FieldNode> candidates = new ArrayList<>();

        for (FieldNode field : asmClass.getClassNode().fields) {
            boolean isPrivate = (field.access & Opcodes.ACC_PRIVATE) != 0;
            boolean isFinal = (field.access & Opcodes.ACC_FINAL) != 0;
            boolean isNonPrimitive = field.desc.startsWith("L");

            if (isPrivate && isFinal && isNonPrimitive) {
                candidates.add(field);
            }
        }

        return candidates;
    }

    private String getAdapteeFieldName(ASMClass asmClass) {
        List<FieldNode> fields = findPotentialAdapteeFields(asmClass);
        return fields.isEmpty() ? "this should not happen" : fields.get(0).name;
    }

    public static class AdapterViolation implements Violation {
        private final String className;
        private final String adapteeField;

        public AdapterViolation(String className, String adapteeField) {
            this.className = className;
            this.adapteeField = adapteeField;
        }

        public String getClassName() {
            return className;
        }

        public String getAdapteeField() {
            return adapteeField;
        }

        @Override
        public String toString() {
            return String.format("Adapter Pattern detected in %s (adaptee field: %s)",
                    className, adapteeField);
        }
    }
}
