package fixtures;

/**
 * Fixture for {@code MethodTooLongPattern}.
 *
 * PASS: a short method with a small number of parameters.
 * FAIL:
 * - a method with too many parameters (> 5)
 * - a method that is too long (> 40 distinct line numbers)
 */
public class MethodTooLongPatternFixture {

    // ===== PASS (should NOT be flagged) =====

    public void passShortAndFewParams(int value) {
        int y = value + 1;
        System.out.println(y);
    }

    // ===== FAIL (should be flagged) =====

    // FAIL: > 5 parameters
    public void failTooManyParams(int a, int b, int c, int d, int e, int f) {
        int sum = a + b + c + d + e + f;
        System.out.println(sum);
    }

    // FAIL: long method by line count (keep each statement on its own line).
    public void failVeryLongMethod() {
        int x = 0;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        x++;
        System.out.println(x);
    }
}

