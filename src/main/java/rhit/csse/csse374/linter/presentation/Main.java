package rhit.csse.csse374.linter.presentation;

import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.data.LinterOutputText;
import rhit.csse.csse374.linter.domain.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Presentation-layer entry point.
 *
 * Responsibilities:
 * - Accept user input (CLI for now)
 * - Configure which projects/checks to run
 * - Invoke the domain layer
 * - Present the final report
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
        String projectLocation = "target/classes";
        System.out.println("Running linter on project at: " + projectLocation);
        

        ConvertToASM converter = new ConvertToASM(projectLocation);

        ASMProject project = converter.toASMProject();

        // Add checks manually
        List<Cursory> cursories = new ArrayList<>();
        // cursories.add(new equalsChecker());
        cursories.add(new PascalClassName());
        cursories.add(new cursory3());
        cursories.add(new cursory4());

        List<Principle> principles = new ArrayList<>();
        principles.add(new openClosedPrinciple());
        principles.add(new principle2());
        principles.add(new principle3());
        principles.add(new principle4());

        List<Pattern> patterns = new ArrayList<>();
        patterns.add(new TemplatePattern());
        patterns.add(new StrategyPattern());
        patterns.add(new DecoratorPattern());
        patterns.add(new AdapterPattern());

        LinterHandler handler = new LinterHandler(patterns, principles, cursories, project);

        LinterOutputText output = handler.outputLinterResult();
        System.out.println(output);
    }

}