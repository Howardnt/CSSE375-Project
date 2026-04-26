package system;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import rhit.csse.csse374.linter.data.ASMClass;
import rhit.csse.csse374.linter.domain.CheckResult;
import rhit.csse.csse374.linter.domain.Cursory;
import rhit.csse.csse374.linter.domain.LinterResult;
import rhit.csse.csse374.linter.domain.SeverityLevel;
import rhit.csse.csse374.linter.domain.Violation;
import rhit.csse.csse374.linter.presentation.LinterService;
import rhit.csse.csse374.linter.presentation.gui.CheckCatalog;
import rhit.csse.csse374.linter.presentation.gui.CheckCatalog.CheckDescriptor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Use-case driver tests for MockGuiHarness.
 *
 * Each test plays one user-facing scenario end-to-end through the harness as
 * if a person were clicking buttons in the real GUI: pick a target path,
 * adjust check selection, load (or skip) a config, click Run, then read the
 * summary banner / export JSON.
 *
 * The harness is wired with a stub LinterService that records every request
 * it receives, which lets each use case assert on *what the GUI sent down to
 * the linter* — the integration glue between user actions and the domain
 * pipeline. That glue is exactly what was previously trapped inside Swing.
 */
public class UseCaseDriverTest {

    //--- UC1: Run with default selection ---
    @Test
    void useCase_userPicksTargetAndClicksRun_completesAndReportsViolations() {
        RecordingService stub = new RecordingService(canned("project", 2, 1, 0));
        MockGuiHarness gui = new MockGuiHarness(stub, fakeCatalog());

        gui.setTargetPath("path/to/classes");
        gui.clickRun();

        assertEquals(1, stub.received.size(), "User clicked Run exactly once");
        assertEquals("path/to/classes", stub.received.get(0).projectPath());
        assertTrue(gui.hasViolations());
        assertEquals(3, gui.totalViolations(),
                "Total should aggregate the 2 errors + 1 warning across categories");
    }

    //--- UC2: Deselect one check before running ---
    @Test
    void useCase_userDeselectsACheck_thatCheckIsNotSentToTheLinter() {
        RecordingService stub = new RecordingService(canned("p", 0, 0, 0));
        MockGuiHarness gui = new MockGuiHarness(stub, fakeCatalog());

        gui.setTargetPath("p");
        gui.deselect("FakeNamingCheck");
        gui.clickRun();

        LinterService.Request request = stub.received.get(0);
        assertEquals(1, request.cursories().size(),
                "One of the two cursory checks was deselected, so only one should run");
        assertFalse(gui.selectedCheckIds().contains("FakeNamingCheck"));
    }

    //--- UC3: Load a JSON config that disables a check ---
    @Test
    void useCase_userLoadsJsonConfigDisablingACheck_thatCheckIsExcluded(@TempDir Path tmp) throws IOException {
        Path configFile = tmp.resolve("linter.json");
        Files.writeString(configFile, """
                {
                  "checks": {
                    "FakeEqualsCheck": { "enabled": false }
                  }
                }
                """);
        RecordingService stub = new RecordingService(canned("p", 0, 0, 0));
        MockGuiHarness gui = new MockGuiHarness(stub, fakeCatalog());

        gui.setTargetPath("p");
        gui.loadConfig(configFile);
        gui.clickRun();

        LinterService.Request request = stub.received.get(0);
        boolean equalsCheckPresent = request.cursories().stream()
                .anyMatch(c -> "FakeEqualsCheck".equals(c.name()));
        assertFalse(equalsCheckPresent, "Config disabled FakeEqualsCheck — it must not be sent");
        assertFalse(gui.selectedCheckIds().contains("FakeEqualsCheck"));
    }

    //--- UC4: Run, then export JSON ---
    @Test
    void useCase_userRunsThenExportsJson_jsonContainsExpectedFields() {
        RecordingService stub = new RecordingService(canned("project-xyz", 1, 0, 0));
        MockGuiHarness gui = new MockGuiHarness(stub, fakeCatalog());

        gui.setTargetPath("project-xyz");
        gui.clickRun();
        String json = gui.exportJson();

        assertTrue(json.contains("\"project\": \"project-xyz\""),
                "Exported JSON must record the project path the user picked");
        assertTrue(json.contains("\"totalViolations\": 1"));
        assertTrue(json.contains("\"severity\": \"ERROR\""));
    }

    //--- UC5: Run, then read the summary banner ---
    @Test
    void useCase_userRunsThenReadsSummaryBanner_bannerReflectsActualCounts() {
        RecordingService stub = new RecordingService(canned("p", 2, 3, 1));
        MockGuiHarness gui = new MockGuiHarness(stub, fakeCatalog());

        gui.setTargetPath("p");
        gui.clickRun();

        String banner = gui.summaryBanner();

        assertTrue(banner.contains("2 errors"),
                "Banner should reflect the 2 ERROR violations, got: " + banner);
        assertTrue(banner.contains("3 warnings"),
                "Banner should reflect the 3 WARNING violations, got: " + banner);
        assertTrue(banner.contains("1 info"),
                "Banner should reflect the 1 INFO violation, got: " + banner);
        assertTrue(banner.contains("6 total"));
        assertTrue(banner.contains("ran in"),
                "Banner should always include a duration after a run");
    }

