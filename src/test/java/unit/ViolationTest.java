package unit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import rhit.csse.csse374.linter.domain.SeverityLevel;
import rhit.csse.csse374.linter.domain.Violation;

public class ViolationTest {

    @Test
    void threeArgConstructorStoresAllFields() {
        Violation v = new Violation("bad equals", "MyClass.myMethod", "ERROR");
        assertEquals("bad equals", v.getMessage());
        assertEquals("MyClass.myMethod", v.getLocation());
        assertEquals("ERROR", v.getSeverity());
    }

    @Test
    void twoArgConstructorDefaultsSeverityToWarning() {
        Violation v = new Violation("bad equals", "MyClass");
        assertEquals("WARNING", v.getSeverity());
        assertEquals("MyClass", v.getLocation());
    }

    @Test
    void oneArgConstructorDefaultsSeverityAndLocation() {
        Violation v = new Violation("some message");
        assertEquals("WARNING", v.getSeverity());
        assertEquals("", v.getLocation());
        assertEquals("some message", v.getMessage());
    }

    @Test
    void toStringIncludesLocationWhenPresent() {
        Violation v = new Violation("bad equals", "MyClass", "WARNING");
        String result = v.toString();
        assertTrue(result.contains("MyClass"));
        assertTrue(result.contains("WARNING"));
        assertTrue(result.contains("bad equals"));
    }

    @Test
    void toStringOmitsAtWhenLocationIsEmpty() {
        Violation v = new Violation("some message", "", "INFO");
        assertFalse(v.toString().contains("at"));
    }

    @Test
    void severityLevelConstructorStoresSeverityLevel() {
        Violation v = new Violation("msg", "loc", SeverityLevel.ERROR);
        assertEquals(SeverityLevel.ERROR, v.getSeverityLevel());
        assertEquals("ERROR", v.getSeverity());
    }

    @Test
    void stringConstructorConvertsToSeverityLevel() {
        Violation v = new Violation("msg", "loc", "INFO");
        assertEquals(SeverityLevel.INFO, v.getSeverityLevel());
    }

    @Test
    void defaultSeverityLevelIsWarning() {
        Violation v = new Violation("msg", "loc");
        assertEquals(SeverityLevel.WARNING, v.getSeverityLevel());
    }
}
