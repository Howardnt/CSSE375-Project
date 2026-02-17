package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMMethod;

import java.util.ArrayList;
import java.util.List;

public class InterfaceSegregationPrinciple extends Principle {

    private static final int EMPTY_METHOD_THRESHOLD = 2;

    public InterfaceSegregationPrinciple() {
        super("Interface Segregation Principle");
    }

    @Override
    public String name() {
        return "ISP-Violation-Checker";
    }

    @Override
    public List<Violation> checkClass(ASMClass cls) {
        List<Violation> violations = new ArrayList<>();
        
        if (cls.getClassNode().interfaces.isEmpty()) {
            return violations;
        }

        int emptyMethods = 0;
        List<String> suspectMethods = new ArrayList<>();

        for (ASMMethod method : cls.getMethods()) {
            if (isEmptyOrThrowsException(method.getMethodNode())) {
                emptyMethods++;
                suspectMethods.add(method.getMethodName());
            }
        }

        if (emptyMethods >= EMPTY_METHOD_THRESHOLD) {
            String msg = String.format("Potential ISP Violation: %s implements interfaces but has %d empty/dummy methods: %s", 
                                        cls.getClassName(), emptyMethods, suspectMethods);
            violations.add(new Violation(msg, cls.getClassName(), "WARNING"));
        }

        return violations;
    }

    private boolean isEmptyOrThrowsException(MethodNode mn) {
        if (mn.name.equals("<init>") || mn.name.equals("<clinit>")) return false;

        InsnList instructions = mn.instructions;
        if (instructions.size() == 0) return true;

        for (AbstractInsnNode insn : instructions.toArray()) {
            if (insn.getOpcode() == org.objectweb.asm.Opcodes.NEW) {
                String type = ((org.objectweb.asm.tree.TypeInsnNode) insn).desc;
                if (type.contains("UnsupportedOperationException")) {
                    return true;
                }
            }
        }
        return instructions.size() < 5; 
    }
}