    //--- UC6: Click Run with no target path picked ---
    @Test
    void useCase_userClicksRunWithoutTarget_userGetsClearError() {
        RecordingService stub = new RecordingService(canned("p", 0, 0, 0));
        MockGuiHarness gui = new MockGuiHarness(stub, fakeCatalog());

        IllegalStateException ex = assertThrows(IllegalStateException.class, gui::clickRun);
        assertTrue(ex.getMessage().toLowerCase().contains("target"));
        assertEquals(0, stub.received.size(),
                "When the user has not picked a target we must not invoke the linter at all");
    }

    //--- UC7: Loading a malformed config surfaces the error to the caller ---
    @Test
    void useCase_userLoadsMalformedConfig_errorBubblesUpForGuiToShow(@TempDir Path tmp) throws IOException {
        Path bad = tmp.resolve("oops.json");
        Files.writeString(bad, "{ not: valid json");

        MockGuiHarness gui = new MockGuiHarness(
                new RecordingService(canned("p", 0, 0, 0)), fakeCatalog());

        assertThrows(RuntimeException.class, () -> gui.loadConfig(bad),
                "Malformed config must surface to the caller, not silently apply");
    }

    //--- UC8: Selecting all after a deselect resets state ---
    @Test
    void useCase_userClicksSelectAllAfterDeselecting_allChecksReturn() {
        RecordingService stub = new RecordingService(canned("p", 0, 0, 0));
        MockGuiHarness gui = new MockGuiHarness(stub, fakeCatalog());

        gui.setTargetPath("p");
        gui.deselect("FakeEqualsCheck");
        gui.deselect("FakeNamingCheck");
        gui.selectAll();
        gui.clickRun();

        assertEquals(2, stub.received.get(0).cursories().size(),
                "Select all should re-include both fake cursory checks");
    }

    //--- UC9: Reading the banner before any run is a clear error ---
    @Test
    void useCase_userOpensBannerBeforeRun_throwsClearError() {
        MockGuiHarness gui = new MockGuiHarness(
                new RecordingService(canned("p", 0, 0, 0)), fakeCatalog());

        assertThrows(IllegalStateException.class, gui::summaryBanner);
        assertThrows(IllegalStateException.class, gui::exportJson);
    }

    //==================== helpers ====================

    /**
     * Subclass-and-override seam on LinterService: records every Request and
     * returns a canned Response. The throwing project loader is unreachable
     * because run() is overridden to never delegate.
     */
    private static final class RecordingService extends LinterService {
        final List<Request> received = new ArrayList<>();
        private final LinterResult cannedResult;
        private final String cannedText;

        RecordingService(LinterResult cannedResult) {
            super(path -> { throw new AssertionError("project loader should not be invoked in tests"); });
            this.cannedResult = cannedResult;
            this.cannedText = "Stub run complete.";
        }

        @Override
        public Response run(Request request) {
            received.add(request);
            return new Response(cannedResult, cannedText);
        }
    }

    private static Supplier<List<CheckDescriptor>> fakeCatalog() {
        return () -> List.of(
                desc("FakeEqualsCheck", CheckCatalog.Category.CURSORY),
                desc("FakeNamingCheck", CheckCatalog.Category.CURSORY));
    }

    private static CheckDescriptor desc(String name, CheckCatalog.Category cat) {
        return new CheckDescriptor(
                name, name, cat, true, () -> new FakeCursory(name));
    }

    private static final class FakeCursory extends Cursory {
        private final String name;

        FakeCursory(String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public List<Violation> checkClass(ASMClass cls) {
            return List.of();
        }
    }

    /**
     * Builds a deterministic LinterResult with the requested per-severity
     * violation counts, all filed under one cursory check group so tests can
     * inspect totals without depending on real ASM scanning.
     */
    private static LinterResult canned(String projectPath, int errors, int warnings, int infos) {
        List<Violation> vs = new ArrayList<>();
        for (int i = 0; i < errors; i++) {
            vs.add(new Violation("e" + i, "Loc", SeverityLevel.ERROR));
        }
        for (int i = 0; i < warnings; i++) {
            vs.add(new Violation("w" + i, "Loc", SeverityLevel.WARNING));
        }
        for (int i = 0; i < infos; i++) {
            vs.add(new Violation("i" + i, "Loc", SeverityLevel.INFO));
        }
        List<CheckResult> cursory = new ArrayList<>();
        cursory.add(new CheckResult(vs, 1, 0, new ArrayList<>(), "Stub"));
        return new LinterResult(
                cursory, new ArrayList<>(), new ArrayList<>(),
                0, 1, 0, 0, projectPath);
    }
}
