package unit;

import org.junit.jupiter.api.Test;

import rhit.csse.csse374.linter.domain.CheckResult;
import rhit.csse.csse374.linter.domain.LinterResult;
import rhit.csse.csse374.linter.domain.SeverityLevel;
import rhit.csse.csse374.linter.domain.Violation;
import rhit.csse.csse374.linter.presentation.JsonReportWriter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JsonReportWriter.
 *
 * Before Refactoring 10 the JSON construction lived inside a Swing action
 * handler and could not be tested without a visible frame. After extraction
 * it is a pure function (LinterResult -> String) that we verify here.
 *
 * These tests lock in the *current* output format, including two pre-existing
 * quirks preserved by this pure refactoring: lack of general JSON escaping,
 * and the "message" field carrying the location string. Fixing those is a
 * separate commit.
 */
public class JsonReportWriterTest {

    private final JsonReportWriter writer = new JsonReportWriter();

    @Test
    void nullResultReturnsEmptyString() {
        assertEquals("", writer.toJson(null),
                "Null input should not crash; it should return an empty string");
    }

    @Test
    void emptyResultEmitsValidTopLevelShape() {
        LinterResult result = emptyResult("some/path");

        String json = writer.toJson(result);

        assertTrue(json.startsWith("{\n"), "JSON should begin with an opening brace on its own line");
        assertTrue(json.contains("\"project\": \"some/path\""),
                "Project path should be included as a JSON field");
        assertTrue(json.contains("\"totalViolations\": 0"),
                "totalViolations should be 0 for an empty result");
        assertTrue(json.contains("\"violations\": [\n  ]"),
                "An empty violations array should still be emitted");
        assertTrue(json.endsWith("}"), "JSON should end with a closing brace");
    }

    @Test
    void singleViolationIsEmittedWithoutTrailingComma() {
        LinterResult result = resultWith(
                new Violation("equals() missing", "com.example.Foo", SeverityLevel.ERROR));

        String json = writer.toJson(result);

        assertTrue(json.contains("\"location\": \"com.example.Foo\""));
        assertTrue(json.contains("\"severity\": \"ERROR\""));
        //Violation block is followed by a newline, not a comma, since it is the last
        int closeBrace = json.lastIndexOf("    }");
        int nextChar = closeBrace + "    }".length();
        assertEquals('\n', json.charAt(nextChar),
                "A single (last) violation must end with a newline, not a comma");
    }

    @Test
    void multipleViolationsAreCommaSeparatedExceptLast() {
        LinterResult result = resultWith(
                new Violation("first", "A", SeverityLevel.ERROR),
                new Violation("second", "B", SeverityLevel.WARNING),
                new Violation("third", "C", SeverityLevel.INFO));

        String json = writer.toJson(result);

        int commaCount = countOccurrences(json, "    },\n");
        assertEquals(2, commaCount,
                "With three violations there must be exactly two comma-terminated entries");
        int totalBlocks = countOccurrences(json, "    }");
        assertEquals(3, totalBlocks, "All three violation blocks should be present");
    }

    @Test
    void totalViolationCountReflectsAllCategories() {
        List<CheckResult> cursory = listOf(new Violation("c1", "Loc1"));
        List<CheckResult> principle = listOf(new Violation("p1", "Loc2"));
        List<CheckResult> pattern = listOf(new Violation("pat1", "Loc3"));
        LinterResult result = new LinterResult(
                cursory, principle, pattern,
                /*totalClasses*/ 0, cursory.size(), principle.size(), pattern.size(),
                "project");

        String json = writer.toJson(result);

        assertTrue(json.contains("\"totalViolations\": 3"),
                "totalViolations should aggregate across cursory, principle, and pattern results");
    }

    @Test
    void embeddedQuotesInLocationAreEscapedInMessageField() {
        //Preserves the original escaping quirk: only the "message" slot
        //(which actually carries the location) gets its quotes escaped
        LinterResult result = resultWith(
                new Violation("msg", "weird\"name", SeverityLevel.WARNING));

        String json = writer.toJson(result);

        assertTrue(json.contains("\"message\": \"weird\\\"name\""),
                "Quotes in the location string must be backslash-escaped in the message field");
    }

    @Test
    void severityLevelIsEmittedAsEnumName() {
        LinterResult result = resultWith(
                new Violation("msg", "loc", SeverityLevel.INFO));

        String json = writer.toJson(result);

        assertTrue(json.contains("\"severity\": \"INFO\""),
                "Severity should be emitted as the enum name (INFO/WARNING/ERROR)");
    }

    //--helpers--

    private static LinterResult emptyResult(String path) {
        return new LinterResult(
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                0, 0, 0, 0, path);
    }

    private static LinterResult resultWith(Violation... violations) {
        List<CheckResult> cursory = new ArrayList<>();
        List<Violation> vs = new ArrayList<>();
        for (Violation v : violations) {
            vs.add(v);
        }
        cursory.add(new CheckResult(vs, violations.length, 0, new ArrayList<>(), "Test"));
        return new LinterResult(
                cursory, new ArrayList<>(), new ArrayList<>(),
                0, 1, 0, 0, "project");
    }

    private static List<CheckResult> listOf(Violation v) {
        List<Violation> vs = new ArrayList<>();
        vs.add(v);
        List<CheckResult> out = new ArrayList<>();
        out.add(new CheckResult(vs, 1, 0, new ArrayList<>(), "Test"));
        return out;
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        int idx = 0;
        while ((idx = haystack.indexOf(needle, idx)) != -1) {
            count++;
            idx += needle.length();
        }
        return count;
    }
}
