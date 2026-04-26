package system;

import rhit.csse.csse374.linter.data.JsonLinterConfigLoader;
import rhit.csse.csse374.linter.data.LinterConfig;
import rhit.csse.csse374.linter.domain.Cursory;
import rhit.csse.csse374.linter.domain.LintCheck;
import rhit.csse.csse374.linter.domain.LinterResult;
import rhit.csse.csse374.linter.domain.Pattern;
import rhit.csse.csse374.linter.domain.Principle;
import rhit.csse.csse374.linter.presentation.JsonReportWriter;
import rhit.csse.csse374.linter.presentation.LinterService;
import rhit.csse.csse374.linter.presentation.ResultsSummary;
import rhit.csse.csse374.linter.presentation.gui.CheckCatalog;
import rhit.csse.csse374.linter.presentation.gui.CheckCatalog.CheckDescriptor;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Headless stand-in for LinterGuiFrame.
 *
 * Objective 2 of Milestone 3 asks for a "driver for the code that sits beneath
 * your presentation layer, so that you can exercise that code systematically
 * as if a user were inputting things in a repeatable way."
 *
 * This harness mirrors the GUI's user-facing operations (set target path,
 * select / deselect checks, load a JSON config, click Run, export JSON, read
 * the summary banner) without touching Swing, so use-case scenarios can be
 * scripted in plain JUnit 5. Internally it calls exactly the same classes the
 * GUI does — LinterService, CheckCatalog, JsonLinterConfigLoader,
 * JsonReportWriter, ResultsSummary — so coverage here is real coverage of the
 * production wiring.
 *
 * Test seam: a non-default constructor accepts a stub LinterService and a
 * fake catalog supplier, so use-case tests can run without disk I/O or
 * classpath scanning.
 */
public final class MockGuiHarness {

    private final LinterService service;
    private final Supplier<List<CheckDescriptor>> catalogSupplier;
    private final JsonReportWriter jsonWriter = new JsonReportWriter();
    private final ResultsSummary summaryFormatter = new ResultsSummary();

    private String targetPath;
    private LinterConfig config = LinterConfig.allEnabled();
    private final Set<String> manuallyDeselected = new HashSet<>();
    private LinterService.Response lastResponse;
    private Duration lastDuration;

    public MockGuiHarness() {
        this(new LinterService(), CheckCatalog::allChecks);
    }

    public MockGuiHarness(LinterService service, Supplier<List<CheckDescriptor>> catalogSupplier) {
        this.service = service;
        this.catalogSupplier = catalogSupplier;
    }

    //--- user actions (mirroring GUI controls) ---

    public void setTargetPath(String path) {
        this.targetPath = path;
    }

    public void selectAll() {
        manuallyDeselected.clear();
    }

    public void deselect(String checkId) {
        manuallyDeselected.add(checkId);
    }

    public void loadConfig(Path file) throws IOException {
        this.config = new JsonLinterConfigLoader().load(file);
    }

    public void useDefaultConfig() {
        this.config = LinterConfig.allEnabled();
    }

    public void clickRun() {
        if (targetPath == null || targetPath.isBlank()) {
            throw new IllegalStateException("No target path set; user must Browse first.");
        }
        long startNs = System.nanoTime();
        LinterService.Request request = buildRequest();
        lastResponse = service.run(request);
        lastDuration = Duration.ofNanos(System.nanoTime() - startNs);
    }

    //--- observers (what the GUI would render after a run) ---

    public LinterResult lastResult() {
        requireRun();
        return lastResponse.result();
    }

    public String summaryBanner() {
        requireRun();
        return summaryFormatter.format(lastResult(), lastDuration);
    }

    public String formattedReport() {
        requireRun();
        return lastResponse.formattedText();
    }

    public String exportJson() {
        requireRun();
        return jsonWriter.toJson(lastResult());
    }

    public boolean hasViolations() {
        return lastResult().hasAnyViolations();
    }

    public int totalViolations() {
        return lastResult().getTotalViolationCount();
    }

    public List<String> selectedCheckIds() {
        List<String> ids = new ArrayList<>();
        for (CheckDescriptor d : catalogSupplier.get()) {
            if (manuallyDeselected.contains(d.id())) {
                continue;
            }
            if (!config.isEnabled(d.id())) {
                continue;
            }
            ids.add(d.id());
        }
        return ids;
    }

    //--- internals ---

    private LinterService.Request buildRequest() {
        List<Cursory> cursories = new ArrayList<>();
        List<Principle> principles = new ArrayList<>();
        List<Pattern> patterns = new ArrayList<>();
        for (CheckDescriptor d : catalogSupplier.get()) {
            if (manuallyDeselected.contains(d.id())) {
                continue;
            }
            if (!config.isEnabled(d.id())) {
                continue;
            }
            LintCheck check = d.create();
            switch (d.category()) {
                case CURSORY -> cursories.add((Cursory) check);
                case PRINCIPLE -> principles.add((Principle) check);
                case PATTERN -> patterns.add((Pattern) check);
            }
        }
        return new LinterService.Request(targetPath, cursories, principles, patterns);
    }

    private void requireRun() {
        if (lastResponse == null) {
            throw new IllegalStateException("No run yet; call clickRun() first.");
        }
    }
}
