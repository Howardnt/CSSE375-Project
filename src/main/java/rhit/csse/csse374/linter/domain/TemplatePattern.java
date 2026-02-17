package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import rhit.csse.csse374.linter.data.ASMClass;

// Refactored to match DecoratorPattern structure
public class TemplatePattern extends Pattern {

    public TemplatePattern() {
        super("Template Method");
    }

    @Override
    public String name() {
        return "Template Method";
    }

    @Override
    public boolean isPattern(ASMClass cls) {
        ClassNode classNode = cls.getClassNode();

        // Only check abstract classes for template method pattern
        if ((classNode.access & Opcodes.ACC_ABSTRACT) == 0) {
            return false;
        }

        // Check each non-abstract method in the abstract class
        for (MethodNode method : classNode.methods) {
            // Skip abstract methods and constructors
            if ((method.access & Opcodes.ACC_ABSTRACT) != 0) {
                continue;
            }
            if (method.name.startsWith("<")) {
                continue;
            }

            // Check if this method calls any abstract methods in the same class
            if (isTemplateMethod(classNode, method)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if a method is a template method by determining if it calls
     * any abstract methods defined in the same class.
     */
    private boolean isTemplateMethod(ClassNode classNode, MethodNode method) {
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode) {
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                // Check if the method call is to the same class
                if (methodInsn.owner.equals(classNode.name)) {
                    // Check if the called method is abstract
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
