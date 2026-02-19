package fixtures;

/**
 * Fixture for {@code StrategyPattern} (strategy-missing hotspot smell detector).
 *
 * PASS: behavior selection is done via polymorphism (no large switch/if-else chain).
 * FAIL: a large switch that selects behaviors and instantiates many different types.
 */
public class StrategyPatternFixture {

    public enum Mode {
        A, B, C, D, E
    }

    // ===== PASS (should NOT be flagged) =====
    // PASS: polymorphic dispatch through a Strategy interface (no large switch).
    public void passPolymorphicDispatch(Mode mode) {
        Strategy s = switch (mode) {
            case A -> new StrategyA();
            case B -> new StrategyB();
            case C -> new StrategyC();
            case D -> new StrategyD();
            case E -> new StrategyE();
        };
        s.run();
    }

    // ===== FAIL (should be flagged) =====
    // FAIL: switch-based behavior selection with many distinct types (hotspot smell).
    public void failSwitchHotspot(Mode mode) {
        switch (mode) {
            case A -> new StrategyA().run();
            case B -> new StrategyB().run();
            case C -> new StrategyC().run();
            case D -> new StrategyD().run();
            case E -> new StrategyE().run();
            default -> new StrategyA().run();
        }
    }

    interface Strategy {
        void run();
    }

    static class StrategyA implements Strategy {
        @Override public void run() { System.out.println("A"); }
    }
    static class StrategyB implements Strategy {
        @Override public void run() { System.out.println("B"); }
    }
    static class StrategyC implements Strategy {
        @Override public void run() { System.out.println("C"); }
    }
    static class StrategyD implements Strategy {
        @Override public void run() { System.out.println("D"); }
    }
    static class StrategyE implements Strategy {
        @Override public void run() { System.out.println("E"); }
    }
}

