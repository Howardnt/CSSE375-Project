package rhit.csse.csse374.linter.presentation.gui;

import rhit.csse.csse374.linter.domain.Cursory;
import rhit.csse.csse374.linter.domain.LinterResult;
import rhit.csse.csse374.linter.domain.Pattern;
import rhit.csse.csse374.linter.domain.Principle;
import rhit.csse.csse374.linter.presentation.LinterService;

import javax.swing.*;
import java.util.List;

/**
 * Background worker that runs the linter off the Swing EDT.
 *
 * Orchestration was moved into LinterService so the GUI and CLI share one
 * pipeline. This worker is now a thin Swing adapter that delegates to the
 * service and passes check names through for the accordion grouping.
 */
public final class RunLinterWorker extends SwingWorker<RunLinterWorker.RunResult, Void> {

    public record RunResult(
            LinterResult result,
            String rawReport,
            List<String> cursoryCheckNames,
            List<String> principleCheckNames,
            List<String> patternCheckNames) {
    }

    private final LinterService service;
    private final String projectPath;
    private final List<Cursory> cursories;
    private final List<Principle> principles;
    private final List<Pattern> patterns;
    private final List<String> cursoryCheckNames;
    private final List<String> principleCheckNames;
    private final List<String> patternCheckNames;

    public RunLinterWorker(
            String projectPath,
            List<Cursory> cursories,
            List<Principle> principles,
            List<Pattern> patterns,
            List<String> cursoryCheckNames,
            List<String> principleCheckNames,
            List<String> patternCheckNames) {
        this(new LinterService(), projectPath, cursories, principles, patterns,
                cursoryCheckNames, principleCheckNames, patternCheckNames);
    }

    //Seam: tests inject a fake service to verify wiring without running a real scan
    RunLinterWorker(
            LinterService service,
            String projectPath,
            List<Cursory> cursories,
            List<Principle> principles,
            List<Pattern> patterns,
            List<String> cursoryCheckNames,
            List<String> principleCheckNames,
            List<String> patternCheckNames) {
        this.service = service;
        this.projectPath = projectPath;
        this.cursories = cursories;
        this.principles = principles;
        this.patterns = patterns;
        this.cursoryCheckNames = cursoryCheckNames;
        this.principleCheckNames = principleCheckNames;
        this.patternCheckNames = patternCheckNames;
    }

    @Override
    protected RunResult doInBackground() {
        LinterService.Response response = service.run(new LinterService.Request(
                projectPath, cursories, principles, patterns));

        return new RunResult(
                response.result(),
                response.formattedText(),
                cursoryCheckNames,
                principleCheckNames,
                patternCheckNames);
    }
}
