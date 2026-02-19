package rhit.csse.csse374.linter.presentation.gui;

import rhit.csse.csse374.linter.domain.*;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.lang.reflect.Modifier;

/**
 * Central registry of checks exposed by the GUI.
 *
 * This uses lightweight runtime discovery (classpath scanning) so that adding/renaming checks
 * does not require updating a hardcoded list in the GUI.
 */
public final class CheckCatalog {

    private CheckCatalog() {
        // utility
    }

    public enum Category {
        CURSORY,
        PRINCIPLE,
        PATTERN
    }

    /**
     * Metadata describing a single runnable check (and how to instantiate it).
     *
     * Supplier is used so each run gets a fresh instance.
     */
    public record CheckDescriptor(
            String id,
            String displayName,
            Category category,
            boolean defaultSelected,
            Supplier<LintCheck> supplier
    ) {
        public LintCheck create() {
            return supplier.get();
        }
    }

    private static volatile List<CheckDescriptor> cachedAll;

    public static List<CheckDescriptor> cursoryChecks() {
        return allChecks().stream()
                .filter(d -> d.category() == Category.CURSORY)
                .toList();
    }

    public static List<CheckDescriptor> principleChecks() {
        return allChecks().stream()
                .filter(d -> d.category() == Category.PRINCIPLE)
                .toList();
    }

    public static List<CheckDescriptor> patternChecks() {
        return allChecks().stream()
                .filter(d -> d.category() == Category.PATTERN)
                .toList();
    }

    public static List<CheckDescriptor> allChecks() {
        List<CheckDescriptor> local = cachedAll;
        if (local != null) {
            return local;
        }
        synchronized (CheckCatalog.class) {
            if (cachedAll == null) {
                cachedAll = discoverAllChecks();
            }
            return cachedAll;
        }
    }

    private static List<CheckDescriptor> discoverAllChecks() {
        String basePackage = "rhit.csse.csse374.linter.domain";
        String basePath = basePackage.replace('.', '/');

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = CheckCatalog.class.getClassLoader();
        }
        final ClassLoader finalCl = cl;

        List<String> classNames = new ArrayList<>();
        try {
            Enumeration<URL> roots = cl.getResources(basePath);
            while (roots.hasMoreElements()) {
                URL url = roots.nextElement();
                classNames.addAll(listClassNamesUnder(url, basePackage, basePath));
            }
        } catch (IOException e) {
            System.err.println("Check discovery failed reading resources: " + e.getMessage());
        }

        // De-dup and create descriptors
        return classNames.stream()
                .distinct()
                .map(name -> toDescriptor(name, finalCl))
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing((CheckDescriptor d) -> d.category().name())
                        .thenComparing(d -> d.displayName().toLowerCase(Locale.ROOT)))
                .toList();
    }

    private static List<String> listClassNamesUnder(URL url, String basePackage, String basePath) {
        try {
            return switch (url.getProtocol()) {
                case "file" -> listFromDirectory(url, basePackage);
                case "jar" -> listFromJar(url, basePath);
                default -> List.of();
            };
        } catch (Exception e) {
            System.err.println("Check discovery failed for " + url + ": " + e.getMessage());
            return List.of();
        }
    }

    private static List<String> listFromDirectory(URL url, String basePackage) throws URISyntaxException, IOException {
        URI uri = url.toURI();
        Path root = Path.of(uri);
        if (!Files.isDirectory(root)) {
            return List.of();
        }

        List<String> out = new ArrayList<>();
        try (var stream = Files.walk(root)) {
            stream
                    .filter(p -> p.toString().endsWith(".class"))
                    .forEach(p -> {
                        Path rel = root.relativize(p);
                        String relName = rel.toString()
                                .replace(File.separatorChar, '.')
                                .replace('/', '.')
                                .replace('\\', '.');
                        if (!relName.endsWith(".class")) {
                            return;
                        }
                        String simple = relName.substring(0, relName.length() - ".class".length());
                        if (simple.contains("$")) {
                            return; // ignore inner/anonymous
                        }
                        out.add(basePackage + "." + simple);
                    });
        }
        return out;
    }

    private static List<String> listFromJar(URL url, String basePath) throws IOException {
        JarURLConnection conn = (JarURLConnection) url.openConnection();
        String prefix = conn.getEntryName();
        if (prefix == null || prefix.isBlank()) {
            prefix = basePath;
        }
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }

        List<String> out = new ArrayList<>();
        try (JarFile jar = conn.getJarFile()) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();
                String name = e.getName();
                if (!name.startsWith(prefix) || !name.endsWith(".class")) {
                    continue;
                }
                if (name.contains("$")) {
                    continue;
                }
                String className = name.substring(0, name.length() - ".class".length()).replace('/', '.');
                out.add(className);
            }
        }
        return out;
    }

    private static CheckDescriptor toDescriptor(String className, ClassLoader cl) {
        try {
            Class<?> clazz = Class.forName(className, false, cl);
            if (!LintCheck.class.isAssignableFrom(clazz)) {
                return null;
            }
            if (clazz.isInterface() || clazz.isEnum() || Modifier.isAbstract(clazz.getModifiers())) {
                return null;
            }
            if (clazz.getName().equals(Cursory.class.getName())
                    || clazz.getName().equals(Principle.class.getName())
                    || clazz.getName().equals(Pattern.class.getName())) {
                return null;
            }

            Category category = categorize(clazz);
            if (category == null) {
                return null;
            }

            @SuppressWarnings("unchecked")
            Class<? extends LintCheck> checkClass = (Class<? extends LintCheck>) clazz;

            Supplier<LintCheck> supplier = () -> {
                try {
                    var ctor = checkClass.getDeclaredConstructor();
                    ctor.setAccessible(true);
                    return ctor.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate " + checkClass.getName(), e);
                }
            };

            String displayName = computeDisplayName(supplier, checkClass);
            String id = checkClass.getName();
            boolean defaultSelected = true;

            return new CheckDescriptor(id, displayName, category, defaultSelected, supplier);
        } catch (ClassNotFoundException e) {
            return null;
        } catch (LinkageError e) {
            System.err.println("Skipping check due to linkage error: " + className + " (" + e.getMessage() + ")");
            return null;
        }
    }

    private static String computeDisplayName(Supplier<LintCheck> supplier, Class<? extends LintCheck> checkClass) {
        try {
            LintCheck check = supplier.get();
            String name = check.name();
            if (name != null && !name.isBlank()) {
                return name;
            }
        } catch (Exception ignored) {
            // fall back
        }
        return checkClass.getSimpleName();
    }

    private static Category categorize(Class<?> clazz) {
        if (Cursory.class.isAssignableFrom(clazz)) {
            return Category.CURSORY;
        }
        if (Principle.class.isAssignableFrom(clazz)) {
            return Category.PRINCIPLE;
        }
        if (Pattern.class.isAssignableFrom(clazz)) {
            return Category.PATTERN;
        }
        return null;
    }
}

