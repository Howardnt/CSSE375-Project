package SeokhyunFeatureTest.fixtures;

//Jack Traversa (with Claude assistance in accordance with the requirements document)

/**
 * Fixture classes for testing Template Method pattern detection.
 *
 * AbstractGame is a classic Template Method: a concrete method (play) that
 * calls abstract methods (initialize, startPlay, endPlay).
 *
 * NotATemplate is an abstract class with NO concrete method calling its
 * own abstract methods, so it should NOT be detected.
 */
public class TemplatePatternFixture {

    // SHOULD be detected as Template Method Pattern:
    // play() is concrete and calls abstract methods initialize(), startPlay(),
    // endPlay()
    public static abstract class AbstractGame {

        // Template method - concrete, calls abstract steps
        public final void play() {
            initialize();
            startPlay();
            endPlay();
        }

        abstract void initialize();

        abstract void startPlay();

        abstract void endPlay();
    }

    // Concrete subclass - should NOT itself be detected (not abstract)
    public static class Cricket extends AbstractGame {
        @Override
        void initialize() {
            System.out.println("Cricket: gathering players");
        }

        @Override
        void startPlay() {
            System.out.println("Cricket: game started");
        }

        @Override
        void endPlay() {
            System.out.println("Cricket: game over");
        }
    }

    // Abstract class with NO template method - should NOT be detected
    public static abstract class NotATemplate {
        abstract void doSomething();

        abstract void doSomethingElse();

        // This concrete method does NOT call any abstract methods of this class
        public void helper() {
            System.out.println("Just a helper");
        }
    }
}
