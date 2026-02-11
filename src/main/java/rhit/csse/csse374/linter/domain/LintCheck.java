package rhit.csse.csse374.linter.domain;

import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.data.LinterOutputText;

/**
 * Common contract for all linter checks (cursory, principle, and pattern).
 *
 * This interface provides safe defaults so placeholder/skeleton checks compile:
 * - {@link #name()} defaults to the simple class name
 * - {@link #run(ASMProject, LinterOutputText)} defaults to a no-op
 */
public interface LintCheck {

    default String name() {
        return getClass().getSimpleName();
    }

    default void run(ASMProject project, LinterOutputText report) {
        // default no-op for skeleton checks
    }
}

