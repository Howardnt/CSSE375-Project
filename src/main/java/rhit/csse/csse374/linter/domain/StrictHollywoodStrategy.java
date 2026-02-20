package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMMethod;
import rhit.csse.csse374.linter.data.Instruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A zero-tolerance Hollywood Strategy.
 * Flags a violation if a subclass makes ANY upward call or instantiates its
 * high-level dependency.
 */
public class StrictHollywoodStrategy implements HollywoodStrategy {

    private static final Set<String> EXCLUDED_METHODS = new HashSet<>(Arrays.asList(
            "<init>", "<clinit>", "equals", "hashCode", "toString"));

    @Override
    public List<Violation> analyzeCoupling(ASMClass cls, Set<String> highLevelTypes,
            Set<String> highLevelMethodSignatures) {
        List<Violation> violations = new ArrayList<>();
        String currentClassName = cls.getClassNode().name;

        for (ASMMethod method : cls.getMethods()) {
            if (EXCLUDED_METHODS.contains(method.getMethodName())) {
                continue;
            }

            for (Instruction instruction : method.getInstructions()) {
                AbstractInsnNode insn = instruction.getInstruction();

                // Check Instantiations
                if (insn.getOpcode() == Opcodes.NEW && insn instanceof TypeInsnNode) {
                    TypeInsnNode typeInsn = (TypeInsnNode) insn;
                    if (highLevelTypes.contains(typeInsn.desc)) {
                        violations.add(new Violation(
                                "Strict Hollywood Violation: Low-level class absolutely cannot instantiate its high-level dependency '"
                                        + typeInsn.desc + "'.",
                                cls.getClassName() + "." + method.getMethodName(),
                                "ERROR"));
                    }
                }

                // Check Upward Calls
                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;

                    boolean isDirectUpwardCall = highLevelTypes.contains(methodInsn.owner);
                    boolean isSelfCallToHighLevelMethod = methodInsn.owner.equals(currentClassName)
                            && highLevelMethodSignatures.contains(methodInsn.name + methodInsn.desc);

                    if ((isDirectUpwardCall || isSelfCallToHighLevelMethod)
                            && !EXCLUDED_METHODS.contains(methodInsn.name)) {
                        violations.add(new Violation(
                                "Strict Hollywood Violation: Zero tolerance for upward calls. Method calls '"
                                        + methodInsn.name + "' from a high-level component.",
                                cls.getClassName() + "." + method.getMethodName(),
                                "ERROR"));
                    }
                }
            }
        }
        return violations;
    }
}
