package fixtures;

/**
 * Fixture for {@code InterfaceSegregationPrinciple}.
 *
 * The ISP checker flags classes that implement interfaces but contain multiple "empty/dummy" methods,
 * e.g., methods that throw UnsupportedOperationException or are otherwise effectively empty.
 *
 * PASS: implements an interface and provides meaningful implementations.
 * FAIL: implements an interface but uses dummy methods (throws UnsupportedOperationException).
 */
public class InterfaceSegregationPrincipleFixture {
    // Container class only.
}

interface TwoMethodService {
    int compute(int a, int b);

    String format(int value);
}

// ===== PASS (should NOT be flagged) =====
class IspPassExample implements TwoMethodService {
    @Override
    public int compute(int a, int b) {
        int sum = a + b;
        // Add a tiny bit of logic to avoid "too small" methods.
        if (sum < 0) {
            sum = -sum;
        }
        return sum;
    }

    @Override
    public String format(int value) {
        String s = "value=" + value;
        if (value % 2 == 0) {
            s = s + " (even)";
        } else {
            s = s + " (odd)";
        }
        return s;
    }
}

// ===== FAIL (should be flagged) =====
class IspFailExample implements TwoMethodService {
    @Override
    public int compute(int a, int b) {
        // FAIL: dummy method
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String format(int value) {
        // FAIL: dummy method
        throw new UnsupportedOperationException("Not supported");
    }
}

