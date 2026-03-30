package checks;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.domain.CheckResult;
import rhit.csse.csse374.linter.domain.ConvertToASM;
import rhit.csse.csse374.linter.domain.MethodTooLongPattern;

/**
 * Tests for MethodTooLongPattern using MethodTooLongPatternFixture.
 *
 * The fixture contains:
 *  - PASS: passShortAndFewParams (1 param, short body)
 *  - FAIL: failTooManyParams (6 params, exceeds limit of 5)
 *  - FAIL: failVeryLongMethod (45+ lines, exceeds limit of 40)
 */
public class MethodTooLongTest {

    private static CheckResult result;

    @BeforeAll
    static void runCheck() {
        ASMProject project = new ConvertToASM("target/test-classes/fixtures").toASMProject();
        result = new MethodTooLongPattern().run(project);
    }

    @Test
    void checkRunsWithoutAnalysisErrors() {
        assertFalse(result.hasAnalysisErrors(),
            "MethodTooLongPattern should not produce analysis errors: " + result.getAnalysisErrors());
    }

    @Test
    void tooManyParamsIsDetected() {
        assertTrue(result.hasViolations(),
            "MethodTooLongPattern should flag failTooManyParams and/or failVeryLongMethod");
    }

    @Test
    void atLeastTwoViolationsFound() {
        assertTrue(result.getViolations().size() >= 2,
            "Expected at least 2 violations (too many params + too long), got: "
            + result.getViolations().size());
    }
}
