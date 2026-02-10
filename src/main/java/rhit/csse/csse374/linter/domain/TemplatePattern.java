package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Skeleton implementation of {@link Pattern} for detecting Template Method usage/misuse.
 */
public class TemplatePattern implements Pattern {

    @Override
    public List<String> check(ClassNode classNode) {
        List<String> findings = new ArrayList<>();

        // Template pattern usually involves an abstract class
        if ((classNode.access & Opcodes.ACC_ABSTRACT) == 0) {
            return findings;
        }

        for (MethodNode method : classNode.methods) {
            // The template method itself is usually concrete (not abstract)
            if ((method.access & Opcodes.ACC_ABSTRACT) != 0) {
                continue;
            }
            // Skip constructors and static initializers
            if (method.name.startsWith("<")) {
                continue;
            }

            if (isTemplateMethod(classNode, method)) {
                findings.add("Template Method Pattern detected in " + classNode.name + "." + method.name);
            }
        }
        return findings;
    }

    private boolean isTemplateMethod(ClassNode classNode, MethodNode method) {
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode) {
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                // Check if the method calls another method in the same class
                if (methodInsn.owner.equals(classNode.name)) {
                    // Check if the called method is abstract (the hook/operation)
                    for (MethodNode m : classNode.methods) {
                        if (m.name.equals(methodInsn.name) && m.desc.equals(methodInsn.desc) && (m.access & Opcodes.ACC_ABSTRACT) != 0) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
