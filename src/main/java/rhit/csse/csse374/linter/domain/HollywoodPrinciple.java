package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import rhit.csse.csse374.linter.data.ASMClass;

import java.io.InputStream;
import java.util.*;

/**
 * Hollywood Principle ("Don't call us, we'll call you") Linter
 *
 * Promotes loose coupling by ensuring high-level components (abstract classes,
 * interfaces) control the execution flow and invoke low-level components
 * (concrete subclasses), rather than the other way around.
 *
 * This class now utilizes the Strategy Pattern to determine coupling
 * violations.
 */
public class HollywoodPrinciple extends Principle {

    private final HollywoodStrategy strategy;
    private final Map<String, Set<String>> resolvedMethodCache = new HashMap<>();

    /**
     * Default constructor uses the Threshold Strategy with a threshold of 3
     * to maintain backwards compatibility with older linter behavior.
     */
    public HollywoodPrinciple() {
        this(new ThresholdHollywoodStrategy(3));
    }

    /**
     * Constructor allowing injection of a specific tuning strategy.
     */
    public HollywoodPrinciple(HollywoodStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public List<Violation> checkClass(ASMClass cls) {
        List<Violation> violations = new ArrayList<>();

        // Only analyze concrete classes that have a non-trivial hierarchy
        int access = cls.getClassNode().access;
        boolean isAbstract = (access & Opcodes.ACC_ABSTRACT) != 0;
        boolean isInterface = (access & Opcodes.ACC_INTERFACE) != 0;

        if (isAbstract || isInterface) {
            // High-level components are not the concern here
            return violations;
        }

        Set<String> highLevelTypes = collectHighLevelTypes(cls);
        if (highLevelTypes.isEmpty()) {
            // No superclass/interface hierarchy to violate
            return violations;
        }

        // Resolve method signatures for each high-level type
        Set<String> highLevelMethodSignatures = resolveHighLevelMethods(highLevelTypes);

        // Delegate the actual rule checking to the current strategy
        violations.addAll(this.strategy.analyzeCoupling(cls, highLevelTypes, highLevelMethodSignatures));

        return violations;
    }

    /**
     * Collects the class's superclass (if it isn't java/lang/Object) and all
     * implemented interfaces as the set of "high-level" types.
     */
    private Set<String> collectHighLevelTypes(ASMClass cls) {
        Set<String> types = new HashSet<>();

        String superName = cls.getClassNode().superName;
        if (superName != null && !superName.equals("java/lang/Object")) {
            types.add(superName);
        }

        List<String> interfaces = cls.getClassNode().interfaces;
        if (interfaces != null) {
            types.addAll(interfaces);
        }

        return types;
    }

    /**
     * Resolves the method signatures (name+descriptor) declared in each
     * high-level type by loading their bytecode via the ClassLoader.
     */
    private Set<String> resolveHighLevelMethods(Set<String> highLevelTypes) {
        Set<String> allSignatures = new HashSet<>();

        for (String typeName : highLevelTypes) {
            // Check cache first
            if (resolvedMethodCache.containsKey(typeName)) {
                allSignatures.addAll(resolvedMethodCache.get(typeName));
                continue;
            }

            Set<String> signatures = new HashSet<>();
            try (InputStream is = ClassLoader.getSystemResourceAsStream(typeName + ".class")) {
                if (is != null) {
                    ClassReader reader = new ClassReader(is);
                    ClassNode node = new ClassNode();
                    reader.accept(node, ClassReader.SKIP_CODE);
                    for (MethodNode m : node.methods) {
                        if (!m.name.startsWith("<")) {
                            signatures.add(m.name + m.desc);
                        }
                    }
                }
            } catch (Exception e) {
                // Silently ignore — if we can't load the type, skip resolution
            }

            resolvedMethodCache.put(typeName, signatures);
            allSignatures.addAll(signatures);
        }

        return allSignatures;
    }

    @Override
    public String name() {
        return "Hollywood Principle";
    }
}
