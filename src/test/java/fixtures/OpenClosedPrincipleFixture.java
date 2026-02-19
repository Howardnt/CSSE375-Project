package fixtures;

/**
 * Fixture for {@code OpenClosedPrinciple}.
 *
 * PASS: no excessive type-checking (instanceof/switch) in methods.
 * FAIL:
 * - method with 3+ instanceof checks (type-checking hotspot)
 * - rigid final class that does type-branching (hard to extend without modification)
 */
public class OpenClosedPrincipleFixture {
    // Container class only.
}

// ===== PASS (should NOT be flagged) =====
class OcpPassExample {
    public int computeTotal(int a, int b) {
        int sum = a + b;
        if (sum < 0) {
            sum = 0;
        }
        return sum;
    }
}

// ===== FAIL (should be flagged) =====
class OcpFailTypeChecksExample {
    public String describe(Object o) {
        // FAIL: multiple instanceof checks are a common OCP smell.
        if (o instanceof String) {
            return "string";
        }
        if (o instanceof Integer) {
            return "integer";
        }
        if (o instanceof java.util.List) {
            return "list";
        }
        return "other";
    }
}

// FAIL: final + type-branching logic makes extension hard without modifying this class.
final class OcpFailRigidFinalExample {
    public int doThing(Object o) {
        if (o instanceof String) {
            return 1;
        }
        if (o instanceof Number) {
            return 2;
        }
        return 0;
    }
}

