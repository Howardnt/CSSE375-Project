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
        if (args.length == 0) {
            System.out.println("Usage: java -jar <jar> <projectPath1> [projectPath2 ...]");
            System.out.println("This is a skeleton run; provide any path strings as placeholders.");
            return;
        }

        List<String> projectLocations = Arrays.asList(args);
        ConvertToASM converter = new ConvertToASM(projectLocations);

        List<ProjectToCheck> projects = converter.toProjectsToCheck();

        // Skeleton default configuration: create the UML-specified checks/detectors.
        List<Cursory> cursories = new ArrayList<>();
        cursories.add(new cursory1());
        cursories.add(new cursory2());
        cursories.add(new cursory3());
        cursories.add(new cursory4());

        List<Principle> principles = new ArrayList<>();
        principles.add(new principle1());
        principles.add(new principle2());
        principles.add(new principle3());
        principles.add(new principle4());

        List<Pattern> patters = new ArrayList<>();
        patters.add(new TemplatePattern());
        patters.add(new StrategyPattern());
        patters.add(new DecoratorPattern());
        patters.add(new AdaptorPattern());

        LinterHandler handler = new LinterHandler(patters, principles, cursories, projects);

        LinterOutputText output = handler.outputLinterResult();
        System.out.println(output);
    }
}

