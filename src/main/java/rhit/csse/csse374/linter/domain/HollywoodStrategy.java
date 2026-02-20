package rhit.csse.csse374.linter.domain;

import rhit.csse.csse374.linter.data.ASMClass;
import java.util.List;
import java.util.Set;

/**
 * Strategy interface for configuring how strictly the Hollywood Principle is
 * enforced.
 */
public interface HollywoodStrategy {

    /**
     * Analyzes a class to determine if it violates the Hollywood Principle
     * based on the specific strategy's coupling rules.
     *
     * @param cls                       The concrete class being analyzed.
     * @param highLevelTypes            The set of known high-level types
     *                                  (interfaces/superclasses) it
     *                                  implements/extends.
     * @param highLevelMethodSignatures The signatures of methods declared in those
     *                                  high-level types.
     * @return A list of violations found, or an empty list if none.
     */
    List<Violation> analyzeCoupling(ASMClass cls, Set<String> highLevelTypes, Set<String> highLevelMethodSignatures);
}
