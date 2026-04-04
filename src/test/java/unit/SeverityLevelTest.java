package unit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import rhit.csse.csse374.linter.domain.SeverityLevel;

public class SeverityLevelTest {

    @Test
    void errorHasNonNullColor() {
        assertNotNull(SeverityLevel.ERROR.getColor());
    }

    @Test
    void warningHasNonNullColor() {
        assertNotNull(SeverityLevel.WARNING.getColor());
    }

    @Test
    void infoHasNonNullColor() {
        assertNotNull(SeverityLevel.INFO.getColor());
    }

    @Test
    void fromStringParsesError() {
        assertEquals(SeverityLevel.ERROR, SeverityLevel.fromString("ERROR"));
    }

    @Test
    void fromStringParsesWarning() {
        assertEquals(SeverityLevel.WARNING, SeverityLevel.fromString("WARNING"));
    }

    @Test
    void fromStringParsesWarnAlias() {
        assertEquals(SeverityLevel.WARNING, SeverityLevel.fromString("WARN"));
    }

    @Test
    void fromStringParsesInfo() {
        assertEquals(SeverityLevel.INFO, SeverityLevel.fromString("info"));
    }

    @Test
    void fromStringDefaultsToWarningForNull() {
        assertEquals(SeverityLevel.WARNING, SeverityLevel.fromString(null));
    }

    @Test
    void fromStringDefaultsToWarningForUnknown() {
        assertEquals(SeverityLevel.WARNING, SeverityLevel.fromString("CRITICAL"));
    }
}
