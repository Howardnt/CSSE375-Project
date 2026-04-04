package rhit.csse.csse374.linter.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMMethod;
import rhit.csse.csse374.linter.data.Instruction;

public class CohesionAnalyzer extends Principle {
    
    @Override
    public String name() {
        return "CohesionAnalyzer";
    }

    @Override
    public List<Violation> checkClass(ASMClass cls) {
        List<Violation> violations = new ArrayList<>();

        /**CODE REFACTORED: Ervin 3/29/2026 - Extract Method for Feature Envy
        Moved interface and abstract class check to its own method and put
        said method in ASMClass.java
        
        Skip interfaces and abstract classes as they are not expected to have
        high cohesion
        **/
        if (cls.isInterface() || cls.isAbstract()) {
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

    /**CODE REFACTORED: Ervin 4/3/2026 - Extract Method for Long Method
    Split calculateLCOM4() into three extracted methods following Fowler's
    Extract Function pattern: extractMethodFieldUsage, buildConnectivityGraph,
    countConnectedComponents.
    **/
    private int calculateLCOM4(ASMClass asmClass) {
        List<String> validMethods = new ArrayList<>();
        Map<String, Set<String>> methodToFields = new HashMap<>();
        Map<String, Set<String>> methodToMethods = new HashMap<>();

        extractMethodFieldUsage(asmClass, validMethods, methodToFields, methodToMethods);

        if (validMethods.isEmpty()) return 1;

        Map<String, Set<String>> graph = buildConnectivityGraph(validMethods, methodToFields, methodToMethods);

        return countConnectedComponents(validMethods, graph);
    }

    private void extractMethodFieldUsage(ASMClass asmClass, List<String> validMethods,
            Map<String, Set<String>> methodToFields, Map<String, Set<String>> methodToMethods) {
        String ownerClass = asmClass.getClassName();

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
    }

    private Map<String, Set<String>> buildConnectivityGraph(List<String> validMethods,
            Map<String, Set<String>> methodToFields, Map<String, Set<String>> methodToMethods) {
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

        return graph;
    }

    private int countConnectedComponents(List<String> validMethods, Map<String, Set<String>> graph) {
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