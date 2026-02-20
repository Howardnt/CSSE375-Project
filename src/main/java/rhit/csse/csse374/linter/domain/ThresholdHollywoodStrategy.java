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
 * A tunable Hollywood Strategy based on a threshold.
 * Flags a violation only if the number of distinct upward calls exceeds the
 * provided threshold,
 * or if it instantiates its high-level dependency.
 */
public class ThresholdHollywoodStrategy implements HollywoodStrategy {

    private final int threshold;
    private static final Set<String> EXCLUDED_METHODS = new HashSet<>(Arrays.asList(
            "<init>", "<clinit>", "equals", "hashCode", "toString"));

    public ThresholdHollywoodStrategy(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public List<Violation> analyzeCoupling(ASMClass cls, Set<String> highLevelTypes,
            Set<String> highLevelMethodSignatures) {
        List<Violation> violations = new ArrayList<>();
        String currentClassName = cls.getClassNode().name;

        for (ASMMethod method : cls.getMethods()) {
            if (EXCLUDED_METHODS.contains(method.getMethodName())) {
                continue;
            }

            int upwardCallCount = 0;
            Set<String> calledMethods = new HashSet<>();

            for (Instruction instruction : method.getInstructions()) {
                AbstractInsnNode insn = instruction.getInstruction();

                // Check Instantiations
                if (insn.getOpcode() == Opcodes.NEW && insn instanceof TypeInsnNode) {
                    TypeInsnNode typeInsn = (TypeInsnNode) insn;
                    if (highLevelTypes.contains(typeInsn.desc)) {
                        violations.add(new Violation(
                                "Hollywood Principle violation: Low-level class instantiates its high-level dependency '"
                                        + typeInsn.desc + "'.",
                                cls.getClassName() + "." + method.getMethodName(),
                                "WARNING"));
                    }
                }

                // Count Upward Calls
                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;

                    boolean isDirectUpwardCall = highLevelTypes.contains(methodInsn.owner);
                    boolean isSelfCallToHighLevelMethod = methodInsn.owner.equals(currentClassName)
                            && highLevelMethodSignatures.contains(methodInsn.name + methodInsn.desc);

                    if ((isDirectUpwardCall || isSelfCallToHighLevelMethod)
                            && !EXCLUDED_METHODS.contains(methodInsn.name)) {
                        String key = methodInsn.name + methodInsn.desc;
                        if (calledMethods.add(key)) {
                            upwardCallCount++;
                        }
                    }
                }
            }

            if (upwardCallCount >= threshold) {
                violations.add(new Violation(
                        String.format(
                                "Potential Hollywood Principle violation: Method makes %d distinct upward calls (threshold is %d).",
                                upwardCallCount, threshold),
                        cls.getClassName() + "." + method.getMethodName(),
                        "WARNING"));
            }
        }
        return violations;
    }
}
