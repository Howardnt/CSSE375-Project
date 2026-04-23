package rhit.csse.csse374.linter.presentation;

import rhit.csse.csse374.linter.domain.Cursory;
import rhit.csse.csse374.linter.domain.LintCheck;
import rhit.csse.csse374.linter.domain.Pattern;
import rhit.csse.csse374.linter.domain.Principle;
import rhit.csse.csse374.linter.presentation.gui.CheckCatalog;
import rhit.csse.csse374.linter.presentation.gui.CheckCatalog.CheckDescriptor;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Headless command-line entry point for the linter.
 *
 * Usage:
 *   linter <path> [--only cursory,principle,pattern,all] [--help]
 *
 * Sharing the execution path with the GUI is possible because both adapters
 * delegate to LinterService. The CLI only owns argument parsing, check
 * category selection, and printing the formatted report.
 *
 * Exit codes:
 *   0 — success, no violations
 *   1 — success, violations were found (useful for CI gating)
 *   2 — usage error (missing args, bad flags)
 *   3 — unexpected failure while running the linter
 */
public final class LinterCLI {

    public static final int EXIT_OK = 0;
    public static final int EXIT_VIOLATIONS = 1;
    public static final int EXIT_USAGE = 2;
    public static final int EXIT_ERROR = 3;

    private final LinterService service;
    private final Supplier<List<CheckDescriptor>> catalogSupplier;

    public LinterCLI() {
        this(new LinterService(), CheckCatalog::allChecks);
    }

    //Seam: tests inject a fake service and catalog to exercise parsing and
    //wiring without running a real scan or depending on classpath discovery
    public LinterCLI(LinterService service, Supplier<List<CheckDescriptor>> catalogSupplier) {
        this.service = service;
        this.catalogSupplier = catalogSupplier;
    }

    public static void main(String[] args) {
        int exit = new LinterCLI().run(args, System.out, System.err);
        System.exit(exit);
    }

    public int run(String[] args, PrintStream out, PrintStream err) {
        if (hasHelpFlag(args)) {
            printUsage(out);
            return EXIT_OK;
        }
        if (args.length == 0) {
            printUsage(err);
            return EXIT_USAGE;
        }

        String targetPath = args[0];
        Set<CheckCatalog.Category> categories = parseCategories(args, err);
        if (categories == null) {
            return EXIT_USAGE;
        }

        LinterService.Request request = buildRequest(targetPath, categories);
        LinterService.Response response;
        try {
            response = service.run(request);
        } catch (RuntimeException e) {
            err.println("Linter failed: " + e.getMessage());
            return EXIT_ERROR;
        }

        out.println(response.formattedText());
        return response.result().hasAnyViolations() ? EXIT_VIOLATIONS : EXIT_OK;
    }

    private boolean hasHelpFlag(String[] args) {
        for (String a : args) {
            if ("--help".equals(a) || "-h".equals(a)) {
                return true;
            }
        }
        return false;
    }

    private Set<CheckCatalog.Category> parseCategories(String[] args, PrintStream err) {
        for (int i = 1; i < args.length; i++) {
            if (!"--only".equals(args[i])) {
                continue;
            }
            if (i + 1 >= args.length) {
                err.println("--only requires a comma-separated list of categories");
                return null;
            }
            return readCategoryList(args[i + 1], err);
        }
        return EnumSet.allOf(CheckCatalog.Category.class);
    }

    private Set<CheckCatalog.Category> readCategoryList(String raw, PrintStream err) {
        Set<CheckCatalog.Category> result = EnumSet.noneOf(CheckCatalog.Category.class);
        for (String piece : raw.split(",")) {
            String token = piece.trim().toUpperCase(Locale.ROOT);
            if (token.isEmpty()) {
                continue;
            }
            if ("ALL".equals(token)) {
                return EnumSet.allOf(CheckCatalog.Category.class);
            }
            try {
                result.add(CheckCatalog.Category.valueOf(token));
            } catch (IllegalArgumentException e) {
                err.println("Unknown category: " + piece.trim()
                        + " (expected cursory, principle, pattern, or all)");
                return null;
            }
        }
        if (result.isEmpty()) {
            err.println("--only requires at least one category");
            return null;
        }
        return result;
    }

    private LinterService.Request buildRequest(String path, Set<CheckCatalog.Category> categories) {
        List<Cursory> cursories = new ArrayList<>();
        List<Principle> principles = new ArrayList<>();
        List<Pattern> patterns = new ArrayList<>();

        for (CheckDescriptor d : catalogSupplier.get()) {
            if (!categories.contains(d.category())) {
                continue;
            }
            LintCheck check = d.create();
            switch (d.category()) {
                case CURSORY -> cursories.add((Cursory) check);
                case PRINCIPLE -> principles.add((Principle) check);
                case PATTERN -> patterns.add((Pattern) check);
            }
        }
        return new LinterService.Request(path, cursories, principles, patterns);
    }

    private void printUsage(PrintStream out) {
        out.println("Usage: linter <path> [--only cursory,principle,pattern,all] [--help]");
        out.println("  <path>                   directory of .class files or a single .class file");
        out.println("  --only <categories>      comma-separated check categories (default: all)");
        out.println("  --help, -h               show this message");
        out.println();
        out.println("Exit codes: 0=clean, 1=violations, 2=usage, 3=error");
    }
}
