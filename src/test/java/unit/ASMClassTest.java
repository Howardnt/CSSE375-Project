package unit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import rhit.csse.csse374.linter.data.ASMClass;

public class ASMClassTest {

    private ASMClass makeClass(int accessFlags, String name) {
        ClassNode node = new ClassNode();
        node.name = name;
        node.access = accessFlags;
        return new ASMClass(node);
    }

    @Test
    void regularClassIsNotInterface() {
        assertFalse(makeClass(Opcodes.ACC_PUBLIC, "MyClass").isInterface());
    }

    @Test
    void regularClassIsNotAbstract() {
        assertFalse(makeClass(Opcodes.ACC_PUBLIC, "MyClass").isAbstract());
    }

    @Test
    void interfaceFlagIsDetected() {
        assertTrue(makeClass(Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE, "MyInterface").isInterface());
    }

    @Test
    void abstractFlagIsDetected() {
        assertTrue(makeClass(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, "MyAbstract").isAbstract());
    }

    @Test
    void getClassNameReturnsInternalName() {
        assertEquals("com/example/MyClass", makeClass(Opcodes.ACC_PUBLIC, "com/example/MyClass").getClassName());
    }

    @Test
    void getMethodsIsEmptyForClassWithNoMethods() {
        assertTrue(makeClass(Opcodes.ACC_PUBLIC, "EmptyClass").getMethods().isEmpty());
    }
}
