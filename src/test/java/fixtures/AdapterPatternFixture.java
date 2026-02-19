package fixtures;

/**
 * Fixture for {@code AdapterPattern} detector.
 *
 * PASS: does not match the adapter heuristic (e.g., has multiple candidate fields).
 * FAIL: implements a target interface and delegates most methods to a single private final adaptee field.
 */
public class AdapterPatternFixture {
    // Container only.
}

interface TargetApi {
    int read();

    int write(int value);

    String info();
}

class Adaptee {
    private int value = 0;

    int get() {
        return value;
    }

    void set(int v) {
        value = v;
    }
}

// ===== FAIL (should be flagged) =====
class AdapterFailExample implements TargetApi {
    // FAIL: exactly one private final non-primitive field (the adaptee)
    private final Adaptee adaptee = new Adaptee();

    @Override
    public int read() {
        // Delegates to adaptee (GETFIELD)
        return adaptee.get();
    }

    @Override
    public int write(int value) {
        // Delegates to adaptee (GETFIELD)
        adaptee.set(value);
        return adaptee.get();
    }

    @Override
    public String info() {
        // Also touches the adaptee (GETFIELD)
        return "value=" + adaptee.get();
    }
}

// ===== PASS (should NOT be flagged) =====
class AdapterPassExample implements TargetApi {
    // PASS: two candidate fields -> heuristic should not match
    private final Adaptee a1 = new Adaptee();
    private final Adaptee a2 = new Adaptee();

    @Override
    public int read() {
        return a1.get() + a2.get();
    }

    @Override
    public int write(int value) {
        a1.set(value);
        a2.set(value);
        return a1.get();
    }

    @Override
    public String info() {
        return "sum=" + (a1.get() + a2.get());
    }
}

