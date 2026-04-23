package system;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.domain.*;

import java.util.ArrayList;
import java.util.List;

/**
 * End-to-end system tests that exercise the full linter pipeline:
 * ConvertToASM -> ASMProject -> LinterHandler -> LinterResult
 *
 * These cover Q2 (business-facing, automated) of the Agile Testing Quadrants.
 */
public class LinterEndToEndTest {

    private static LinterResult result;

    @BeforeAll
    static void runFullLinterPipeline() {
        ASMProject project = new ConvertToASM("target/test-classes/fixtures").toASMProject();

        List<Cursory> cursories = new ArrayList<>();
        cursories.add(new EqualsChecker());
        cursories.add(new PascalClassName());
        cursories.add(new MethodTooLongPattern());

        List<Principle> principles = new ArrayList<>();
        principles.add(new CohesionAnalyzer());
        principles.add(new OpenClosedPrinciple());

        List<Pattern> patterns = new ArrayList<>();
        patterns.add(new TemplatePattern());
        patterns.add(new DecoratorPattern());

        LinterHandler handler = new LinterHandler(patterns, principles, cursories, project);
        result = handler.runLinterAnalysis();
    }

    @Test
    void linterProducesNonNullResult() {
        assertNotNull(result, "LinterHandler should return a non-null LinterResult");
    }

    @Test
    void linterDetectsAtLeastOneViolation() {
        assertTrue(result.getTotalViolationCount() > 0,
            "Linter should find at least one violation in the fixture classes");
    }

    @Test
    void linterReportsCorrectTotalClasses() {
        assertTrue(result.getTotalClasses() > 0,
            "Linter should report at least one class was analyzed");
    }

    @Test
    void cursoryResultsArePresent() {
        assertNotNull(result.getCursoryResults(),
            "Cursory results should not be null");
        assertFalse(result.getCursoryResults().isEmpty(),
            "Cursory results should contain at least one CheckResult");
    }

    @Test
    void principleResultsArePresent() {
        assertNotNull(result.getPrincipleResults(),
            "Principle results should not be null");
        assertFalse(result.getPrincipleResults().isEmpty(),
            "Principle results should contain at least one CheckResult");
    }

    @Test
    void patternResultsArePresent() {
        assertNotNull(result.getPatternResults(),
            "Pattern results should not be null");
        assertFalse(result.getPatternResults().isEmpty(),
            "Pattern results should contain at least one CheckResult");
    }

    @Test
    void allViolationsHaveMessages() {
        for (CheckResult cr : result.getCursoryResults()) {
            for (Violation v : cr.getViolations()) {
                assertNotNull(v.getMessage(), "Violation message should not be null");
                assertFalse(v.getMessage().isBlank(), "Violation message should not be blank");
            }
        }
        for (CheckResult cr : result.getPrincipleResults()) {
            for (Violation v : cr.getViolations()) {
                assertNotNull(v.getMessage(), "Violation message should not be null");
                assertFalse(v.getMessage().isBlank(), "Violation message should not be blank");
            }
        }
    }

    @Test
    void linterOutputTextFormatsWithoutCrashing() {
        rhit.csse.csse374.linter.presentation.LinterOutputText output =
            new rhit.csse.csse374.linter.presentation.LinterOutputText();
        output.formatResult(result);
        String text = output.toString();
        assertNotNull(text, "Formatted output should not be null");
        assertFalse(text.isBlank(), "Formatted output should not be blank");
    }
}
