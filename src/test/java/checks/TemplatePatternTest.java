package checks;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.domain.CheckResult;
import rhit.csse.csse374.linter.domain.ConvertToASM;
import rhit.csse.csse374.linter.domain.TemplatePattern;

/**
 * Tests for TemplatePattern using TemplatePatternFixture.
 *
 * The fixture contains:
 *  - PASS: TemplatePassAbstractBase (abstract hook exists, but no concrete method calls it)
 *  - FAIL: TemplateFailAbstractBase (templateMethod() calls abstract step())
 */
public class TemplatePatternTest {

    private static CheckResult result;

    @BeforeAll
    static void runCheck() {
        ASMProject project = new ConvertToASM("target/test-classes/fixtures").toASMProject();
        result = new TemplatePattern().run(project);
    }

    @Test
    void checkRunsWithoutAnalysisErrors() {
        assertFalse(result.hasAnalysisErrors(),
            "TemplatePattern should not produce analysis errors: " + result.getAnalysisErrors());
    }

    @Test
    void templateMethodPatternIsDetected() {
        assertTrue(result.hasViolations(),
            "TemplatePattern should detect TemplateFailAbstractBase");
    }
}
