package system;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.domain.*;

import java.util.List;

/**
 * System test: verifies that SeverityLevel flows end-to-end from
 * check execution through Violation creation.
 */
public class SeveritySystemTest {

    private static ASMProject project;

    @BeforeAll
    static void loadProject() {
        project = new ConvertToASM("target/test-classes/fixtures").toASMProject();
    }

    @Test
    void cohesionViolationsHaveDefaultWarningSeverity() {
        CohesionAnalyzer analyzer = new CohesionAnalyzer();
        CheckResult result = analyzer.run(project);
        for (Violation v : result.getViolations()) {
            assertEquals(SeverityLevel.WARNING, v.getSeverityLevel(),
                "CohesionAnalyzer violations should default to WARNING severity");
        }
    }

    @Test
    void equalsCheckerViolationsHaveDefaultWarningSeverity() {
        EqualsChecker checker = new EqualsChecker();
        CheckResult result = checker.run(project);
        for (Violation v : result.getViolations()) {
            assertEquals(SeverityLevel.WARNING, v.getSeverityLevel(),
                "EqualsChecker violations should default to WARNING severity");
        }
    }

    @Test
    void allViolationsHaveNonNullSeverity() {
        CohesionAnalyzer analyzer = new CohesionAnalyzer();
        EqualsChecker equalsChecker = new EqualsChecker();
        PascalClassName pascalChecker = new PascalClassName();

        List<CheckResult> results = List.of(
            analyzer.run(project),
            equalsChecker.run(project),
            pascalChecker.run(project)
        );

        for (CheckResult result : results) {
            for (Violation v : result.getViolations()) {
                assertNotNull(v.getSeverityLevel(),
                    "Every violation must have a non-null SeverityLevel");
                assertNotNull(v.getSeverity(),
                    "Every violation must return a non-null severity string");
            }
        }
    }

    @Test
    void severityStringMatchesEnumName() {
        PascalClassName checker = new PascalClassName();
        CheckResult result = checker.run(project);
        for (Violation v : result.getViolations()) {
            assertEquals(v.getSeverityLevel().name(), v.getSeverity(),
                "getSeverity() string should match getSeverityLevel().name()");
        }
    }
}
