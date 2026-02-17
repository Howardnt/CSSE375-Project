package rhit.csse.csse374.linter.presentation.gui;

import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.domain.*;
import rhit.csse.csse374.linter.presentation.LinterOutputText;

import javax.swing.*;
import java.util.List;

/**
 * Background worker that runs the linter off the Swing EDT.
 */
public final class RunLinterWorker extends SwingWorker<RunLinterWorker.RunResult, Void> {

    public record RunResult(
            LinterResult result,
            String rawReport,
            List<String> cursoryCheckNames,
            List<String> principleCheckNames,
            List<String> patternCheckNames
    ) {
    }

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
            List<String> patternCheckNames
    ) {
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
        ConvertToASM converter = new ConvertToASM(projectPath);
        ASMProject project = converter.toASMProject();

        LinterHandler handler = new LinterHandler(patterns, principles, cursories, project);
        LinterResult result = handler.runLinterAnalysis();

        LinterOutputText output = new LinterOutputText();
        output.formatResult(result);

        return new RunResult(
                result,
                output.toString(),
                cursoryCheckNames,
                principleCheckNames,
                patternCheckNames
        );
    }
}

