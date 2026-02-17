package rhit.csse.csse374.linter.domain;

import rhit.csse.csse374.linter.data.ASMProject;
import java.util.ArrayList;
import java.util.List;

//Jack Traversa
public class AdapterPattern extends Pattern {
    @Override
    public CheckResult runPatternCheck(ASMProject project) {
        List<Violation> violations = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int totalMethods = 0;
        int totalClasses = project.getClasses().size();

        return new CheckResult(violations, totalClasses, totalMethods, errors, "Adapter Pattern");
    }
}

