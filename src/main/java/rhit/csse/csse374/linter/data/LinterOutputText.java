package rhit.csse.csse374.linter.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data-layer object representing the final linter report.
 *
 * For the skeleton, this is a simple list of human-readable lines.
 * Later, this can evolve into a richer model (severity levels, file/line locations, JSON export, etc.).
 */
public class LinterOutputText {

    private final List<String> lines = new ArrayList<>();

    public void addLine(String line) {
        lines.add(line);
    }

    public List<String> getLines() {
        return Collections.unmodifiableList(lines);
    }

    @Override
    public String toString() {
        if (lines.isEmpty()) {
            return "(no findings yet — skeleton output)";
        }
        return String.join(System.lineSeparator(), lines);
    }
}

