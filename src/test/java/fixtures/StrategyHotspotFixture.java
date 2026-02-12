package fixtures;

/**
 * Fixtures for the `StrategyPattern` smell detector.
 *
 * The hotspot method intentionally uses a big switch and creates multiple different types,
 * which should trigger the heuristic (switchCount>=1 and newTypeCount>=3).
 */
public class StrategyHotspotFixture {

    public enum Mode {
        A, B, C, D, E
    }

    // Should be flagged: large switch-based behavior selection + many NEW types.
    public void process(Mode mode) {
        switch (mode) {
            case A:
                new HandlerA().run();
                break;
            case B:
                new HandlerB().run();
                break;
            case C:
                new HandlerC().run();
                break;
            case D:
                new HandlerD().run();
                break;
            case E:
                new HandlerE().run();
                break;
            default:
                new HandlerA().run();
        }
    }

    // Control: small switch that should not be flagged.
    public void smallSwitch(int value) {
        switch (value) {
            case 0:
                System.out.println("zero");
                break;
            case 1:
                System.out.println("one");
                break;
            default:
                System.out.println("other");
        }
    }

    interface Handler {
        void run();
    }

    static class HandlerA implements Handler {
        @Override public void run() { System.out.println("A"); }
    }
    static class HandlerB implements Handler {
        @Override public void run() { System.out.println("B"); }
    }
    static class HandlerC implements Handler {
        @Override public void run() { System.out.println("C"); }
    }
    static class HandlerD implements Handler {
        @Override public void run() { System.out.println("D"); }
    }
    static class HandlerE implements Handler {
        @Override public void run() { System.out.println("E"); }
    }
}

