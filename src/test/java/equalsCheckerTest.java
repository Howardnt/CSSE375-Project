import rhit.csse.csse374.linter.domain.CheckResult;
import rhit.csse.csse374.linter.domain.ConvertToASM;
import rhit.csse.csse374.linter.domain.Violation;
import rhit.csse.csse374.linter.domain.equalsChecker;

import java.util.Arrays;
import java.util.List;

//Jack Traversa (with Claude assistance in accordance with the requirements document)
public class equalsCheckerTest {

    public static void main(String[] args) {
        String classPath = "target/test-classes/EqualsTestClass.class";

        System.out.println("Running equals checker test...");
        System.out.println("Checking: " + classPath);
        System.out.println("=".repeat(60));

        // Convert the class file to ASM representation
        ConvertToASM converter = new ConvertToASM(classPath);

        // Run the checker
        equalsChecker checker = new equalsChecker();

        CheckResult result = checker.run(converter.toASMProject());

        // Print summary
        System.out.println(result);
        System.out.println("=".repeat(60));

        // Print violations
        if (result.hasViolations()) {
            System.out.println("VIOLATIONS FOUND:");
            for (Violation violation : result.getViolations()) {
                System.out.println("  • " + violation);
            }
        } else {
            System.out.println("\nNo violations found!");
        }

        // Print analysis errors if any
        if (result.hasAnalysisErrors()) {
            System.err.println("\nANALYSIS ERRORS:");
            for (String error : result.getAnalysisErrors()) {
                System.err.println("  • " + error);
            }
        }

        System.out.println("=".repeat(60));

        System.out.println("\nTest complete!");
    }
}