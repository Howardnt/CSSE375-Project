package rhit.csse.csse374.linter.data;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParsingException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads a LinterConfig from a JSON document.
 *
 * Schema (all keys optional):
 * <pre>
 *   {
 *     "defaultEnabled": true,
 *     "checks": {
 *       "EqualsChecker":        { "enabled": true },
 *       "MethodTooLongPattern": { "enabled": false, "options": { "threshold": "20" } }
 *     }
 *   }
 * </pre>
 *
 * Keys may use either the fully qualified class name or the simple name of a
 * check. Missing keys fall back to "defaultEnabled" (which itself defaults to
 * true). JSON parsing and file I/O are isolated here in the data layer; the
 * domain only sees the returned LinterConfig.
 */
public final class JsonLinterConfigLoader {

    public static final class ConfigParseException extends RuntimeException {
        public ConfigParseException(String message) {
            super(message);
        }

        public ConfigParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public LinterConfig load(Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        }
        String text = Files.readString(path);
        return parse(text);
    }

    public LinterConfig read(Reader reader) {
        if (reader == null) {
            throw new IllegalArgumentException("reader must not be null");
        }
        try (JsonReader r = Json.createReader(reader)) {
            return parseRoot(r.readObject());
        } catch (JsonParsingException e) {
            throw new ConfigParseException("Malformed JSON in linter config: " + e.getMessage(), e);
        } catch (ClassCastException e) {
            throw new ConfigParseException("Linter config must be a JSON object at the top level", e);
        }
    }

    public LinterConfig parse(String json) {
        if (json == null || json.isBlank()) {
            return new MapLinterConfig(Map.of(), Map.of(), true);
        }
        return read(new StringReader(json));
    }

    private LinterConfig parseRoot(JsonObject root) {
        boolean defaultEnabled = root.containsKey("defaultEnabled")
                ? readBoolean(root, "defaultEnabled")
                : true;

        Map<String, Boolean> enabledByKey = new HashMap<>();
        Map<String, Map<String, String>> optionsByKey = new HashMap<>();

        if (root.containsKey("checks")) {
            JsonObject checks = readObject(root, "checks");
            for (String id : checks.keySet()) {
                JsonObject entry = readObject(checks, id);
                if (entry.containsKey("enabled")) {
                    enabledByKey.put(id, readBoolean(entry, "enabled"));
                }
                if (entry.containsKey("options")) {
                    optionsByKey.put(id, readOptions(entry.getJsonObject("options")));
                }
            }
        }

        return new MapLinterConfig(enabledByKey, optionsByKey, defaultEnabled);
    }

    private static Map<String, String> readOptions(JsonObject o) {
        Map<String, String> opts = new HashMap<>();
        for (String k : o.keySet()) {
            opts.put(k, jsonValueToString(o.get(k)));
        }
        return opts;
    }

    private static boolean readBoolean(JsonObject parent, String key) {
        JsonValue v = parent.get(key);
        if (v == JsonValue.TRUE) {
            return true;
        }
        if (v == JsonValue.FALSE) {
            return false;
        }
        throw new ConfigParseException("\"" + key + "\" must be a JSON boolean");
    }

    private static JsonObject readObject(JsonObject parent, String key) {
        JsonValue v = parent.get(key);
        if (v == null || v.getValueType() != JsonValue.ValueType.OBJECT) {
            throw new ConfigParseException("\"" + key + "\" must be a JSON object");
        }
        return (JsonObject) v;
    }

    private static String jsonValueToString(JsonValue v) {
        return switch (v.getValueType()) {
            case STRING -> ((JsonString) v).getString();
            case TRUE -> "true";
            case FALSE -> "false";
            case NULL -> "";
            default -> v.toString();
        };
    }
}
