package checks;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.domain.CheckResult;
import rhit.csse.csse374.linter.domain.ConvertToASM;
import rhit.csse.csse374.linter.domain.PascalClassName;
import rhit.csse.csse374.linter.domain.Violation;

import java.util.List;

/**
 * Tests for PascalClassName using PascalClassNameFixture.
 *
 * The fixture contains:
 *  - PASS: PascalClassNameFixture, GoodPascalCaseExample
 *  - FAIL: badPascalCaseExample (starts with lowercase)
 */
public class PascalClassNameTest {

    private static CheckResult result;

    @BeforeAll
    static void runCheck() {
        ASMProject project = new ConvertToASM("target/test-classes/fixtures").toASMProject();
        result = new PascalClassName().run(project);
    }

    @Test
    void checkRunsWithoutAnalysisErrors() {
        assertFalse(result.hasAnalysisErrors(),
            "PascalClassName should not produce analysis errors: " + result.getAnalysisErrors());
    }

    @Test
    void badClassNameIsDetected() {
        assertTrue(result.hasViolations(),
            "PascalClassName should flag 'badPascalCaseExample'");
    }

    @Test
    void violationReferencesTheBadClass() {
        List<Violation> violations = result.getViolations();
        boolean foundBadClass = violations.stream()
            .anyMatch(v -> v.getLocation().contains("badPascalCaseExample") ||
                          v.getMessage().toLowerCase().contains("badpascalcaseexample") ||
                          v.getLocation().toLowerCase().contains("badpascal"));
        assertTrue(foundBadClass,
            "Expected a violation for 'badPascalCaseExample', got: " + violations);
    }
}
