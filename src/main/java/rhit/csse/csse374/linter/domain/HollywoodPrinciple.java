package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMMethod;
import rhit.csse.csse374.linter.data.Instruction;

import java.util.*;

/**
 * Hollywood Principle ("Don't call us, we'll call you") Linter
 *
 * Promotes loose coupling by ensuring high-level components (abstract classes,
 * interfaces) control the execution flow and invoke low-level components
 * (concrete subclasses), rather than the other way around.
 *
 * Detects:
 * 1. Concrete classes that excessively call back up into their own
 * abstract superclass or implemented interfaces.
 * 2. Concrete classes that instantiate their own high-level dependencies
 * (superclass or interfaces).
 */
public class HollywoodPrinciple extends Principle {

    // A class making 3+ upward calls is likely pulling control from the high-level
    // component
    private static final int UPWARD_CALL_THRESHOLD = 3;

    private static final Set<String> EXCLUDED_METHODS = new HashSet<>(Arrays.asList(
            "<init>", "<clinit>", "equals", "hashCode", "toString"));

    public HollywoodPrinciple() {
        super("Hollywood Principle");
    }

    @Override
    public List<Violation> checkClass(ASMClass cls) {
        List<Violation> violations = new ArrayList<>();

        // Only analyze concrete classes that have a non-trivial hierarchy
        int access = cls.getClassNode().access;
        boolean isAbstract = (access & Opcodes.ACC_ABSTRACT) != 0;
        boolean isInterface = (access & Opcodes.ACC_INTERFACE) != 0;

        if (isAbstract || isInterface) {
            // High-level components are not the concern here
            return violations;
        }

        Set<String> highLevelTypes = collectHighLevelTypes(cls);
        if (highLevelTypes.isEmpty()) {
            // No superclass/interface hierarchy to violate
            return violations;
        }

        checkUpwardCalls(cls, highLevelTypes, violations);
        checkUpwardInstantiation(cls, highLevelTypes, violations);

        return violations;
    }

    /**
     * Collects the class's superclass (if it isn't java/lang/Object) and all
     * implemented interfaces as the set of "high-level" types.
     */
    private Set<String> collectHighLevelTypes(ASMClass cls) {
        Set<String> types = new HashSet<>();

        String superName = cls.getClassNode().superName;
        if (superName != null && !superName.equals("java/lang/Object")) {
            types.add(superName);
        }

        List<String> interfaces = cls.getClassNode().interfaces;
        if (interfaces != null) {
            types.addAll(interfaces);
        }

        return types;
    }

    /**
     * Detects methods that make too many calls back up into the high-level
     * (abstract superclass / interface) types. This suggests the low-level
     * concrete class is "pulling" control rather than being invoked.
     */
    private void checkUpwardCalls(ASMClass cls, Set<String> highLevelTypes,
            List<Violation> violations) {
        for (ASMMethod method : cls.getMethods()) {
            if (EXCLUDED_METHODS.contains(method.getMethodName())) {
                continue;
            }

            int upwardCallCount = 0;
            Set<String> calledMethods = new HashSet<>();

            for (Instruction instruction : method.getInstructions()) {
                AbstractInsnNode insn = instruction.getInstruction();
                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    if (highLevelTypes.contains(methodInsn.owner)
                            && !EXCLUDED_METHODS.contains(methodInsn.name)) {
                        String key = methodInsn.owner + "." + methodInsn.name + methodInsn.desc;
                        if (calledMethods.add(key)) {
                            upwardCallCount++;
                        }
                    }
                }
            }

            if (upwardCallCount >= UPWARD_CALL_THRESHOLD) {
                String location = cls.getClassName() + "." + method.getMethodName();
                violations.add(new Violation(
                        String.format(
                                "Potential Hollywood Principle violation: Method makes %d distinct " +
                                        "upward calls to its superclass/interfaces. Low-level components " +
                                        "should not call high-level components; consider inverting control " +
                                        "so the high-level component drives execution.",
                                upwardCallCount),
                        location,
                        "WARNING"));
            }
        }
    }

    /**
     * Detects concrete classes that instantiate their own high-level
     * dependencies (superclass or interfaces), indicating the low-level
     * component is controlling object creation rather than receiving
     * dependencies from above.
     */
    private void checkUpwardInstantiation(ASMClass cls, Set<String> highLevelTypes,
            List<Violation> violations) {
        for (ASMMethod method : cls.getMethods()) {
            if (EXCLUDED_METHODS.contains(method.getMethodName())) {
                continue;
            }

            for (Instruction instruction : method.getInstructions()) {
                AbstractInsnNode insn = instruction.getInstruction();
                if (insn.getOpcode() == Opcodes.NEW && insn instanceof TypeInsnNode) {
                    TypeInsnNode typeInsn = (TypeInsnNode) insn;
                    if (highLevelTypes.contains(typeInsn.desc)) {
                        String location = cls.getClassName() + "." + method.getMethodName();
                        violations.add(new Violation(
                                "Hollywood Principle violation: Low-level class instantiates " +
                                        "its high-level dependency '" + typeInsn.desc + "'. " +
                                        "Consider using dependency injection or a factory so the " +
                                        "high-level component controls object creation.",
                                location,
                                "WARNING"));
                    }
                }
            }
        }
    }

    @Override
    public String name() {
        return "Hollywood Principle";
    }
}
