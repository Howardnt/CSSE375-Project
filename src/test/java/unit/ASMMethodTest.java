package unit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMMethod;

public class ASMMethodTest {

    private ASMMethod makeMethod(String className, String methodName) {
        MethodNode mn = new MethodNode(Opcodes.ACC_PUBLIC, methodName, "()V", null, null);
        mn.instructions.add(new InsnNode(Opcodes.RETURN));
        mn.maxLocals = 1;
        mn.maxStack = 0;
        return new ASMMethod(className, mn);
    }

    @Test
    void basicAnalysisSucceedsOnSimpleMethod() {
        ASMMethod m = makeMethod("TestClass", "doSomething");
        assertTrue(m.isAnalysisSuccessful());
    }

    @Test
    void getInstructionsReturnsNonNullList() {
        ASMMethod m = makeMethod("TestClass", "doSomething");
        assertNotNull(m.getInstructions());
    }

    @Test
    void getInstructionsWithTypeInfoReturnsNonNullList() {
        ASMMethod m = makeMethod("TestClass", "doSomething");
        assertNotNull(m.getInstructionsWithTypeInfo());
    }

    @Test
    void methodNameIsCorrect() {
        ASMMethod m = makeMethod("TestClass", "myMethod");
        assertEquals("myMethod", m.getMethodName());
    }

    @Test
    void fullMethodNameCombinesClassAndMethod() {
        ASMMethod m = makeMethod("com/example/Foo", "bar");
        assertEquals("com/example/Foo.bar", m.getFullMethodName());
    }

    @Test
    void classWithMethodsProducesASMMethodsViaASMClass() {
        ClassNode cn = new ClassNode();
        cn.name = "TestClass";
        cn.access = Opcodes.ACC_PUBLIC;
        MethodNode mn = new MethodNode(Opcodes.ACC_PUBLIC, "testMethod", "()V", null, null);
        cn.methods.add(mn);

        ASMClass cls = new ASMClass(cn);
        assertEquals(1, cls.getMethods().size());
        assertEquals("testMethod", cls.getMethods().get(0).getMethodName());
    }
}
