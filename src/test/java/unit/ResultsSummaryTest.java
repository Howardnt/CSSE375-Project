package unit;

import org.junit.jupiter.api.Test;

import rhit.csse.csse374.linter.domain.CheckResult;
import rhit.csse.csse374.linter.domain.LinterResult;
import rhit.csse.csse374.linter.domain.SeverityLevel;
import rhit.csse.csse374.linter.domain.Violation;
import rhit.csse.csse374.linter.presentation.ResultsSummary;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ResultsSummary.
 *
 * The summary is what the GUI banner displays after a run, so these tests
 * lock in the formatting: severity counts, singular/plural handling, total
 * aggregation, and duration unit selection (ms vs s vs min).
 */
public class ResultsSummaryTest {

    private final ResultsSummary formatter = new ResultsSummary();

    @Test
    void nullResultShowsFriendlyPlaceholder() {
        assertEquals("No run yet.", formatter.format(null, null));
    }

    @Test
    void emptyResultShowsAllZeroCounts() {
        LinterResult result = emptyResult();
        String out = formatter.format(result, Duration.ofMillis(42));
        assertTrue(out.startsWith("0 errors · 0 warnings · 0 info · 0 total · "),
                "All-zero result must read as zero in every slot, got: " + out);
    }

    @Test
    void singularErrorAndWarningUseSingularForms() {
        LinterResult result = resultWith(
                new Violation("e", "Loc", SeverityLevel.ERROR),
                new Violation("w", "Loc", SeverityLevel.WARNING));
        String out = formatter.format(result, null);
        assertTrue(out.contains("1 error "), "Single error should read '1 error', not '1 errors'");
        assertTrue(out.contains("1 warning "), "Single warning should read '1 warning', not '1 warnings'");
    }

    @Test
    void mixedSeveritiesAreCountedSeparately() {
        LinterResult result = resultWith(
                new Violation("e1", "A", SeverityLevel.ERROR),
                new Violation("e2", "B", SeverityLevel.ERROR),
                new Violation("w1", "C", SeverityLevel.WARNING),
                new Violation("i1", "D", SeverityLevel.INFO),
                new Violation("i2", "E", SeverityLevel.INFO),
                new Violation("i3", "F", SeverityLevel.INFO));
        String out = formatter.format(result, null);
        assertTrue(out.contains("2 errors"));
        assertTrue(out.contains("1 warning "));
        assertTrue(out.contains("3 info"));
        assertTrue(out.contains("6 total"));
    }

    @Test
    void durationUnder1SecondReportsMilliseconds() {
        String out = formatter.format(emptyResult(), Duration.ofMillis(250));
        assertTrue(out.endsWith("ran in 250 ms"),
                "Sub-second runs should report ms, got: " + out);
    }

    @Test
    void durationBetween1sAnd60sReportsSeconds() {
        String out = formatter.format(emptyResult(), Duration.ofMillis(1234));
        assertTrue(out.endsWith("ran in 1.23 s"),
                "1.234 s run should report two-decimal seconds, got: " + out);
    }

    @Test
    void durationOverAMinuteReportsMinutesAndSeconds() {
        String out = formatter.format(emptyResult(), Duration.ofSeconds(75));
        assertTrue(out.endsWith("ran in 1 min 15 s"),
                "75-second run should be '1 min 15 s', got: " + out);
    }

    @Test
    void nullDurationOmitsRanInSegment() {
        String out = formatter.format(emptyResult(), null);
        assertFalse(out.contains("ran in"),
                "When no duration is supplied the banner must not fabricate one, got: " + out);
    }

    @Test
    void violationsWithoutSeverityAreNotCounted() {
        List<Violation> vs = new ArrayList<>();
        vs.add(makeViolationWithNullSeverity());
        List<CheckResult> cursory = new ArrayList<>();
        cursory.add(new CheckResult(vs, 1, 0, new ArrayList<>(), "Test"));
        LinterResult result = new LinterResult(
                cursory, new ArrayList<>(), new ArrayList<>(),
                0, 1, 0, 0, "project");

        String out = formatter.format(result, null);

        assertTrue(out.contains("0 errors") && out.contains("0 warnings") && out.contains("0 info"),
                "Null-severity violations should not inflate per-severity counts, got: " + out);
    }

    //--helpers--

    private static LinterResult emptyResult() {
        return new LinterResult(
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                0, 0, 0, 0, "project");
    }

    private static LinterResult resultWith(Violation... violations) {
        List<Violation> vs = new ArrayList<>();
        for (Violation v : violations) {
            vs.add(v);
        }
        List<CheckResult> cursory = new ArrayList<>();
        cursory.add(new CheckResult(vs, 1, 0, new ArrayList<>(), "Test"));
        return new LinterResult(
                cursory, new ArrayList<>(), new ArrayList<>(),
                0, 1, 0, 0, "project");
    }

    private static Violation makeViolationWithNullSeverity() {
        //SeverityLevel-typed constructor accepts null directly; this forces a
        //null severity so the formatter's null-guard is exercised
        return new Violation("nulled", "Loc", (SeverityLevel) null);
    }
}
