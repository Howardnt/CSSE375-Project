package rhit.csse.csse374.linter.domain;

import rhit.csse.csse374.linter.data.ASMProject;

/**
 * Skeleton implementation of {@link Pattern} for detecting Template Method usage/misuse.
 */
public class TemplatePattern extends Pattern {
    @Override
    public CheckResult runPatternCheck(ASMProject project) {
        return new CheckResult(
                java.util.Collections.emptyList(),
                project.getClasses().size(),
                0,
                java.util.Collections.emptyList(),
                "Template Pattern"
        );
    }
}

