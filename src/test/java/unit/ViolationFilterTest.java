package unit;

import org.junit.jupiter.api.Test;

import rhit.csse.csse374.linter.domain.SeverityLevel;
import rhit.csse.csse374.linter.domain.Violation;
import rhit.csse.csse374.linter.presentation.gui.ViolationFilter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ViolationFilter.
 *
 * Before Refactoring 9 this logic was a private method on ResultsAccordionPanel
 * and could not be tested without spinning up Swing. After extraction, the
 * filter is a pure class that takes any List<Violation> and returns a
 * filtered sub-list, so all edge cases fit in straight-line JUnit tests.
 */
public class ViolationFilterTest {

    private static final Violation ERROR_BAD_EQUALS = new Violation(
            "equals() missing", "com.example.Foo", SeverityLevel.ERROR);
    private static final Violation WARNING_LONG_METHOD = new Violation(
            "method is too long", "com.example.Bar.process()", SeverityLevel.WARNING);
    private static final Violation INFO_STYLE_HINT = new Violation(
            "prefer final", "com.example.Baz", SeverityLevel.INFO);

    private final List<Violation> sample = List.of(
            ERROR_BAD_EQUALS, WARNING_LONG_METHOD, INFO_STYLE_HINT);

    @Test
    void allSeverityAndEmptyQueryReturnsInputUnchanged() {
        ViolationFilter filter = new ViolationFilter("All", "");
        List<Violation> result = filter.apply(sample);
        assertEquals(sample, result, "Passive filter should return the input unchanged");
        assertFalse(filter.isActive(), "Passive filter should not report itself as active");
    }

    @Test
    void nullSeverityIsTreatedAsAll() {
        ViolationFilter filter = new ViolationFilter(null, null);
        assertFalse(filter.isActive(), "null inputs must not make the filter active");
        assertEquals(sample, filter.apply(sample));
    }

    @Test
    void severityFilterKeepsOnlyMatchingSeverity() {
        ViolationFilter filter = new ViolationFilter("ERROR", "");
        List<Violation> result = filter.apply(sample);
        assertEquals(1, result.size(), "Only ERROR violation should remain");
        assertSame(ERROR_BAD_EQUALS, result.get(0));
        assertTrue(filter.isActive());
    }

    @Test
    void severityFilterIsCaseInsensitive() {
        ViolationFilter filter = new ViolationFilter("warning", "");
        List<Violation> result = filter.apply(sample);
        assertEquals(1, result.size());
        assertSame(WARNING_LONG_METHOD, result.get(0));
    }

    @Test
    void queryFilterMatchesOnMessage() {
        ViolationFilter filter = new ViolationFilter("All", "equals");
        List<Violation> result = filter.apply(sample);
        assertEquals(1, result.size());
        assertSame(ERROR_BAD_EQUALS, result.get(0));
    }

    @Test
    void queryFilterMatchesOnLocation() {
        ViolationFilter filter = new ViolationFilter("All", "Bar.process");
        List<Violation> result = filter.apply(sample);
        assertEquals(1, result.size());
        assertSame(WARNING_LONG_METHOD, result.get(0));
    }

    @Test
    void queryFilterIsCaseInsensitive() {
        ViolationFilter filter = new ViolationFilter("All", "EQUALS");
        assertEquals(1, filter.apply(sample).size(),
                "Lowercase vs uppercase query should not matter");
    }

    @Test
    void queryAndSeverityApplyTogether() {
        ViolationFilter filter = new ViolationFilter("INFO", "prefer");
        List<Violation> result = filter.apply(sample);
        assertEquals(1, result.size());
        assertSame(INFO_STYLE_HINT, result.get(0));
    }

    @Test
    void queryThatDoesNotMatchReturnsEmpty() {
        ViolationFilter filter = new ViolationFilter("All", "no-such-thing");
        assertTrue(filter.apply(sample).isEmpty(),
                "Non-matching query should produce an empty result");
        assertTrue(filter.isActive());
    }

    @Test
    void queryWhitespaceIsTrimmedBeforeMatching() {
        ViolationFilter filter = new ViolationFilter("All", "   equals   ");
        assertEquals(1, filter.apply(sample).size(),
                "Leading/trailing whitespace in the query must be ignored");
    }

    @Test
    void applyHandlesNullAndEmptyInputsGracefully() {
        ViolationFilter filter = new ViolationFilter("ERROR", "foo");
        assertTrue(filter.apply(null).isEmpty(), "null input should yield an empty list");
        assertTrue(filter.apply(List.of()).isEmpty(), "empty input should yield an empty list");
    }

    @Test
    void isActiveDetectsSeverityOrQueryAlone() {
        assertTrue(new ViolationFilter("ERROR", "").isActive(),
                "Non-All severity alone should make the filter active");
        assertTrue(new ViolationFilter("All", "q").isActive(),
                "A query alone should make the filter active");
        assertFalse(new ViolationFilter("All", "").isActive(),
                "Defaults should leave the filter inactive");
    }
}
