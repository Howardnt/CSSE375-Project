package fixtures;

/**
 * Fixtures for the `MethodTooLongPattern` check:
 * - Too many parameters (>5)
 * - Method too long (>40 distinct line numbers)
 * - A control method that should not be flagged
 */
public class MethodLengthAndParamsFixture {

    // Should be flagged: >5 parameters
    public void tooManyParams(int a, int b, int c, int d, int e, int f) {
        int sum = a + b + c + d + e + f;
        System.out.println(sum);
    }

    // Should be flagged: long method by line count (keep each statement on its own line).
    public void veryLongMethod() {
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

    // Control: should not be flagged.
    public void shortAndFewParams(int value) {
        System.out.println(value);
    }
}

