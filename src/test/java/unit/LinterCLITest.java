package unit;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;

import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.domain.CheckResult;
import rhit.csse.csse374.linter.domain.EqualsChecker;
import rhit.csse.csse374.linter.domain.LinterResult;
import rhit.csse.csse374.linter.domain.OpenClosedPrinciple;
import rhit.csse.csse374.linter.domain.PascalClassName;
import rhit.csse.csse374.linter.domain.StrategyPattern;
import rhit.csse.csse374.linter.presentation.LinterCLI;
import rhit.csse.csse374.linter.presentation.LinterService;
import rhit.csse.csse374.linter.presentation.gui.CheckCatalog;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LinterCLI.
 *
 * The CLI is exercised through its injected seams: a fake LinterService
 * captures the Request, and a fake catalog supplies deterministic descriptors.
 * Stdout and stderr are captured via ByteArrayOutputStream, so none of these
 * tests depend on the file system or the real CheckCatalog discovery.
 */
public class LinterCLITest {

    @Test
    void noArgsPrintsUsageToStderrAndReturnsUsageExitCode() {
        Streams s = new Streams();
        LinterCLI cli = cleanCliReturning(new Capture(), /*violations*/ false);

        int exit = cli.run(new String[]{}, s.out, s.err);

        assertEquals(LinterCLI.EXIT_USAGE, exit);
        assertTrue(s.errText().contains("Usage:"),
                "Usage should be printed to stderr when no args are given");
    }

    @Test
    void helpFlagPrintsUsageToStdoutAndReturnsOk() {
        Streams s = new Streams();
        LinterCLI cli = cleanCliReturning(new Capture(), false);

        int exit = cli.run(new String[]{"--help"}, s.out, s.err);

        assertEquals(LinterCLI.EXIT_OK, exit);
        assertTrue(s.outText().contains("Usage:"),
                "Usage should be printed to stdout when --help is given");
    }

    @Test
    void shortHelpFlagAlsoPrintsUsage() {
        Streams s = new Streams();
        LinterCLI cli = cleanCliReturning(new Capture(), false);

        int exit = cli.run(new String[]{"-h"}, s.out, s.err);

        assertEquals(LinterCLI.EXIT_OK, exit);
        assertTrue(s.outText().contains("Usage:"));
    }

    @Test
    void cleanRunReturnsOkExitCodeAndPrintsReport() {
        Streams s = new Streams();
        Capture capture = new Capture();
        LinterCLI cli = cleanCliReturning(capture, /*violations*/ false);

        int exit = cli.run(new String[]{"somepath"}, s.out, s.err);

        assertEquals(LinterCLI.EXIT_OK, exit, "No violations should yield EXIT_OK");
        assertFalse(s.outText().isBlank(), "The formatted report should be printed to stdout");
        assertEquals("somepath", capture.request.get().projectPath(),
                "The positional arg should become the Request.projectPath");
    }

    @Test
    void violationsCauseViolationsExitCode() {
        Streams s = new Streams();
        LinterCLI cli = cleanCliReturning(new Capture(), /*violations*/ true);

        int exit = cli.run(new String[]{"somepath"}, s.out, s.err);

        assertEquals(LinterCLI.EXIT_VIOLATIONS, exit,
                "A run that found violations must return EXIT_VIOLATIONS (useful for CI gating)");
    }

    @Test
    void onlyCursoryLimitsRequestToCursoryChecks() {
        Streams s = new Streams();
        Capture capture = new Capture();
        LinterCLI cli = cliWithFullCatalog(capture, false);

        int exit = cli.run(new String[]{"somepath", "--only", "cursory"}, s.out, s.err);

        assertEquals(LinterCLI.EXIT_OK, exit);
        LinterService.Request req = capture.request.get();
        assertFalse(req.cursories().isEmpty(), "Cursory checks should be included");
        assertTrue(req.principles().isEmpty(), "Principles must be excluded");
        assertTrue(req.patterns().isEmpty(), "Patterns must be excluded");
    }

    @Test
    void onlyPrinciplePatternIncludesBothAndExcludesCursory() {
        Streams s = new Streams();
        Capture capture = new Capture();
        LinterCLI cli = cliWithFullCatalog(capture, false);

        int exit = cli.run(new String[]{"somepath", "--only", "principle,pattern"}, s.out, s.err);

        assertEquals(LinterCLI.EXIT_OK, exit);
        LinterService.Request req = capture.request.get();
        assertTrue(req.cursories().isEmpty());
        assertFalse(req.principles().isEmpty());
        assertFalse(req.patterns().isEmpty());
    }

    @Test
    void onlyAllIncludesEveryCategory() {
        Streams s = new Streams();
        Capture capture = new Capture();
        LinterCLI cli = cliWithFullCatalog(capture, false);

        int exit = cli.run(new String[]{"somepath", "--only", "all"}, s.out, s.err);

        assertEquals(LinterCLI.EXIT_OK, exit);
        LinterService.Request req = capture.request.get();
        assertFalse(req.cursories().isEmpty());
        assertFalse(req.principles().isEmpty());
        assertFalse(req.patterns().isEmpty());
    }

