package fixtures;

/**
 * Fixture for {@code DecoratorPattern} detector.
 *
 * PASS: implements a component interface but does not hold a component-typed field.
 * FAIL: implements a component interface and holds a field of that same component type (wraps another component).
 */
public class DecoratorPatternFixture {
    // Container only.
}

interface DecComponent {
    String render();
}

// ===== PASS (should NOT be flagged) =====
class DecoratorPassExample implements DecComponent {
    private final String value = "ok";

    @Override
    public String render() {
        return value;
    }
}

// ===== FAIL (should be flagged) =====
class DecoratorFailExample implements DecComponent {
    // FAIL: holds a field whose type matches an implemented interface (typical Decorator structure).
    private final DecComponent inner;

    DecoratorFailExample(DecComponent inner) {
        this.inner = inner;
    }

    @Override
    public String render() {
        return "decorated(" + inner.render() + ")";
    }
}

