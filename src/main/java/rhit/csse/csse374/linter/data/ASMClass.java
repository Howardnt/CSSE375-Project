package rhit.csse.csse374.linter.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Represents a single class with its methods ready for analysis.
 * Wraps ASM's ClassNode to provide a cleaner interface for linters.
 */
public class ASMClass {
    private final String className;
    private final ClassNode classNode;
    private final List<ASMMethod> methods;

    public ASMClass(ClassNode classNode) {
        this.className = classNode.name;
        this.classNode = classNode;
        this.methods = new ArrayList<>();

        for (MethodNode methodNode : classNode.methods) {
            this.methods.add(new ASMMethod(className, methodNode));
        }
    }

    public String getClassName() {
        return className;
    }

    public List<ASMMethod> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    //Utility method to check if class is an interface
    public boolean isInterface() {
        return (classNode.access & org.objectweb.asm.Opcodes.ACC_INTERFACE) != 0;
    }


    public boolean isAbstract() {
        return (classNode.access & org.objectweb.asm.Opcodes.ACC_ABSTRACT) != 0;
    }


    // Keep access to raw ClassNode for advanced use cases
    public ClassNode getClassNode() {
        return classNode;
    }
}