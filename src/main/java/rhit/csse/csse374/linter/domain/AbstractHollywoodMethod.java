package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.tree.MethodInsnNode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractHollywoodMethod implements HollywoodStrategy {
    
    protected static final Set<String> EXCLUDED_METHODS = new HashSet<>(Arrays.asList(
            "<init>", "<clinit>", "equals", "hashCode", "toString"));

    /**
     * Shared logic to detect if a method instruction constitutes an upward call.
     */
    protected boolean isUpwardCall(MethodInsnNode methodInsn, String currentClassName, 
                                   Set<String> highLevelTypes, Set<String> highLevelMethodSignatures) {
        
        boolean isDirectUpwardCall = highLevelTypes.contains(methodInsn.owner);
        boolean isSelfCallToHighLevelMethod = methodInsn.owner.equals(currentClassName)
                && highLevelMethodSignatures.contains(methodInsn.name + methodInsn.desc);

        return (isDirectUpwardCall || isSelfCallToHighLevelMethod) 
                && !EXCLUDED_METHODS.contains(methodInsn.name);
    }
}