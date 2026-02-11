package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMMethod;
import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.data.LinterOutputText;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Cursory check: method too long / too many parameters.
 *
 * - Too many parameters: > 5
 * - Too long: > 40 distinct source lines (via LineNumberNode)
 *   Fallback if no line numbers: > 200 real bytecode instructions
 */
public class cursory2 implements Cursory {

    private static final int MAX_PARAMETERS = 5;
    private static final int MAX_SOURCE_LINES = 40;
    private static final int MAX_BYTECODE_INSTRUCTIONS_FALLBACK = 200;

    @Override
    public String name() {
        return "MethodTooLong / TooManyParameters";
    }

    @Override
    public void run(ASMProject project, LinterOutputText report) {
        List<ASMClass> classes = project.getClasses();
        for (ASMClass asmClass : classes) {
            for (ASMMethod method : asmClass.getMethods()) {
                analyzeMethod(method, report);
            }
        }
    }

    private void analyzeMethod(ASMMethod method, LinterOutputText report) {
        MethodNode methodNode = method.getMethodNode();
        if (methodNode == null) {
            return;
        }

        if ((methodNode.access & (Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE)) != 0) {
            return;
        }

        int paramCount = org.objectweb.asm.Type.getArgumentTypes(methodNode.desc).length;

        int lineCount = estimateLineCount(methodNode);
        int realInsnCount = (lineCount > 0) ? -1 : countRealInstructions(methodNode);

        boolean tooManyParams = paramCount > MAX_PARAMETERS;
        boolean tooLongByLines = lineCount > MAX_SOURCE_LINES;
        boolean tooLongByBytecode = (lineCount == 0) && realInsnCount > MAX_BYTECODE_INSTRUCTIONS_FALLBACK;

        if (!tooManyParams && !tooLongByLines && !tooLongByBytecode) {
            return;
        }

        String qualifiedName = method.getClassName() + "." + method.getMethodName();
        if (tooManyParams) {
            report.addLine("CURSORY: TooManyParameters in " + qualifiedName
                    + " (params=" + paramCount + ", max=" + MAX_PARAMETERS + ")");
        }
        if (tooLongByLines) {
            report.addLine("CURSORY: MethodTooLong (by lines) in " + qualifiedName
                    + " (lines=" + lineCount + ", max=" + MAX_SOURCE_LINES + ")");
        } else if (tooLongByBytecode) {
            report.addLine("CURSORY: MethodTooLong (by bytecode) in " + qualifiedName
                    + " (instructions=" + realInsnCount + ", max=" + MAX_BYTECODE_INSTRUCTIONS_FALLBACK + ")");
        }
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

