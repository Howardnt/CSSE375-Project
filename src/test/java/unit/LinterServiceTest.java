package unit;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;

import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.domain.Cursory;
import rhit.csse.csse374.linter.domain.EqualsChecker;
import rhit.csse.csse374.linter.domain.Pattern;
import rhit.csse.csse374.linter.domain.Principle;
import rhit.csse.csse374.linter.presentation.LinterService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LinterService.
 *
 * Exercises the "Parameterize Constructor" seam so the project loader is
 * replaced with a fake — the service is tested without touching disk.
 */
public class LinterServiceTest {

    @Test
    void runReturnsNonNullResponseWithFormattedText() {
        LinterService service = new LinterService(path -> emptyProject(path));

        LinterService.Response response = service.run(new LinterService.Request(
                "/fake/path", List.of(), List.of(), List.of()));

        assertNotNull(response, "Response must not be null");
        assertNotNull(response.result(), "Response.result must not be null");
        assertNotNull(response.formattedText(), "Response.formattedText must not be null");
        assertFalse(response.formattedText().isBlank(),
                "Formatted text should be populated even on an empty project");
    }

    @Test
    void seamPassesProjectPathToLoader() {
        AtomicReference<String> seen = new AtomicReference<>();
        LinterService service = new LinterService(path -> {
            seen.set(path);
            return emptyProject(path);
        });

        service.run(new LinterService.Request(
                "target/test-classes/fixtures", List.of(), List.of(), List.of()));

        assertEquals("target/test-classes/fixtures", seen.get(),
                "The seam should forward the request's projectPath to the loader");
    }

    @Test
    void runExecutesSuppliedChecksAndReflectsCountsInResult() {
        LinterService service = new LinterService(path -> emptyProject(path));
        List<Cursory> cursories = List.of(new EqualsChecker());

        LinterService.Response response = service.run(new LinterService.Request(
                "/fake/path", cursories, List.of(), List.of()));

        assertEquals(1, response.result().getCursoryCheckCount(),
                "Cursory check count should reflect the number of cursory checks run");
        assertEquals(0, response.result().getPrincipleCheckCount());
        assertEquals(0, response.result().getPatternCheckCount());
        assertEquals(1, response.result().getCursoryResults().size(),
                "One cursory result should be produced per cursory check");
    }

    @Test
    void defaultConstructorUsesRealProjectLoader() {
        LinterService service = new LinterService();
        LinterService.Response response = service.run(new LinterService.Request(
                "target/test-classes/fixtures",
                List.of(new EqualsChecker()),
                List.of(),
                List.of()));

        assertTrue(response.result().getTotalClasses() > 0,
                "Default loader should pick up compiled fixture classes");
    }

    @Test
    void runPreservesProjectPathOnResult() {
        LinterService service = new LinterService(path -> emptyProject(path));

        LinterService.Response response = service.run(new LinterService.Request(
                "/some/fake/location",
                List.of(),
                List.of(),
                List.of()));

        assertEquals("/some/fake/location", response.result().getProjectPath(),
                "Result should carry through the project path from the loaded project");
    }

    @Test
    void runIsRepeatableAndDoesNotMutateRequestLists() {
        LinterService service = new LinterService(path -> emptyProject(path));
        List<Cursory> cursories = new ArrayList<>();
        cursories.add(new EqualsChecker());
        List<Principle> principles = new ArrayList<>();
        List<Pattern> patterns = new ArrayList<>();

        LinterService.Request request = new LinterService.Request(
                "/fake/path", cursories, principles, patterns);

        LinterService.Response first = service.run(request);
        LinterService.Response second = service.run(request);

        assertEquals(first.result().getCursoryCheckCount(),
                second.result().getCursoryCheckCount(),
                "Running the same request twice should produce equivalent counts");
        assertEquals(1, cursories.size(), "Service must not mutate the caller's list");
    }

    private static ASMProject emptyProject(String path) {
        return new ASMProject(path, Collections.<ClassNode>emptyList());
    }
}
