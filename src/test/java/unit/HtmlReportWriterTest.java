package unit;

import org.junit.jupiter.api.Test;

import rhit.csse.csse374.linter.domain.CheckResult;
import rhit.csse.csse374.linter.domain.LinterResult;
import rhit.csse.csse374.linter.domain.SeverityLevel;
import rhit.csse.csse374.linter.domain.Violation;
import rhit.csse.csse374.linter.presentation.HtmlReportWriter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HtmlReportWriter.
 *
 * The writer is a pure function — these tests verify the rendered HTML
 * contains the expected structural pieces (header, summary badges,
 * per-check tables) and that user-supplied text is HTML-escaped to
 * prevent injection from violation messages or project paths.
 */
public class HtmlReportWriterTest {

    private final HtmlReportWriter writer = new HtmlReportWriter();

    @Test
    void nullResultReturnsEmptyString() {
        assertEquals("", writer.toHtml(null));
    }

    @Test
    void emptyResultEmitsValidHtmlShell() {
        LinterResult result = emptyResult("some/project");
        String html = writer.toHtml(result);

        assertTrue(html.startsWith("<!DOCTYPE html>"),
                "Output must start with a valid DOCTYPE");
        assertTrue(html.contains("<title>Linter Report — some/project</title>"));
        assertTrue(html.contains("<h1>Linter Report</h1>"));
        assertTrue(html.endsWith("</html>\n"),
                "Output must close the html tag");
    }

    @Test
    void summaryBadgesShowZeroCountsForEmptyResult() {
        String html = writer.toHtml(emptyResult("p"));
        assertTrue(html.contains("0 errors</span>"));
        assertTrue(html.contains("0 warnings</span>"));
        assertTrue(html.contains("0 info</span>"));
        assertTrue(html.contains("0 total</span>"));
    }

    @Test
    void violationAppearsInACheckTable() {
        LinterResult result = resultWith(
                new Violation("equals() missing", "com.example.Foo", SeverityLevel.ERROR));
        String html = writer.toHtml(result);

        assertTrue(html.contains("<table>"), "Each check group must render as a table");
        assertTrue(html.contains("com.example.Foo"));
        assertTrue(html.contains("equals() missing"));
        assertTrue(html.contains("badge-error\">ERROR</span>"),
                "ERROR severity must render with the badge-error CSS class");
    }

    @Test
    void severityBadgesUseDistinctCssClasses() {
        LinterResult result = resultWith(
                new Violation("e", "A", SeverityLevel.ERROR),
                new Violation("w", "B", SeverityLevel.WARNING),
                new Violation("i", "C", SeverityLevel.INFO));
        String html = writer.toHtml(result);

        assertTrue(html.contains("badge-error\">ERROR"));
        assertTrue(html.contains("badge-warning\">WARNING"));
        assertTrue(html.contains("badge-info\">INFO"));
    }

    @Test
    void summaryBadgeCountsAggregateAcrossAllCategories() {
        LinterResult result = resultWith(
                new Violation("e1", "A", SeverityLevel.ERROR),
                new Violation("e2", "B", SeverityLevel.ERROR),
                new Violation("w1", "C", SeverityLevel.WARNING));
        String html = writer.toHtml(result);

        assertTrue(html.contains("2 errors</span>"));
        assertTrue(html.contains("1 warnings</span>"));
        assertTrue(html.contains("3 total</span>"));
    }

    @Test
    void htmlSpecialCharactersInViolationAreEscaped() {
        //Defends against HTML-injection from class names like "Foo<T>" or
        //project paths containing & characters.
        LinterResult result = resultWith(
                new Violation("uses <script>alert(1)</script>", "Foo<T>", SeverityLevel.WARNING));
        String html = writer.toHtml(result);

        assertFalse(html.contains("<script>alert(1)</script>"),
                "Raw <script> tag from a violation message must never appear in the output");
        assertTrue(html.contains("&lt;script&gt;alert(1)&lt;/script&gt;"),
                "Special characters in violation messages must be HTML-escaped");
        assertTrue(html.contains("Foo&lt;T&gt;"),
                "Special characters in location must also be escaped");
    }

    @Test
    void projectPathIsEscapedInTitleAndHeader() {
        LinterResult result = new LinterResult(
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                0, 0, 0, 0,
                "path/with/<bad>&chars");
        String html = writer.toHtml(result);

        assertFalse(html.contains("<bad>&chars"));
        assertTrue(html.contains("path/with/&lt;bad&gt;&amp;chars"));
    }

    @Test
    void categoryHeadersAreEmittedForAllThreeCategories() {
        String html = writer.toHtml(emptyResult("p"));
        assertTrue(html.contains("<h2>Cursory</h2>"));
        assertTrue(html.contains("<h2>Principle</h2>"));
        assertTrue(html.contains("<h2>Pattern</h2>"));
    }

    @Test
    void cssIsInlinedSoOutputIsSelfContained() {
        String html = writer.toHtml(emptyResult("p"));
        assertTrue(html.contains("<style>"),
                "Styles must be inlined; no external stylesheet links allowed");
        assertFalse(html.contains("<link"), "Self-contained output must not reference external stylesheets");
        assertFalse(html.contains("<script"), "Self-contained output must not contain JavaScript");
    }

    @Test
    void checkGroupHeaderShowsViolationCount() {
        LinterResult result = resultWith(
                new Violation("v1", "A", SeverityLevel.WARNING),
                new Violation("v2", "B", SeverityLevel.WARNING));
        String html = writer.toHtml(result);
        assertTrue(html.contains("(2)"),
                "Check-group header must indicate the number of violations");
    }

    //--helpers--

    private static LinterResult emptyResult(String path) {
        return new LinterResult(
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                0, 0, 0, 0, path);
    }

    private static LinterResult resultWith(Violation... violations) {
        List<Violation> vs = new ArrayList<>();
        for (Violation v : violations) {
            vs.add(v);
        }
        List<CheckResult> cursory = new ArrayList<>();
        cursory.add(new CheckResult(vs, 1, 0, new ArrayList<>(), "TestCheck"));
        return new LinterResult(
                cursory, new ArrayList<>(), new ArrayList<>(),
                0, 1, 0, 0, "project");
    }
}
