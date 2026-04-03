package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.tree.*;
import java.util.*;

public class SrpMetricsCalculator {

    public double computeDisjointRatio(Map<MethodNode, Set<String>> fieldsByMethod) {
        List<MethodNode> methods = new ArrayList<>(fieldsByMethod.keySet());
        int n = methods.size();
        if (n < 2) return 0.0;

        int disjointPairs = 0;
        int overlappingPairs = 0;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Set<String> a = fieldsByMethod.get(methods.get(i));
                Set<String> b = fieldsByMethod.get(methods.get(j));
                if (a.isEmpty() && b.isEmpty()) continue;
                
                Set<String> intersection = new HashSet<>(a);
                intersection.retainAll(b);
                if (intersection.isEmpty()) disjointPairs++;
                else overlappingPairs++;
            }
        }
        return (disjointPairs + overlappingPairs == 0) ? 0.0 : (double) disjointPairs / (disjointPairs + overlappingPairs);
    }

    public int computeDependencyFanOut(ClassNode classNode, List<MethodNode> methods) {
        Set<String> packages = new HashSet<>();
        for (MethodNode method : methods) {
            for (int i = 0; i < method.instructions.size(); i++) {
                AbstractInsnNode insn = method.instructions.get(i);
                if (insn instanceof MethodInsnNode call && !classNode.name.equals(call.owner)) {
                    String owner = call.owner;
                    int lastSlash = owner.lastIndexOf('/');
                    packages.add(lastSlash > 0 ? owner.substring(0, lastSlash) : owner);
                }
            }
        }
        return packages.size();
    }
}