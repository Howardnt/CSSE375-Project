import rhit.csse.csse374.linter.data.ASMProject;
import rhit.csse.csse374.linter.domain.*;
import rhit.csse.csse374.linter.presentation.LinterOutputText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Manual smoke-test runner for the linter checks.
 *
 * This is intentionally NOT a JUnit test. Run it directly from your IDE.
 *
 * Usage:
 * - With arg: args[0] is a directory containing compiled .class files (searched
 * recursively),
 * or a single .class file.
 * - With no args: tries common output folders (bin/, target/classes/, etc.).
 */
public class ManualLintRunner {

    public static void main(String[] args) {
        String targetPath = (args.length > 0) ? args[0] : findDefaultOutputDir();

        if (targetPath == null) {
            System.err.println("No default compiled-classes output directory found.");
            System.err.println("Pass a path to a directory containing .class files, e.g.:");
            System.err.println("  ManualLintRunner C:/path/to/project/target/classes");
            System.err.println("Or, if you compiled this repo with `javac -d out ...`, run:");
            System.err.println("  ManualLintRunner out");
            System.exit(2);
            return;
        }

        System.out.println("ManualLintRunner: linting compiled classes under:");
        System.out.println("  " + new File(targetPath).getAbsolutePath());
        System.out.println();

        ConvertToASM converter = new ConvertToASM(targetPath);
        ASMProject project = converter.toASMProject();

        List<Cursory> cursories = new ArrayList<>();
        cursories.add(new equalsChecker());
        cursories.add(new PascalClassName());
        // cursories.add(new CamelCaseChecker());

        List<Principle> principles = new ArrayList<>();
        principles.add(new openClosedPrinciple());
        principles.add(new HollywoodPrinciple());
        // principles.add(...);

        List<Pattern> patterns = new ArrayList<>();
        patterns.add(new TemplatePattern());
        patterns.add(new StrategyPattern());
        patterns.add(new DecoratorPattern());
        patterns.add(new AdapterPattern());
        patterns.add(new singleResponsibilityPrinciple());
        patterns.add(new MethodTooLongPattern());

        LinterHandler handler = new LinterHandler(patterns, principles, cursories, project);
        LinterResult result = handler.runLinterAnalysis();

        LinterOutputText output = new LinterOutputText();
        output.formatResult(result);

        System.out.println(output);
    }

    private static String findDefaultOutputDir() {
        // Try common IDE / build outputs; return the first that contains .class files.
        String[] candidates = new String[] {
                // our recommended manual compilation output
                "out",
                "bin",
                "target/test-classes",
                "target/classes",
                "out/test",
                "out/production"
        };

        for (String candidate : candidates) {
            File f = new File(candidate);
            if (f.exists() && f.isDirectory() && containsClassFiles(f)) {
                return f.getPath();
            }
        }
        return null;
    }

    private static boolean containsClassFiles(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return false;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                if (containsClassFiles(f)) {
                    return true;
                }
            } else if (f.getName().endsWith(".class")) {
                return true;
            }
        }
        return false;
    }
}
