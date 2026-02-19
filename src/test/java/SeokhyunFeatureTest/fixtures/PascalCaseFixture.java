package SeokhyunFeatureTest.fixtures;

//Jack Traversa (with Claude assistance in accordance with the requirements document)

/**
 * Fixture classes for testing PascalCase cursory check.
 * Contains inner classes with valid PascalCase names.
 */
public class PascalCaseFixture {

    // GOOD: Valid PascalCase name - should NOT be flagged
    public static class GoodClassName {
        public void doSomething() {
        }
    }

    // GOOD: Short all-caps (<=3 chars) allowed as acronym - should NOT be flagged
    public static class URL {
        public void fetch() {
        }
    }

    // GOOD: Another valid PascalCase name
    public static class MyHttpClient {
        public void connect() {
        }
    }
}
