package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
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
 * A specialized strategy that ignores all upward method calls.
 * It ONLY flags a violation if a concrete subclass explicitly instantiates its
 * own
 * high-level dependency (superclass/interface) using the 'new' keyword.
 */
public class InstantiationOnlyStrategy implements HollywoodStrategy {

    private static final Set<String> EXCLUDED_METHODS = new HashSet<>(Arrays.asList(
            "<init>", "<clinit>", "equals", "hashCode", "toString"));

    @Override
    public List<Violation> analyzeCoupling(ASMClass cls, Set<String> highLevelTypes,
            Set<String> highLevelMethodSignatures) {
        List<Violation> violations = new ArrayList<>();

        for (ASMMethod method : cls.getMethods()) {
            if (EXCLUDED_METHODS.contains(method.getMethodName())) {
                continue;
            }

            for (Instruction instruction : method.getInstructions()) {
                AbstractInsnNode insn = instruction.getInstruction();

                // Check Instantiations Only
                if (insn.getOpcode() == Opcodes.NEW && insn instanceof TypeInsnNode) {
                    TypeInsnNode typeInsn = (TypeInsnNode) insn;
                    if (highLevelTypes.contains(typeInsn.desc)) {
                        violations.add(new Violation(
                                "Instantiation Hollywood Violation: Low-level class instantiates " +
                                        "its high-level dependency '" + typeInsn.desc + "'.",
                                cls.getClassName() + "." + method.getMethodName(),
                                "WARNING"));
                    }
                }
            }
        }
        return violations;
    }
}
