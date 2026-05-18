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

    @Test
    void innerAndAnonymousClassesDoNotProduceFalsePositives() {
        //GoodOuterClass$GoodInner and GoodOuterClass$1 share PascalCase
        //goodness with their outer. Their .class files must not produce
        //violations — the outer's check is what reports any naming issue.
        List<Violation> violations = result.getViolations();
        long innerClassViolations = violations.stream()
            .filter(v -> v.getLocation().contains("GoodOuterClass$"))
            .count();
        assertEquals(0, innerClassViolations,
            "Inner/anonymous class files must not produce their own violations, got: "
                + violations);
    }

    @Test
    void badOuterClassIsReportedExactlyOnce() {
        //M4 fix: previously a lowercase outer class with N inner classes
        //produced N+1 violations (one for the outer's own file, plus one for
        //each inner-class file). Inner-class files are now skipped, so this
        //should be exactly 1 even if inner classes existed.
        List<Violation> violations = result.getViolations();
        long badClassViolations = violations.stream()
            .filter(v -> v.getLocation().toLowerCase().contains("badpascalcaseexample"))
            .count();
        assertEquals(1, badClassViolations,
            "A bad outer class must be flagged exactly once, got: " + badClassViolations);
    }
}