    @Test
    void unknownCategoryReturnsUsageExitAndExplainsError() {
        Streams s = new Streams();
        LinterCLI cli = cliWithFullCatalog(new Capture(), false);

        int exit = cli.run(new String[]{"somepath", "--only", "bogus"}, s.out, s.err);

        assertEquals(LinterCLI.EXIT_USAGE, exit);
        assertTrue(s.errText().toLowerCase().contains("bogus"),
                "The error message should identify the bad category");
    }

    @Test
    void missingValueAfterOnlyReturnsUsageExit() {
        Streams s = new Streams();
        LinterCLI cli = cliWithFullCatalog(new Capture(), false);

        int exit = cli.run(new String[]{"somepath", "--only"}, s.out, s.err);

        assertEquals(LinterCLI.EXIT_USAGE, exit);
    }

    @Test
    void serviceFailureIsReportedAsErrorExitCode() {
        Streams s = new Streams();
        LinterService failingService = new LinterService(path -> {
            throw new RuntimeException("boom");
        });
        LinterCLI cli = new LinterCLI(failingService, () -> List.of());

        int exit = cli.run(new String[]{"somepath"}, s.out, s.err);

        assertEquals(LinterCLI.EXIT_ERROR, exit);
        assertTrue(s.errText().contains("Linter failed"),
                "The failure message should be surfaced on stderr");
    }

    private static LinterCLI cleanCliReturning(Capture capture, boolean withViolations) {
        LinterService recording = new LinterService(path -> emptyProject(path)) {
            @Override
            public Response run(Request request) {
                capture.request.set(request);
                LinterResult result = buildResult(request, withViolations);
                return new Response(result, "formatted");
            }
        };
        return new LinterCLI(recording, () -> List.of());
    }

    private static LinterCLI cliWithFullCatalog(Capture capture, boolean withViolations) {
        LinterService recording = new LinterService(path -> emptyProject(path)) {
            @Override
            public Response run(Request request) {
                capture.request.set(request);
                LinterResult result = buildResult(request, withViolations);
                return new Response(result, "formatted");
            }
        };
        return new LinterCLI(recording, LinterCLITest::fakeCatalog);
    }

    private static List<CheckCatalog.CheckDescriptor> fakeCatalog() {
        List<CheckCatalog.CheckDescriptor> list = new ArrayList<>();
        list.add(new CheckCatalog.CheckDescriptor(
                "cursory.equals",
                "Equals",
                CheckCatalog.Category.CURSORY,
                true,
                EqualsChecker::new));
        list.add(new CheckCatalog.CheckDescriptor(
                "cursory.pascal",
                "PascalClassName",
                CheckCatalog.Category.CURSORY,
                true,
                PascalClassName::new));
        list.add(new CheckCatalog.CheckDescriptor(
                "principle.ocp",
                "OCP",
                CheckCatalog.Category.PRINCIPLE,
                true,
                OpenClosedPrinciple::new));
        list.add(new CheckCatalog.CheckDescriptor(
                "pattern.strategy",
                "Strategy",
                CheckCatalog.Category.PATTERN,
                true,
                StrategyPattern::new));
        return list;
    }

    private static LinterResult buildResult(LinterService.Request request, boolean withViolations) {
        List<CheckResult> cursory = new ArrayList<>();
        List<CheckResult> principle = new ArrayList<>();
        List<CheckResult> pattern = new ArrayList<>();
        if (withViolations) {
            List<rhit.csse.csse374.linter.domain.Violation> violations = new ArrayList<>();
            violations.add(new rhit.csse.csse374.linter.domain.Violation(
                    "fake-violation", "nowhere"));
            cursory.add(new CheckResult(violations, 1, 0, new ArrayList<>(), "FakeCheck"));
        }
        return new LinterResult(
                cursory, principle, pattern,
                /*totalClasses*/ 0,
                request.cursories().size(),
                request.principles().size(),
                request.patterns().size(),
                request.projectPath());
    }

    private static ASMProject emptyProject(String path) {
        return new ASMProject(path, Collections.<ClassNode>emptyList());
    }

    private static final class Capture {
        final AtomicReference<LinterService.Request> request = new AtomicReference<>();
    }

    private static final class Streams {
        final ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        final ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
        final PrintStream out = new PrintStream(outBuf, true, StandardCharsets.UTF_8);
        final PrintStream err = new PrintStream(errBuf, true, StandardCharsets.UTF_8);

        String outText() {
            return outBuf.toString(StandardCharsets.UTF_8);
        }

        String errText() {
            return errBuf.toString(StandardCharsets.UTF_8);
        }
    }
}
