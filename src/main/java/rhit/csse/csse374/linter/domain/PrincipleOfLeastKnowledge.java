package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMMethod;
import rhit.csse.csse374.linter.data.Instruction;

import java.util.ArrayList;
import java.util.List;

public class PrincipleOfLeastKnowledge extends Principle {
    
     @Override
    public String name() {
        return "PrincipleOfLeastKnowledge";
    }

    @Override
    public List<Violation> checkClass(ASMClass cls) {
        List<Violation> violations = new ArrayList<>();
        String currentClassName = cls.getClassName();

        for (ASMMethod method : cls.getMethods()) {
            List<Instruction> instructions = method.getInstructions();
            
            for (int i = 0; i < instructions.size() - 1; i++) {
                AbstractInsnNode insn1 = instructions.get(i).getInstruction();
                
                if (insn1 instanceof MethodInsnNode) {
                    MethodInsnNode call1 = (MethodInsnNode) insn1;
                    
                    Type returnType1 = Type.getReturnType(call1.desc);
                    
                    if (returnType1.getSort() == Type.OBJECT && 
                        !returnType1.getInternalName().equals(currentClassName) &&
                        !returnType1.getInternalName().startsWith("java/lang/")) {
                        
                        AbstractInsnNode nextInsn = instructions.get(i + 1).getInstruction();
                        
                        if (nextInsn instanceof MethodInsnNode) {
                            MethodInsnNode call2 = (MethodInsnNode) nextInsn;
                            
                            if (call2.owner.equals(returnType1.getInternalName())) {
                                violations.add(new Violation(
                                        currentClassName,
                                        "Law of Demeter violation in method " + method.getMethodName() + 
                                        ". The method calls " + call1.name + "() and immediately invokes " + 
                                        call2.name + "() on the returned object."
                                ));
                            }
                        }
                    }
                }
            }
        }

        return violations;
    }
}
