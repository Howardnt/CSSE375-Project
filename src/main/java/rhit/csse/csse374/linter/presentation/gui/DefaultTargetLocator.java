package rhit.csse.csse374.linter.presentation.gui;

import java.io.File;

/**
 * Utility for guessing a reasonable default compiled-classes output directory.
 *
 * This mirrors the idea in ManualLintRunner, but lives in main sources so the GUI doesn't
 * depend on test code.
 */
public final class DefaultTargetLocator {

    private DefaultTargetLocator() {
        // utility
    }

    public static String findDefaultOutputDir() {
        String[] candidates = new String[]{
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

    public static boolean containsClassFiles(File dir) {
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

