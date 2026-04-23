package rhit.csse.csse374.linter.presentation;

import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.domain.ConvertToASM;
import rhit.csse.csse374.linter.domain.Cursory;
import rhit.csse.csse374.linter.domain.LinterHandler;
import rhit.csse.csse374.linter.domain.LinterResult;
import rhit.csse.csse374.linter.domain.Pattern;
import rhit.csse.csse374.linter.domain.Principle;

import java.util.List;
import java.util.function.Function;

/**
 * Application service that orchestrates the full linter pipeline.
 *
 * Extracted from LinterGuiFrame / RunLinterWorker so the Swing GUI and the
 * headless CLI share one execution path. This class does not depend on Swing
 * or any other UI toolkit; both presentation adapters (GUI, CLI) call into it.
 *
 * Feathers' "Parameterize Constructor" seam: the project loader can be
 * replaced in tests with a fake that skips file I/O entirely.
 */
//Left non-final so tests can use Feathers' "Subclass and Override" seam to stub run()
public class LinterService {

    public record Request(
            String projectPath,
            List<Cursory> cursories,
            List<Principle> principles,
            List<Pattern> patterns) {
    }

    public record Response(
            LinterResult result,
            String formattedText) {
    }

    private final Function<String, ASMProject> projectLoader;

    public LinterService() {
        this(path -> new ConvertToASM(path).toASMProject());
    }

    //Seam: tests inject a fake loader to avoid touching the file system
    public LinterService(Function<String, ASMProject> projectLoader) {
        this.projectLoader = projectLoader;
    }

    public Response run(Request request) {
        ASMProject project = projectLoader.apply(request.projectPath());

        LinterHandler handler = new LinterHandler(
                request.patterns(),
                request.principles(),
                request.cursories(),
                project);
        LinterResult result = handler.runLinterAnalysis();

        LinterOutputText output = new LinterOutputText();
        output.formatResult(result);

        return new Response(result, output.toString());
    }
}
