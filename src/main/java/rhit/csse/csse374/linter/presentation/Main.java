package rhit.csse.csse374.linter.presentation;

import rhit.csse.csse374.linter.data.LinterOutputText;
import rhit.csse.csse374.linter.data.ProjectToCheck;
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
        List<String> projectLocations = Arrays.asList("target/classes");

        // --- SECTION 2: Original CLI Logic (Temporarily Commented Out) ---
        /*
        if (args.length == 0) {
            System.out.println("Usage: java -jar <jar> <projectPath1> [projectPath2 ...]");
            return;
        }
        List<String> projectLocations = Arrays.asList(args);
        */

        ConvertToASM converter = new ConvertToASM(projectLocations);

        List<ProjectToCheck> projects = converter.toProjectsToCheck();

        // Add checks manually
        List<Cursory> cursories = new ArrayList<>();
        cursories.add(new PascalCaseForClassName());

        List<Principle> principles = new ArrayList<>();

        List<Pattern> patters = new ArrayList<>();

        LinterHandler handler = new LinterHandler(patters, principles, cursories, projects);

        LinterOutputText output = handler.outputLinterResult();
        System.out.println(output);
    }

}
