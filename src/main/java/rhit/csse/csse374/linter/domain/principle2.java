package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.data.LinterOutputText;

import java.util.*;

/**
 * Principle check: Single Responsibility Principle (SRP) heuristic detector.
 *
 * SRP is semantic, so this uses indicators:
 * - size (fields / methods)
 * - cohesion (field sharing across methods, LCOM-ish)
 * - dependency fan-out (distinct external packages referenced via method calls)
 */
public class principle2 implements Principle {

    private static final int MIN_PUBLIC_METHODS = 10;
    private static final int MIN_TOTAL_METHODS = 20;
    private static final int MIN_FIELDS = 8;
    private static final double MIN_DISJOINT_RATIO = 0.5;
    private static final int MIN_DEPENDENCY_PACKAGES = 3;

    @Override
    public String name() {
        return "SingleResponsibilityPrinciple";
    }

    @Override
    public void run(ASMProject project, LinterOutputText report) {
        for (ASMClass asmClass : project.getClasses()) {
            analyzeClass(asmClass.getClassNode(), report);
        }
    }

    private void analyzeClass(ClassNode classNode, LinterOutputText report) {
        @SuppressWarnings("unchecked")
        List<FieldNode> fields = (List<FieldNode>) classNode.fields;
        @SuppressWarnings("unchecked")
        List<MethodNode> methods = (List<MethodNode>) classNode.methods;

        int fieldCount = fields.size();

        List<MethodNode> interestingMethods = new ArrayList<>();
        int publicMethodCount = 0;
        for (MethodNode method : methods) {
            if ("<init>".equals(method.name) || "<clinit>".equals(method.name)) {
                continue;
            }
            if ((method.access & (Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE)) != 0) {
                continue;
            }
            interestingMethods.add(method);
            if ((method.access & Opcodes.ACC_PUBLIC) != 0) {
                publicMethodCount++;
            }
        }

        int methodCount = interestingMethods.size();

        if (fieldCount < MIN_FIELDS
                || (publicMethodCount < MIN_PUBLIC_METHODS && methodCount < MIN_TOTAL_METHODS)) {
            return;
        }

        Map<MethodNode, Set<String>> fieldsByMethod = computeFieldsAccessedPerMethod(classNode, interestingMethods);
        double disjointRatio = computeDisjointRatio(fieldsByMethod);
        int dependencyPackages = computeDependencyFanOut(classNode, interestingMethods);

        boolean lowCohesion = disjointRatio >= MIN_DISJOINT_RATIO;
        boolean highFanOut = dependencyPackages >= MIN_DEPENDENCY_PACKAGES;

        if (!lowCohesion && !highFanOut) {
            return;
        }

        report.addLine("PRINCIPLE: Possible SRP violation in " + classNode.name);
        report.addLine("           fields=" + fieldCount
                + ", publicMethods=" + publicMethodCount
                + ", totalMethods=" + methodCount);
        report.addLine("           cohesionDisjointRatio=" + String.format(Locale.ROOT, "%.2f", disjointRatio)
                + ", dependencyPackages=" + dependencyPackages);
        report.addLine("           suggestion: class likely has multiple responsibilities; consider splitting into smaller, cohesive types.");
    }

    private Map<MethodNode, Set<String>> computeFieldsAccessedPerMethod(ClassNode classNode, List<MethodNode> methods) {
        Map<MethodNode, Set<String>> result = new HashMap<>();
        for (MethodNode method : methods) {
            Set<String> accessed = new HashSet<>();
            InsnList insns = method.instructions;
            for (int i = 0; i < insns.size(); i++) {
                AbstractInsnNode insn = insns.get(i);
                if (insn instanceof FieldInsnNode) {
                    FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                    if (classNode.name.equals(fieldInsn.owner)) {
                        accessed.add(fieldInsn.name + ":" + fieldInsn.desc);
                    }
                }
            }
            result.put(method, accessed);
        }
        return result;
    }

    private double computeDisjointRatio(Map<MethodNode, Set<String>> fieldsByMethod) {
        List<MethodNode> methods = new ArrayList<>(fieldsByMethod.keySet());
        int n = methods.size();
        if (n < 2) {
            return 0.0;
        }

        int disjointPairs = 0;
        int overlappingPairs = 0;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Set<String> a = fieldsByMethod.get(methods.get(i));
                Set<String> b = fieldsByMethod.get(methods.get(j));
                if (a.isEmpty() && b.isEmpty()) {
                    continue;
                }
                Set<String> intersection = new HashSet<>(a);
                intersection.retainAll(b);
                if (intersection.isEmpty()) {
                    disjointPairs++;
                } else {
                    overlappingPairs++;
                }
            }
        }

        int totalPairs = disjointPairs + overlappingPairs;
        if (totalPairs == 0) {
            return 0.0;
        }
        return (double) disjointPairs / (double) totalPairs;
    }

    private int computeDependencyFanOut(ClassNode classNode, List<MethodNode> methods) {
        Set<String> packages = new HashSet<>();
        for (MethodNode method : methods) {
            InsnList insns = method.instructions;
            for (int i = 0; i < insns.size(); i++) {
                AbstractInsnNode insn = insns.get(i);
                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) insn;
                    if (classNode.name.equals(call.owner)) {
                        continue;
                    }
                    String owner = call.owner;
                    int lastSlash = owner.lastIndexOf('/');
                    packages.add(lastSlash > 0 ? owner.substring(0, lastSlash) : owner);
                }
            }
        }
        return packages.size();
    }
}

