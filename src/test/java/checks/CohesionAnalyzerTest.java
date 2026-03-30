package checks;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.domain.CheckResult;
import rhit.csse.csse374.linter.domain.CohesionAnalyzer;
import rhit.csse.csse374.linter.domain.ConvertToASM;
import rhit.csse.csse374.linter.domain.Violation;

import java.util.List;

/**
 * Tests for CohesionAnalyzer using SingleResponsibilityPrincipleFixture.
 *
 * The fixture contains:
 *  - PASS: SrpGoodCohesiveExample (small, all methods share the same fields)
 *  - FAIL: SrpBadGodClassExample (many fields, disconnected method groups, high fan-out)
 */
public class CohesionAnalyzerTest {

    private static CheckResult result;
    private static final CohesionAnalyzer analyzer = new CohesionAnalyzer();

    @BeforeAll
    static void runCheck() {
        ASMProject project = new ConvertToASM("target/test-classes/fixtures").toASMProject();
        result = analyzer.run(project);
    }

    @Test
    void checkRunsWithoutAnalysisErrors() {
        assertFalse(result.hasAnalysisErrors(),
            "CohesionAnalyzer should not produce analysis errors: " + result.getAnalysisErrors());
    }

    @Test
    void godClassIsDetected() {
        assertTrue(result.hasViolations(),
            "CohesionAnalyzer should flag SrpBadGodClassExample for low cohesion");
    }

    @Test
    void violationMentionsCohesion() {
        List<Violation> violations = result.getViolations();
        // CohesionAnalyzer stores the description in getLocation(), class name in getMessage()
        boolean mentionsCohesion = violations.stream()
            .anyMatch(v -> v.getLocation().toLowerCase().contains("cohesion")
                       || v.getLocation().contains("LCOM4"));
        assertTrue(mentionsCohesion,
            "Violation location should mention cohesion or LCOM4. Got: " + violations);
    }

    @Test
    void interfaceIsNotFlagged() {
        ClassNode node = new ClassNode();
        node.name = "SomeInterface";
        node.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT;
        ASMClass interfaceClass = new ASMClass(node);
        List<Violation> violations = analyzer.checkClass(interfaceClass);
        assertTrue(violations.isEmpty(), "Interfaces should never be flagged for cohesion");
    }

    @Test
    void abstractClassIsNotFlagged() {
        ClassNode node = new ClassNode();
        node.name = "SomeAbstract";
        node.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT;
        ASMClass abstractClass = new ASMClass(node);
        List<Violation> violations = analyzer.checkClass(abstractClass);
        assertTrue(violations.isEmpty(), "Abstract classes should never be flagged for cohesion");
    }
}
