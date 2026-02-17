package rhit.csse.csse374.linter.presentation;

import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.domain.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point.
 *
 * Responsibilities:
 * - Accept user input (CLI for now)
 * - Configure which projects/checks to run
 * - Invoke the domain layer to run analysis
 * - Format and present the final report
 *
 * Later, this can evolve into a richer UI (interactive CLI menu or GUI).
 */
public class Main {

    public static void main(String[] args) {
        // --- SECTION 1: Hardcoded Test Path (Active for Testing) ---
        System.out.println("Running with hardcoded test path...");
        // List<String> projectLocation = Arrays.asList("target/classes");
        // --- SECTION 2: Original CLI Logic (Temporarily Commented Out) ---

        if (args.length == 0) {
            System.out.println("Usage: java -jar <jar> <projectPath>");
            return;
        }

        String projectLocation = args[0];

        // Load project
        ConvertToASM converter = new ConvertToASM(projectLocation);
        ASMProject project = converter.toASMProject();

        // Configure checks
        List<Cursory> cursories = new ArrayList<>();
        cursories.add(new EqualsChecker());
        cursories.add(new PascalClassName());
        // cursories.add(new CamelCaseChecker());
        // cursories.add(new cursory4());

        List<Principle> principles = new ArrayList<>();
        // principles.add(new openClosedPrinciple());
        // principles.add(new principle2());
        // principles.add(new principle3());
        // principles.add(new principle4());

        List<Pattern> patterns = new ArrayList<>();
        patterns.add(new TemplatePattern());
        // patterns.add(new StrategyPattern());
        // patterns.add(new DecoratorPattern());
        patterns.add(new AdapterPattern());
        patterns.add(new singleResponsibilityPrinciple());
        patterns.add(new MethodTooLongPattern());

        // Domain layer: Run the analysis
        LinterHandler handler = new LinterHandler(patterns, principles, cursories, project);
        LinterResult result = handler.runLinterAnalysis();

        // Presentation layer: Format and display the results
        LinterOutputText output = new LinterOutputText();
        output.formatResult(result);
        System.out.println(output);
    }

}