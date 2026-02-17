package fixtures;

import java.util.ArrayList;
import java.util.List;

/**
 * Control fixture: should NOT be flagged as an SRP violation by `singleResponsibilityPrinciple`.
 *
 * Small number of fields and cohesive methods that work with the same state.
 */
public class SrpCohesiveFixture {

    private final List<String> items = new ArrayList<>();
    private int count = 0;

    public void addItem(String item) {
        items.add(item);
        count++;
    }

    public void removeItem(String item) {
        if (items.remove(item)) {
            count--;
        }
    }

    public int size() {
        return count;
    }

    public boolean contains(String item) {
        return items.contains(item);
    }
}

