package rhit.csse.csse374.linter.presentation.gui;

import rhit.csse.csse374.linter.domain.*;

import java.util.List;
import java.util.function.Supplier;

/**
 * Central registry of checks exposed by the GUI.
 *
 * This avoids the GUI needing to know about check classes scattered across the domain layer,
 * and makes it easy to change defaults without changing linter core logic.
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
    public record CheckDescriptor<T extends LintCheck>(
            String id,
            String displayName,
            Category category,
            boolean defaultSelected,
            Supplier<T> supplier
    ) {
        public T create() {
            return supplier.get();
        }
    }

    public static List<CheckDescriptor<? extends Cursory>> cursoryChecks() {
        return List.of(
                new CheckDescriptor<>("equalsChecker", "Equals operator on reference types", Category.CURSORY, true, equalsChecker::new),
                new CheckDescriptor<>("pascalClassName", "PascalCase class names", Category.CURSORY, true, PascalClassName::new),
                new CheckDescriptor<>("camelCaseChecker", "camelCase method/field naming", Category.CURSORY, false, CamelCaseChecker::new)
        );
    }

    public static List<CheckDescriptor<? extends Principle>> principleChecks() {
        return List.of(
                new CheckDescriptor<>("openClosedPrinciple", "Open/Closed Principle", Category.PRINCIPLE, true, openClosedPrinciple::new)
        );
    }

    public static List<CheckDescriptor<? extends Pattern>> patternChecks() {
        return List.of(
                new CheckDescriptor<>("templatePattern", "Template Method detector", Category.PATTERN, true, TemplatePattern::new),
                new CheckDescriptor<>("strategyPattern", "Strategy-missing hotspots", Category.PATTERN, true, StrategyPattern::new),
                new CheckDescriptor<>("decoratorPattern", "Decorator detector", Category.PATTERN, true, DecoratorPattern::new),
                new CheckDescriptor<>("adapterPattern", "Adapter detector", Category.PATTERN, true, AdapterPattern::new),
                new CheckDescriptor<>("srpHeuristic", "Single Responsibility Principle (heuristic)", Category.PATTERN, true, singleResponsibilityPrinciple::new),
                new CheckDescriptor<>("methodTooLong", "Method too long / too many parameters", Category.PATTERN, true, MethodTooLongPattern::new)
        );
    }
}

