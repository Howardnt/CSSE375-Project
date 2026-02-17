package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.tree.MethodNode;
import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMMethod;
import rhit.csse.csse374.linter.data.ASMProject;

import java.util.ArrayList;
import java.util.List;

/**
 * Cursory check: ensure method names are lowerCamelCase.
 *
 * Rules (heuristic):
 * - Ignore constructors / static initializers (<init>, <clinit>)
 * - Ignore synthetic/bridge methods
 * - Flag names containing underscores or starting with a non-lowercase letter
 * - Allow only letters/digits after the first character
 */
public class CamelCaseChecker extends Cursory {

    @Override
    public String name() {
        return "CamelCaseMethodNames";
    }

    private Violation checkMethodName(ASMMethod method) {
        MethodNode methodNode = method.getMethodNode();
        if (methodNode == null) {
            return null;
        }

        String methodName = method.getMethodName();
        if ("<init>".equals(methodName) || "<clinit>".equals(methodName)) {
            return null;
        }

        // Skip compiler-generated helpers
        if ((methodNode.access & (org.objectweb.asm.Opcodes.ACC_SYNTHETIC | org.objectweb.asm.Opcodes.ACC_BRIDGE)) != 0) {
            return null;
        }

        if (isLowerCamelCase(methodName)) {
            return null;
        }

        String location = method.getFullMethodName();
        String msg = "NonCamelCaseMethodName (name=\"" + methodName + "\")";
        return new Violation(msg, location, "WARNING");
    }

    private boolean isLowerCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        if (name.indexOf('_') >= 0) {
            return false;
        }
        char first = name.charAt(0);
        if (!Character.isLowerCase(first)) {
            return false;
        }
        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

    @Override
public List<Violation> checkClass(ASMClass cls) {
    List<Violation> violations = new ArrayList<>();
    for (ASMMethod method : cls.getMethods()) {
        Violation v = checkMethodName(method);
        if (v != null) {
            violations.add(v);
        }
    }
    return violations;
}
}

