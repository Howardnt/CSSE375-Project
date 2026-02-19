package fixtures;

/**
 * Fixture for {@code TemplatePattern} (Template Method detector).
 *
 * PASS: abstract class without a concrete method that calls an abstract hook in the same class.
 * FAIL: abstract class with a concrete template method that calls an abstract hook method.
 */
public class TemplatePatternFixture {
    // Container only.
}

// ===== PASS (should NOT be flagged) =====
abstract class TemplatePassAbstractBase {
    // Abstract hook exists, but no concrete method calls it.
    abstract void hook();

    public void unrelatedConcreteMethod() {
        System.out.println("does not call hook");
    }
}

// ===== FAIL (should be flagged) =====
abstract class TemplateFailAbstractBase {
    abstract void step();

    // FAIL: concrete method calls an abstract method in the same class.
    public final void templateMethod() {
        System.out.println("before");
        step();
        System.out.println("after");
    }
}

