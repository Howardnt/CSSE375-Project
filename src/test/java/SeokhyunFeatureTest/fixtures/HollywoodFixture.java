package SeokhyunFeatureTest.fixtures;

//Jack Traversa (with Claude assistance in accordance with the requirements document)

/**
 * Fixture classes for testing Hollywood Principle detection.
 *
 * The Hollywood Principle says "Don't call us, we'll call you" —
 * low-level (concrete) classes should not excessively call back up
 * into their high-level (abstract/interface) dependencies.
 */
public class HollywoodFixture {

    // High-level interface with multiple methods
    public interface HighLevelService {
        void operationA();

        void operationB();

        void operationC();

        void operationD();
    }

    // BAD: Makes >= 3 distinct upward calls into the interface it implements.
    // Should trigger "excessive upward calls" violation.
    public static class BadLowLevel implements HighLevelService {

        @Override
        public void operationA() {
        }

        @Override
        public void operationB() {
        }

        @Override
        public void operationC() {
        }

        @Override
        public void operationD() {
        }

        // This method calls back up into the interface methods excessively
        public void doWork() {
            operationA();
            operationB();
            operationC();
            operationD();
        }
    }

    // GOOD: Makes fewer than 3 distinct upward calls.
    // Should NOT trigger a violation.
    public static class GoodLowLevel implements HighLevelService {

        @Override
        public void operationA() {
        }

        @Override
        public void operationB() {
        }

        @Override
        public void operationC() {
        }

        @Override
        public void operationD() {
        }

        // Only 1 upward call - under threshold
        public void doWork() {
            operationA();
        }
    }

    // Abstract superclass for instantiation test
    public static class ParentComponent {
        public void serve() {
            System.out.println("serving");
        }
    }

    // BAD: Concrete class that instantiates its own superclass.
    // Should trigger "upward instantiation" violation.
    public static class InstantiatesParent extends ParentComponent {
        public ParentComponent createAnother() {
            return new ParentComponent();
        }
    }
}
