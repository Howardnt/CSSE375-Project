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

// ===== Inner-class handling fixture =====
// Compiles to:
//   GoodOuterClass.class
//   GoodOuterClass$GoodInner.class
//   GoodOuterClass$1.class            (anonymous inner)
// All three should produce zero violations — inner-class files must
// not double-flag the outer.
class GoodOuterClass {
    static class GoodInner {
        int value = 1;
    }

    Runnable anonymous = new Runnable() {
        @Override
        public void run() {
            //noop
        }
    };
}

