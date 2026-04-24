package rhit.csse.csse374.linter.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default LinterConfig backed by two maps (enabled flags + per-check options).
 *
 * Keys may be either the fully qualified class name of a check or its simple
 * name — a lookup on a fully qualified id transparently falls back to the
 * simple name, so user-authored configs can stay short.
 */
public final class MapLinterConfig implements LinterConfig {

    private final Map<String, Boolean> enabledByKey;
    private final Map<String, Map<String, String>> optionsByKey;
    private final boolean defaultEnabled;

    public MapLinterConfig(
            Map<String, Boolean> enabledByKey,
            Map<String, Map<String, String>> optionsByKey,
            boolean defaultEnabled) {
        this.enabledByKey = (enabledByKey == null) ? Map.of() : new HashMap<>(enabledByKey);
        this.optionsByKey = deepCopy(optionsByKey);
        this.defaultEnabled = defaultEnabled;
    }

    @Override
    public boolean isEnabled(String checkId) {
        if (checkId == null) {
            return defaultEnabled;
        }
        if (enabledByKey.containsKey(checkId)) {
            return enabledByKey.get(checkId);
        }
        String simple = simpleName(checkId);
        if (enabledByKey.containsKey(simple)) {
            return enabledByKey.get(simple);
        }
        return defaultEnabled;
    }

    @Override
    public Map<String, String> optionsFor(String checkId) {
        if (checkId == null) {
            return Collections.emptyMap();
        }
        Map<String, String> direct = optionsByKey.get(checkId);
        if (direct != null) {
            return Collections.unmodifiableMap(direct);
        }
        Map<String, String> bySimple = optionsByKey.get(simpleName(checkId));
        if (bySimple != null) {
            return Collections.unmodifiableMap(bySimple);
        }
        return Collections.emptyMap();
    }

    public boolean defaultEnabled() {
        return defaultEnabled;
    }

    private static String simpleName(String fqName) {
        int idx = fqName.lastIndexOf('.');
        return (idx >= 0) ? fqName.substring(idx + 1) : fqName;
    }

    private static Map<String, Map<String, String>> deepCopy(
            Map<String, Map<String, String>> src) {
        if (src == null) {
            return new HashMap<>();
        }
        Map<String, Map<String, String>> out = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> e : src.entrySet()) {
            out.put(e.getKey(), new HashMap<>(e.getValue()));
        }
        return out;
    }
}
