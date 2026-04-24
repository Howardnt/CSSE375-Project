package unit;

import org.junit.jupiter.api.Test;

import rhit.csse.csse374.linter.data.LinterConfig;
import rhit.csse.csse374.linter.data.MapLinterConfig;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MapLinterConfig.
 *
 * These exercise the in-memory LinterConfig directly (no JSON involved) so
 * lookup rules (simple vs fully qualified name, defaults, null-safety) can be
 * verified without a parser in the loop.
 */
public class MapLinterConfigTest {

    @Test
    void fullyQualifiedKeyMatchesExactly() {
        Map<String, Boolean> enabled = new HashMap<>();
        enabled.put("rhit.csse.csse374.linter.domain.EqualsChecker", false);
        MapLinterConfig config = new MapLinterConfig(enabled, Map.of(), true);

        assertFalse(config.isEnabled("rhit.csse.csse374.linter.domain.EqualsChecker"));
        assertTrue(config.isEnabled("rhit.csse.csse374.linter.domain.CamelCaseChecker"),
                "Unlisted checks should fall back to defaultEnabled=true");
    }

    @Test
    void simpleNameKeyMatchesFullyQualifiedLookup() {
        Map<String, Boolean> enabled = new HashMap<>();
        enabled.put("EqualsChecker", false);
        MapLinterConfig config = new MapLinterConfig(enabled, Map.of(), true);

        assertFalse(config.isEnabled("rhit.csse.csse374.linter.domain.EqualsChecker"),
                "Simple-name keys in the config should match a fully qualified check id");
    }

    @Test
    void defaultEnabledGovernsUnlistedChecks() {
        MapLinterConfig allOff = new MapLinterConfig(Map.of(), Map.of(), false);
        assertFalse(allOff.isEnabled("AnythingAtAll"));

        MapLinterConfig allOn = new MapLinterConfig(Map.of(), Map.of(), true);
        assertTrue(allOn.isEnabled("AnythingAtAll"));
    }

    @Test
    void nullCheckIdYieldsDefaultEnabled() {
        MapLinterConfig off = new MapLinterConfig(Map.of(), Map.of(), false);
        assertFalse(off.isEnabled(null));
        MapLinterConfig on = new MapLinterConfig(Map.of(), Map.of(), true);
        assertTrue(on.isEnabled(null));
    }

    @Test
    void optionsLookupFallsBackToSimpleName() {
        Map<String, Map<String, String>> options = new HashMap<>();
        options.put("MethodTooLongPattern", Map.of("threshold", "20"));
        MapLinterConfig config = new MapLinterConfig(Map.of(), options, true);

        assertEquals("20",
                config.optionsFor("rhit.csse.csse374.linter.domain.MethodTooLongPattern").get("threshold"));
    }

    @Test
    void optionsForUnknownCheckIsEmpty() {
        MapLinterConfig config = new MapLinterConfig(Map.of(), Map.of(), true);
        assertTrue(config.optionsFor("Missing").isEmpty());
        assertTrue(config.optionsFor(null).isEmpty());
    }

    @Test
    void optionsMapIsUnmodifiableToCallers() {
        Map<String, Map<String, String>> options = new HashMap<>();
        options.put("X", new HashMap<>(Map.of("k", "v")));
        MapLinterConfig config = new MapLinterConfig(Map.of(), options, true);

        assertThrows(UnsupportedOperationException.class,
                () -> config.optionsFor("X").put("sneaky", "value"),
                "Returned options map must not allow external mutation");
    }

    @Test
    void allEnabledFactoryAlwaysReturnsTrue() {
        LinterConfig config = LinterConfig.allEnabled();
        assertTrue(config.isEnabled("Anything"));
        assertTrue(config.optionsFor("Anything").isEmpty());
    }
}
