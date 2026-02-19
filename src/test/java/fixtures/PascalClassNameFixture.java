package fixtures;

/**
 * Fixture for {@code PascalClassName} (PascalCase class naming check).
 *
 * PASS: classes that start with an uppercase letter (PascalCase).
 * FAIL: a class that starts with a lowercase letter.
 */
public class PascalClassNameFixture {
    // This class is intentionally PascalCase and should PASS.
}

// ===== PASS (should NOT be flagged) =====
class GoodPascalCaseExample {
    int value = 42;
}

// ===== FAIL (should be flagged) =====
// FAIL: class name starts with lowercase 'b'
class badPascalCaseExample {
    int value = 13;
}

