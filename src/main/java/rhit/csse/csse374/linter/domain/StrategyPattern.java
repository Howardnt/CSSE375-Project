package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMProject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Strategy check (smell detector): flags methods that look like type/mode-based behavior selection
 * (big switch / big if-else chains), and suggests considering the Strategy Pattern.
 */
public class StrategyPattern extends Pattern {

    private static final int MIN_SWITCHES_FOR_HOTSPOT = 1;
    private static final int MIN_BRANCHES_FOR_HOTSPOT = 12;
    private static final int MIN_INSTANCEOF_FOR_HOTSPOT = 2;
    private static final int MIN_DISTINCT_CALL_OWNERS_SWITCH = 4;
    private static final int MIN_NEW_TYPES_SWITCH = 3;
    private static final int MIN_DISTINCT_CALL_OWNERS_BRANCH = 5;
    private static final int MIN_REAL_INSTRUCTIONS = 20;

    @Override
    public String name() {
        return "Strategy";
    }

    @Override
    public CheckResult runPatternCheck(ASMProject project) {
        List<Violation> violations = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        int totalClasses = project.getClasses().size();
        int totalMethods = 0;

        List<ASMClass> classes = project.getClasses();
        for (ASMClass asmClass : classes) {
            totalMethods += asmClass.getMethods().size();

            ClassNode classNode = asmClass.getClassNode();
            @SuppressWarnings("unchecked")
            List<MethodNode> methods = (List<MethodNode>) classNode.methods;

            for (MethodNode methodNode : methods) {
                try {
                    Violation v = analyzeMethod(classNode, methodNode);
                    if (v != null) {
                        violations.add(v);
                    }
                } catch (Exception e) {
                    errors.add("Error analyzing " + classNode.name + "." + methodNode.name + ": " + e.getMessage());
                }
            }
        }

        return new CheckResult(violations, totalClasses, totalMethods, errors, "Strategy Pattern");
    }

    @Override
    boolean isPattern(ASMClass cls) {
        // This detector uses the detailed analysis path (runPatternCheck), not the simple per-class predicate.
        return false;
    }

    private Violation analyzeMethod(ClassNode classNode, MethodNode methodNode) {
        if ((methodNode.access & (Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE)) != 0) {
            return null;
        }

        Metrics m = collectMetrics(methodNode);
        if (m.realInsnCount < MIN_REAL_INSTRUCTIONS) {
            return null;
        }

        boolean switchTriggered = m.switchCount >= MIN_SWITCHES_FOR_HOTSPOT
                && (m.distinctCallOwners.size() >= MIN_DISTINCT_CALL_OWNERS_SWITCH
                || m.newTypes.size() >= MIN_NEW_TYPES_SWITCH);

        boolean branchTriggered = m.switchCount == 0
                && m.branchCount >= MIN_BRANCHES_FOR_HOTSPOT
                && (m.instanceofCount >= MIN_INSTANCEOF_FOR_HOTSPOT
                || m.distinctCallOwners.size() >= MIN_DISTINCT_CALL_OWNERS_BRANCH);

        if (!switchTriggered && !branchTriggered) {
            return null;
        }

        String qualifiedName = classNode.name + "." + methodNode.name;
        String reason = switchTriggered
                ? "large switch-based behavior selection"
                : "large if/else chain with type-based behavior";

        String msg = "Strategy-missing hotspot in " + qualifiedName + " (" + reason + "). "
                + "metrics: switches=" + m.switchCount
                + ", branches=" + m.branchCount
                + ", instanceof=" + m.instanceofCount
                + ", distinctCallOwners=" + m.distinctCallOwners.size()
                + ", newTypes=" + m.newTypes.size()
                + ". suggestion: consider extracting behaviors into a Strategy interface and concrete strategy classes.";

        return new Violation(msg, qualifiedName, "INFO");
    }

    private Metrics collectMetrics(MethodNode methodNode) {
        Metrics m = new Metrics();
        InsnList insns = methodNode.instructions;
        for (int i = 0; i < insns.size(); i++) {
            AbstractInsnNode insn = insns.get(i);
            int opcode = insn.getOpcode();
            if (opcode >= 0) {
                m.realInsnCount++;
            }

            if (opcode == Opcodes.TABLESWITCH || opcode == Opcodes.LOOKUPSWITCH) {
                m.switchCount++;
            } else if (insn instanceof JumpInsnNode) {
                if (isIfOpcode(opcode)) {
                    m.branchCount++;
                }
            } else if (opcode == Opcodes.INSTANCEOF) {
                m.instanceofCount++;
            } else if (insn instanceof MethodInsnNode) {
                m.distinctCallOwners.add(((MethodInsnNode) insn).owner);
            } else if (insn instanceof TypeInsnNode && opcode == Opcodes.NEW) {
                m.newTypes.add(((TypeInsnNode) insn).desc);
            }
        }
        return m;
    }

    private boolean isIfOpcode(int opcode) {
        return opcode == Opcodes.IFEQ
                || opcode == Opcodes.IFNE
                || opcode == Opcodes.IFLT
                || opcode == Opcodes.IFGE
                || opcode == Opcodes.IFGT
                || opcode == Opcodes.IFLE
                || opcode == Opcodes.IF_ICMPEQ
                || opcode == Opcodes.IF_ICMPNE
                || opcode == Opcodes.IF_ICMPLT
                || opcode == Opcodes.IF_ICMPGE
                || opcode == Opcodes.IF_ICMPGT
                || opcode == Opcodes.IF_ICMPLE
                || opcode == Opcodes.IF_ACMPEQ
                || opcode == Opcodes.IF_ACMPNE
                || opcode == Opcodes.IFNULL
                || opcode == Opcodes.IFNONNULL;
    }

    private static class Metrics {
        int switchCount = 0;
        int branchCount = 0;
        int instanceofCount = 0;
        int realInsnCount = 0;
        Set<String> distinctCallOwners = new HashSet<>();
        Set<String> newTypes = new HashSet<>();
    }
}

