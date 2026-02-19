package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMMethod;
import rhit.csse.csse374.linter.data.Instruction;

import java.util.*;

public class CohesionAnalyzer extends Principle {
    
    @Override
    public String name() {
        return "CohesionAnalyzer";
    }

    @Override
    public List<Violation> checkClass(ASMClass cls) {
        List<Violation> violations = new ArrayList<>();

        if ((cls.getClassNode().access & org.objectweb.asm.Opcodes.ACC_INTERFACE) != 0 ||
            (cls.getClassNode().access & org.objectweb.asm.Opcodes.ACC_ABSTRACT) != 0) {
            return violations;
        }

        int lcom4Score = calculateLCOM4(cls);

        if (lcom4Score > 1) {
            violations.add(new Violation(
                    cls.getClassName(),
                    "Class lacks cohesion (LCOM4 Score: " + lcom4Score + "). It contains disconnected method groups. Consider splitting this class to adhere to the Single Responsibility Principle."
            ));
        }

        return violations;
    }

    private int calculateLCOM4(ASMClass asmClass) {
        String ownerClass = asmClass.getClassName();
        Map<String, Set<String>> methodToFields = new HashMap<>();
        Map<String, Set<String>> methodToMethods = new HashMap<>();
        List<String> validMethods = new ArrayList<>();

        for (ASMMethod method : asmClass.getMethods()) {
            String methodName = method.getMethodName();

            if (methodName.equals("<init>") || methodName.equals("<clinit>")) {
                continue;
            }

            validMethods.add(methodName);
            methodToFields.putIfAbsent(methodName, new HashSet<>());
            methodToMethods.putIfAbsent(methodName, new HashSet<>());

            for (Instruction instruction : method.getInstructions()) {
                AbstractInsnNode insnNode = instruction.getInstruction();

                if (insnNode instanceof FieldInsnNode) {
                    FieldInsnNode fieldNode = (FieldInsnNode) insnNode;
                    if (fieldNode.owner.equals(ownerClass)) {
                        methodToFields.get(methodName).add(fieldNode.name);
                    }
                }

                if (insnNode instanceof MethodInsnNode) {
                    MethodInsnNode methodCallNode = (MethodInsnNode) insnNode;
                    if (methodCallNode.owner.equals(ownerClass)) {
                        methodToMethods.get(methodName).add(methodCallNode.name);
                    }
                }
            }
        }

        if (validMethods.isEmpty()) return 1;

        Map<String, Set<String>> graph = new HashMap<>();
        for (String m : validMethods) graph.put(m, new HashSet<>());

        for (int i = 0; i < validMethods.size(); i++) {
            for (int j = i + 1; j < validMethods.size(); j++) {
                String m1 = validMethods.get(i);
                String m2 = validMethods.get(j);

                boolean edgeExists = methodToMethods.get(m1).contains(m2) || methodToMethods.get(m2).contains(m1);

                if (!edgeExists) {
                    Set<String> sharedFields = new HashSet<>(methodToFields.get(m1));
                    sharedFields.retainAll(methodToFields.get(m2));
                    if (!sharedFields.isEmpty()) {
                        edgeExists = true;
                    }
                }

                if (edgeExists) {
                    graph.get(m1).add(m2);
                    graph.get(m2).add(m1);
                }
            }
        }

        int components = 0;
        Set<String> visited = new HashSet<>();

        for (String method : validMethods) {
            if (!visited.contains(method)) {
                components++;
                Queue<String> queue = new LinkedList<>();
                queue.add(method);
                visited.add(method);

                while (!queue.isEmpty()) {
                    String current = queue.poll();
                    for (String neighbor : graph.get(current)) {
                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        return components;
    }
}