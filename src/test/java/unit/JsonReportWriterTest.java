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
 * Before Refactoring 10 (M3) the JSON construction lived inside a Swing
 * action handler and could not be tested without a visible frame. After
 * extraction it is a pure function (LinterResult -> String) we verify here.
 *
 * In M4 the two pre-existing quirks the M3 refactoring preserved — no
 * general string escaping, and the "message" field emitting the location
 * string — were fixed. These tests now pin the *correct* behavior.
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
    void embeddedQuotesInLocationAreEscapedInLocationField() {
        LinterResult result = resultWith(
                new Violation("msg", "weird\"name", SeverityLevel.WARNING));

        String json = writer.toJson(result);

        assertTrue(json.contains("\"location\": \"weird\\\"name\""),
                "Quotes in the location string must be backslash-escaped in the location field");
    }

    @Test
    void messageFieldEmitsTheActualViolationMessage() {
        //M4 fix: previously the "message" field emitted the *location* string,
        //which made JSON consumers unable to read the actual violation message.
        //Now the field correctly carries the message and the location lives
        //only in the "location" field.
        LinterResult result = resultWith(
                new Violation("equals() is missing", "com.example.Foo", SeverityLevel.ERROR));

        String json = writer.toJson(result);

        assertTrue(json.contains("\"message\": \"equals() is missing\""),
                "The \"message\" field must carry the violation's message, not the location");
        assertTrue(json.contains("\"location\": \"com.example.Foo\""),
                "The \"location\" field carries the location");
    }

    @Test
    void backslashAndControlCharactersAreEscaped() {
        //M4 fix: general JSON escaping (not just quotes). Backslashes, newlines,
        //tabs etc must be properly encoded so the output parses as strict JSON.
        LinterResult result = resultWith(
                new Violation("line1\nline2\ttabbed", "weird\\path", SeverityLevel.WARNING));

        String json = writer.toJson(result);

        assertTrue(json.contains("\"message\": \"line1\\nline2\\ttabbed\""),
                "Newlines and tabs in messages must be escaped");
        assertTrue(json.contains("\"location\": \"weird\\\\path\""),
                "Backslashes in locations must be doubled to escape");
    }

    @Test
    void projectPathWithSpecialCharsIsEscaped() {
        LinterResult result = new LinterResult(
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                0, 0, 0, 0,
                "path/with\"quote");

        String json = writer.toJson(result);

        assertTrue(json.contains("\"project\": \"path/with\\\"quote\""),
                "Project path must be JSON-escaped");
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
