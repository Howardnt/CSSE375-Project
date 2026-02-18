package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMMethod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Pattern-style check: method too long / too many parameters.
 *
 * - Too many parameters: > 5
 * - Too long: > 40 distinct source lines (via LineNumberNode)
 *   Fallback if no line numbers: > 200 real bytecode instructions
 */
public class MethodTooLongPattern extends Cursory {

    private static final int MAX_PARAMETERS = 5;
    private static final int MAX_SOURCE_LINES = 40;
    private static final int MAX_BYTECODE_INSTRUCTIONS_FALLBACK = 200;

    @Override
    public String name() {
        return "MethodTooLong / TooManyParameters";
    }

    @Override
    public List<Violation> checkClass(ASMClass cls) {
        List<Violation> violations = new ArrayList<>();

        for (ASMMethod method : cls.getMethods()) {
            try {
                violations.addAll(analyzeMethod(method));
            } catch (Exception e) {
                String qualifiedName = method.getClassName() + "." + method.getMethodName();
                violations.add(new Violation("Error analyzing method: " + e.getMessage(), qualifiedName, "ERROR"));
            }
        }

        return violations;
    }

    private List<Violation> analyzeMethod(ASMMethod method) {
        List<Violation> violations = new ArrayList<>();

        MethodNode methodNode = method.getMethodNode();
        if (methodNode == null) {
            return violations;
        }

        if ((methodNode.access & (Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE)) != 0) {
            return violations;
        }

        int paramCount = org.objectweb.asm.Type.getArgumentTypes(methodNode.desc).length;

        int lineCount = estimateLineCount(methodNode);
        int realInsnCount = (lineCount > 0) ? -1 : countRealInstructions(methodNode);

        boolean tooManyParams = paramCount > MAX_PARAMETERS;
        boolean tooLongByLines = lineCount > MAX_SOURCE_LINES;
        boolean tooLongByBytecode = (lineCount == 0) && realInsnCount > MAX_BYTECODE_INSTRUCTIONS_FALLBACK;

        if (!tooManyParams && !tooLongByLines && !tooLongByBytecode) {
            return violations;
        }

        String qualifiedName = method.getClassName() + "." + method.getMethodName();
        if (tooManyParams) {
            violations.add(new Violation(
                    "TooManyParameters (params=" + paramCount + ", max=" + MAX_PARAMETERS + ")",
                    qualifiedName,
                    "WARNING"
            ));
        }
        if (tooLongByLines) {
            violations.add(new Violation(
                    "MethodTooLong (by lines) (lines=" + lineCount + ", max=" + MAX_SOURCE_LINES + ")",
                    qualifiedName,
                    "WARNING"
            ));
        } else if (tooLongByBytecode) {
            violations.add(new Violation(
                    "MethodTooLong (by bytecode) (instructions=" + realInsnCount + ", max=" + MAX_BYTECODE_INSTRUCTIONS_FALLBACK + ")",
                    qualifiedName,
                    "WARNING"
            ));
        }

        return violations;
    }

    private int estimateLineCount(MethodNode methodNode) {
        Set<Integer> lines = new HashSet<>();
        for (int i = 0; i < methodNode.instructions.size(); i++) {
            AbstractInsnNode insn = methodNode.instructions.get(i);
            if (insn instanceof LineNumberNode) {
                lines.add(((LineNumberNode) insn).line);
            }
        }
        return lines.size();
    }

    private int countRealInstructions(MethodNode methodNode) {
        int count = 0;
        for (int i = 0; i < methodNode.instructions.size(); i++) {
            AbstractInsnNode insn = methodNode.instructions.get(i);
            if (insn.getOpcode() >= 0) {
                count++;
            }
        }
        return count;
    }
}

