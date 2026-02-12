package rhit.csse.csse374.linter.domain;

import rhit.csse.csse374.linter.data.ASMProject;

/**
 * Domain-layer interface for a design pattern detector.
 */
public interface Pattern {
    public CheckResult runPatternCheck(ASMProject project);
}

