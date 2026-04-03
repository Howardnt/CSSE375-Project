package checks;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.domain.CheckResult;
import rhit.csse.csse374.linter.domain.ConvertToASM;
import rhit.csse.csse374.linter.domain.DecoratorPattern;

/**
 * Tests for DecoratorPattern using DecoratorPatternFixture.
 *
 * The fixture contains:
 *  - PASS: DecoratorPassExample (implements interface but holds no component-typed field)
 *  - FAIL: DecoratorFailExample (implements interface AND holds a field of that same interface type)
 */
public class DecoratorPatternTest {

    private static CheckResult result;

    @BeforeAll
    static void runCheck() {
        ASMProject project = new ConvertToASM("target/test-classes/fixtures").toASMProject();
        result = new DecoratorPattern().run(project);
    }

    @Test
    void checkRunsWithoutAnalysisErrors() {
        assertFalse(result.hasAnalysisErrors(),
            "DecoratorPattern should not produce analysis errors: " + result.getAnalysisErrors());
    }

    @Test
    void decoratorIsDetected() {
        assertTrue(result.hasViolations(),
            "DecoratorPattern should detect DecoratorFailExample");
    }
}
