package fixtures;

/**
 * Fixture for {@code CamelCaseChecker} (lowerCamelCase method naming).
 *
 * PASS: method names start lowercase and contain no underscores.
 * FAIL: method names that start uppercase or contain underscores.
 */
public class CamelCaseCheckerFixture {

    // ===== PASS (should NOT be flagged) =====

    public void passLowerCamelCaseMethodName() {
        int x = 1;
        x += 2;
        System.out.println(x);
    }

    // ===== FAIL (should be flagged) =====

    // FAIL: starts with uppercase
    public void FailStartsWithUppercase() {
        System.out.println("bad name");
    }

    // FAIL: contains underscore
    public void fail_contains_underscore() {
        System.out.println("bad name");
    }
}

