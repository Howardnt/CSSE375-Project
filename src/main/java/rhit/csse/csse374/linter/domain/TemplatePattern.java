package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMProject;

import java.util.ArrayList;
import java.util.List;

public class TemplatePattern implements Pattern {

    public static class TemplatePatternViolation implements Violation {
        private final String className;
        private final String methodName;

        public TemplatePatternViolation(String className, String methodName) {
            this.className = className;
            this.methodName = methodName;
        }

        @Override
        public String toString() {
            return "Template Method Pattern detected in " + className.replace('/', '.') + "." + methodName;
        }
    }

    @Override
    public CheckResult runPatternCheck(ASMProject project) {
        List<Violation> violations = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int classesChecked = 0;
        int methodsChecked = 0;

        for (ASMClass asmClass : project.getClasses()) {
            ClassNode classNode = asmClass.getClassNode();
            classesChecked++;

            if ((classNode.access & Opcodes.ACC_ABSTRACT) == 0) {
                continue;
            }

            for (MethodNode method : classNode.methods) {
                if ((method.access & Opcodes.ACC_ABSTRACT) != 0) {
                    continue;
                }
                if (method.name.startsWith("<")) {
                    continue;
                }

                methodsChecked++;

                if (isTemplateMethod(classNode, method)) {
                    violations.add(new TemplatePatternViolation(classNode.name, method.name));
                }
            }
        }

        return new CheckResult(violations, classesChecked, methodsChecked, errors, "Template Pattern");
    }

    private boolean isTemplateMethod(ClassNode classNode, MethodNode method) {
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode) {
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                if (methodInsn.owner.equals(classNode.name)) {
                    for (MethodNode m : classNode.methods) {
                        if (m.name.equals(methodInsn.name) && m.desc.equals(methodInsn.desc)
                                && (m.access & Opcodes.ACC_ABSTRACT) != 0) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
