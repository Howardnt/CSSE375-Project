package checks;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.domain.CheckResult;
import rhit.csse.csse374.linter.domain.ConvertToASM;
import rhit.csse.csse374.linter.domain.EqualsChecker;

/**
 * Tests for EqualsChecker using EqualsTestClass fixture.
 *
 * The fixture contains:
 *  - BAD: == on String, Integer, List, ArrayList, Boolean, and string literal
 *  - GOOD: == on null, primitive int, primitive boolean
 */
public class EqualsCheckerTest {

    private static CheckResult result;

    @BeforeAll
    static void runCheck() {
        ASMProject project = new ConvertToASM("target/test-classes/fixtures").toASMProject();
        result = new EqualsChecker().run(project);
    }

    @Test
    void checkRunsWithoutAnalysisErrors() {
        assertFalse(result.hasAnalysisErrors(),
            "EqualsChecker should not produce analysis errors: " + result.getAnalysisErrors());
    }

    @Test
    void badComparisonsAreDetected() {
        assertTrue(result.hasViolations(),
            "EqualsChecker should flag == on String, Integer, List, etc.");
    }

    @Test
    void atLeastFiveViolationsFound() {
        // Fixture has: String, Integer, List, ArrayList, Boolean, String literal = 6 bad methods
        assertTrue(result.getViolations().size() >= 5,
            "Expected at least 5 violations, got: " + result.getViolations().size());
    }

    @Test
    void violationMessagesMentionEquals() {
        result.getViolations().forEach(v ->
            assertTrue(v.getMessage().toLowerCase().contains("==") ||
                       v.getMessage().toLowerCase().contains("equals"),
                "Violation message should reference == or equals: " + v.getMessage())
        );
    }
}
