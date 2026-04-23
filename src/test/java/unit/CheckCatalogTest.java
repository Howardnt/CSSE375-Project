package unit;

import org.junit.jupiter.api.Test;

import rhit.csse.csse374.linter.domain.Cursory;
import rhit.csse.csse374.linter.domain.HollywoodPrinciple;
import rhit.csse.csse374.linter.domain.LintCheck;
import rhit.csse.csse374.linter.domain.Pattern;
import rhit.csse.csse374.linter.domain.Principle;
import rhit.csse.csse374.linter.presentation.gui.CheckCatalog;
import rhit.csse.csse374.linter.presentation.gui.CheckCatalog.CheckDescriptor;
import rhit.csse.csse374.linter.presentation.gui.CheckCatalog.Category;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for CheckCatalog. These lock in the discovery behaviour
 * that existed before Refactoring 8 (Extract Method on toDescriptor) so the
 * extraction cannot silently drop or mis-categorize checks.
 */
public class CheckCatalogTest {

    @Test
    void discoveryReturnsNonEmptyList() {
        List<CheckDescriptor> all = CheckCatalog.allChecks();
        assertNotNull(all, "allChecks must not return null");
        assertFalse(all.isEmpty(), "Classpath scanning should find at least one check");
    }

    @Test
    void everyDescriptorHasANonNullCategoryAndDisplayName() {
        for (CheckDescriptor d : CheckCatalog.allChecks()) {
            assertNotNull(d.category(), "Descriptor category must not be null: " + d.id());
            assertNotNull(d.displayName(), "Descriptor displayName must not be null: " + d.id());
            assertFalse(d.displayName().isBlank(),
                    "Descriptor displayName must not be blank: " + d.id());
        }
    }

    @Test
    void baseClassesAreExcludedFromDiscovery() {
        Set<String> ids = idSet(CheckCatalog.allChecks());
        assertFalse(ids.contains(Cursory.class.getName()),
                "Abstract Cursory base class must not appear in the catalog");
        assertFalse(ids.contains(Principle.class.getName()),
                "Abstract Principle base class must not appear in the catalog");
        assertFalse(ids.contains(Pattern.class.getName()),
                "Abstract Pattern base class must not appear in the catalog");
        assertFalse(ids.contains(HollywoodPrinciple.class.getName()),
                "HollywoodPrinciple base class must not appear in the catalog");
    }

    @Test
    void knownConcreteChecksAreDiscovered() {
        Set<String> ids = idSet(CheckCatalog.allChecks());
        //Sanity-check a few representative checks from each category
        assertTrue(ids.contains("rhit.csse.csse374.linter.domain.EqualsChecker"),
                "EqualsChecker should be discovered");
        assertTrue(ids.contains("rhit.csse.csse374.linter.domain.PascalClassName"),
                "PascalClassName should be discovered");
        assertTrue(ids.contains("rhit.csse.csse374.linter.domain.OpenClosedPrinciple"),
                "OpenClosedPrinciple should be discovered");
        assertTrue(ids.contains("rhit.csse.csse374.linter.domain.StrategyPattern"),
                "StrategyPattern should be discovered");
    }

    @Test
    void categoryMatchesActualCheckTypeForEveryDescriptor() {
        for (CheckDescriptor d : CheckCatalog.allChecks()) {
            LintCheck check = d.create();
            switch (d.category()) {
                case CURSORY -> assertInstanceOf(Cursory.class, check,
                        "CURSORY descriptor must produce a Cursory: " + d.id());
                case PRINCIPLE -> assertInstanceOf(Principle.class, check,
                        "PRINCIPLE descriptor must produce a Principle: " + d.id());
                case PATTERN -> assertInstanceOf(Pattern.class, check,
                        "PATTERN descriptor must produce a Pattern: " + d.id());
            }
        }
    }

    @Test
    void supplierProducesFreshInstanceEachCall() {
        CheckDescriptor any = CheckCatalog.allChecks().get(0);
        LintCheck first = any.create();
        LintCheck second = any.create();
        assertNotNull(first);
        assertNotNull(second);
        assertNotSame(first, second,
                "Supplier must create a new instance per invocation so runs do not share state");
    }

    @Test
    void categoryFiltersPartitionAllChecks() {
        List<CheckDescriptor> all = CheckCatalog.allChecks();
        List<CheckDescriptor> cursory = CheckCatalog.cursoryChecks();
        List<CheckDescriptor> principle = CheckCatalog.principleChecks();
        List<CheckDescriptor> pattern = CheckCatalog.patternChecks();

        assertEquals(
                all.size(),
                cursory.size() + principle.size() + pattern.size(),
                "Category filters should partition allChecks with no overlap or loss");

        cursory.forEach(d -> assertEquals(Category.CURSORY, d.category()));
        principle.forEach(d -> assertEquals(Category.PRINCIPLE, d.category()));
        pattern.forEach(d -> assertEquals(Category.PATTERN, d.category()));
    }

    @Test
    void allChecksIsCachedAndStable() {
        List<CheckDescriptor> first = CheckCatalog.allChecks();
        List<CheckDescriptor> second = CheckCatalog.allChecks();
        assertSame(first, second,
                "Repeated calls to allChecks() should return the cached list reference");
    }

    private static Set<String> idSet(List<CheckDescriptor> descriptors) {
        return descriptors.stream().map(CheckDescriptor::id).collect(Collectors.toSet());
    }
}
