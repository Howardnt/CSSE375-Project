package rhit.csse.csse374.linter.domain;

import rhit.csse.csse374.linter.data.ASMProject;

import java.util.Collections;

// Jack Traversa
public class openClosedPrinciple implements Principle {

    @Override
    public String name() {
        return "Open/Closed Principle";
    }

    @Override
    public CheckResult run(ASMProject project) {
        // Placeholder implementation so the project compiles and the principle slot is wired up.
        // Add real OCP heuristics here.
        return new CheckResult(
                Collections.emptyList(),
                project.getClasses().size(),
                0,
                Collections.emptyList(),
                name()
        );
    }
}

