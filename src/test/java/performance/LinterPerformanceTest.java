package performance;

import org.junit.jupiter.api.Test;

import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.domain.Cursory;
import rhit.csse.csse374.linter.domain.LintCheck;
import rhit.csse.csse374.linter.domain.Pattern;
import rhit.csse.csse374.linter.domain.Principle;
import rhit.csse.csse374.linter.presentation.LinterService;
import rhit.csse.csse374.linter.presentation.gui.CheckCatalog;
import rhit.csse.csse374.linter.presentation.gui.CheckCatalog.CheckDescriptor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Lisa Crispin Q4 (performance-facing technology) tests.
 *
 * The budgets here are deliberately generous so the tests fail only on a real
 * regression — not on a slow CI runner — but tight enough that a quadratic
 * blowup (e.g. accidentally reloading the project once per check) would trip
 * them. If a budget ever needs to be raised, capture the regression first;
 * never just bump the number.
 */
public class LinterPerformanceTest {

    @Test
    void emptyProjectRunCompletesUnderOneSecond() {
        //Pipeline overhead with zero checks and zero classes should be tiny.
        //If this ever fails, the framework wiring (ConvertToASM stub, output
        //formatter, response assembly) has grown a per-run hot path.
        LinterService service = new LinterService(p -> new ASMProject(p, new ArrayList<>()));
        LinterService.Request request = new LinterService.Request(
                "fake", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        Duration elapsed = timed(() -> service.run(request));

        assertTrue(elapsed.toMillis() < 1_000,
                "Empty pipeline run should complete in under 1 s, took "
                        + elapsed.toMillis() + " ms");
    }

    @Test
    void selfScanWithAllChecksFinishesUnderBudget() {
        Path classesDir = Path.of("target", "classes");
        assumeTrue(Files.isDirectory(classesDir),
                "Skipping: target/classes does not exist (run mvn compile first)");

        LinterService service = new LinterService();
        LinterService.Request request = buildRequestForAllChecks(classesDir.toString());

        Duration elapsed = timed(() -> service.run(request));

        long budgetSeconds = 30;
        assertTrue(elapsed.toMillis() < budgetSeconds * 1000,
                "Self-scan with every check enabled should finish in under "
                        + budgetSeconds + " s, took " + elapsed.toMillis() + " ms");
    }

    @Test
    void repeatedRunsDoNotDegradeAcrossInvocations() {
        //Catches subtle cumulative state: caches that grow without bound,
        //thread-locals that pile up, classloader leaks. The tolerance is
        //wide because JIT warmup makes run #1 the slowest in practice.
        LinterService service = new LinterService(p -> new ASMProject(p, new ArrayList<>()));
        LinterService.Request request = new LinterService.Request(
                "fake", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        timed(() -> service.run(request));
        Duration first = timed(() -> service.run(request));
        Duration second = timed(() -> service.run(request));
        Duration third = timed(() -> service.run(request));

        long worstLater = Math.max(second.toMillis(), third.toMillis());
        long firstMs = Math.max(first.toMillis(), 1);
        assertTrue(worstLater <= firstMs * 5L + 50L,
                "Later runs should not be drastically slower than the first; "
                        + "first=" + firstMs + " ms, worstLater=" + worstLater + " ms");
    }

    private LinterService.Request buildRequestForAllChecks(String path) {
        List<Cursory> cursories = new ArrayList<>();
        List<Principle> principles = new ArrayList<>();
        List<Pattern> patterns = new ArrayList<>();
        for (CheckDescriptor d : CheckCatalog.allChecks()) {
            LintCheck check = d.create();
            switch (d.category()) {
                case CURSORY -> cursories.add((Cursory) check);
                case PRINCIPLE -> principles.add((Principle) check);
                case PATTERN -> patterns.add((Pattern) check);
            }
        }
        return new LinterService.Request(path, cursories, principles, patterns);
    }

    private static Duration timed(Runnable r) {
        long start = System.nanoTime();
        r.run();
        return Duration.ofNanos(System.nanoTime() - start);
    }
}
