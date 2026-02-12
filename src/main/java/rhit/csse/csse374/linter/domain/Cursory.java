package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.tree.ClassNode;

import rhit.csse.csse374.linter.data.ASMProject;

import java.util.List;

/**
 * Domain-layer interface for a "cursory" check.
 *
 * A cursory check is intended to be lightweight (style/naming/obvious issues).
 */
public interface Cursory {

    /**
     * Analyzes a ClassNode and returns a list of violations found.
     *
     * @param classNode the ASM ClassNode to analyze
     * @return a list of violation messages (empty if the class passes the check)
     */
    CheckResult runCursoryCheck(ASMProject project);
}

