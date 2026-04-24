package rhit.csse.csse374.linter.data;

import java.util.Collections;
import java.util.Map;

/**
 * Runtime configuration for a linter run: which checks are enabled, plus any
 * per-check key/value options that individual checks may read.
 *
 * Lives in the data layer so the domain layer can consume it via its existing
 * downward dependency on data. The interface is intentionally plain — no JSON
 * or file-format concepts leak through; producing instances of it is the job
 * of a loader (see JsonLinterConfigLoader).
 */
public interface LinterConfig {

    boolean isEnabled(String checkId);

    Map<String, String> optionsFor(String checkId);

    static LinterConfig allEnabled() {
        return new LinterConfig() {
            @Override
            public boolean isEnabled(String checkId) {
                return true;
            }

            @Override
            public Map<String, String> optionsFor(String checkId) {
                return Collections.emptyMap();
            }
        };
    }
}
