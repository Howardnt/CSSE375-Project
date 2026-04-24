package unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import rhit.csse.csse374.linter.data.JsonLinterConfigLoader;
import rhit.csse.csse374.linter.data.JsonLinterConfigLoader.ConfigParseException;
import rhit.csse.csse374.linter.data.LinterConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JsonLinterConfigLoader.
 *
 * Covers the three things the plan calls out for the config feature:
 *   1. valid files parse correctly
 *   2. defaults apply for absent keys
 *   3. malformed input fails loudly instead of silently producing a broken config
 */
public class JsonLinterConfigLoaderTest {

    private final JsonLinterConfigLoader loader = new JsonLinterConfigLoader();

    @Test
    void emptyStringReturnsAllEnabledConfig() {
        LinterConfig config = loader.parse("");
        assertTrue(config.isEnabled("EqualsChecker"),
                "Empty input should behave like defaults: every check enabled");
        assertTrue(config.optionsFor("EqualsChecker").isEmpty());
    }

    @Test
    void nullInputReturnsAllEnabledConfig() {
        LinterConfig config = loader.parse(null);
        assertTrue(config.isEnabled("anything"));
    }

    @Test
    void emptyObjectLeavesDefaultEnabledTrue() {
        LinterConfig config = loader.parse("{}");
        assertTrue(config.isEnabled("EqualsChecker"),
                "When \"defaultEnabled\" is absent it must default to true");
    }

    @Test
    void disabledCheckIsReportedDisabled() {
        String json = """
                {
                  "checks": {
                    "EqualsChecker": { "enabled": false }
                  }
                }
                """;
        LinterConfig config = loader.parse(json);
        assertFalse(config.isEnabled("EqualsChecker"));
        assertTrue(config.isEnabled("OtherCheckNotListed"),
                "Checks that are absent from the config should still default to enabled");
    }

    @Test
    void fullyQualifiedIdMatchesSimpleNameKey() {
        String json = """
                {
                  "checks": {
                    "EqualsChecker": { "enabled": false }
                  }
                }
                """;
        LinterConfig config = loader.parse(json);
        assertFalse(config.isEnabled("rhit.csse.csse374.linter.domain.EqualsChecker"),
                "A simple-name key in the JSON should also match a fully qualified check id");
    }

    @Test
    void defaultEnabledFalseFlipsUnlistedChecksOff() {
        String json = """
                {
                  "defaultEnabled": false,
                  "checks": {
                    "EqualsChecker": { "enabled": true }
                  }
                }
                """;
        LinterConfig config = loader.parse(json);
        assertTrue(config.isEnabled("EqualsChecker"),
                "Explicitly enabled check should still be on when defaultEnabled is false");
        assertFalse(config.isEnabled("SomeRandomCheck"),
                "Unlisted checks must inherit defaultEnabled=false");
    }

    @Test
    void optionsForExposesStringValues() {
        String json = """
                {
                  "checks": {
                    "MethodTooLongPattern": {
                      "enabled": true,
                      "options": { "threshold": "20", "strict": true }
                    }
                  }
                }
                """;
        LinterConfig config = loader.parse(json);
        assertEquals("20", config.optionsFor("MethodTooLongPattern").get("threshold"));
        assertEquals("true", config.optionsFor("MethodTooLongPattern").get("strict"));
    }

    @Test
    void optionsForUnknownCheckReturnsEmptyMap() {
        LinterConfig config = loader.parse("{\"checks\": {}}");
        assertTrue(config.optionsFor("NoSuchCheck").isEmpty());
    }

    @Test
    void malformedJsonThrowsConfigParseException() {
        assertThrows(ConfigParseException.class,
                () -> loader.parse("{ not: valid json"),
                "A syntax error must surface as ConfigParseException, not a silent pass");
    }

    @Test
    void wrongShapeForEnabledFieldThrows() {
        String json = """
                {
                  "checks": {
                    "EqualsChecker": { "enabled": "yes please" }
                  }
                }
                """;
        assertThrows(ConfigParseException.class, () -> loader.parse(json),
                "'enabled' must be a JSON boolean, not a string");
    }

    @Test
    void topLevelNotObjectThrows() {
        assertThrows(ConfigParseException.class,
                () -> loader.parse("[1, 2, 3]"));
    }

    @Test
    void loadReadsFromDisk(@TempDir Path tmp) throws IOException {
        Path file = tmp.resolve("linter.json");
        Files.writeString(file, """
                {
                  "checks": {
                    "EqualsChecker": { "enabled": false }
                  }
                }
                """);

        LinterConfig config = loader.load(file);

        assertFalse(config.isEnabled("EqualsChecker"));
    }

    @Test
    void loadRejectsNullPath() {
        assertThrows(IllegalArgumentException.class, () -> loader.load(null));
    }
}
