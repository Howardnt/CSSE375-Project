package fixtures;

import java.util.ArrayList;
import java.util.List;

/**
 * Fixture for {@code EqualsChecker} (aka the equals-operator-on-reference-types check).
 *
 * PASS section: uses of {@code ==} that are legitimate (null checks, primitives).
 * FAIL section: uses of {@code ==} on reference types like {@link String}, wrappers, and collections.
 */
public class EqualsCheckerFixture {

    // ===== PASS (should NOT be flagged) =====

    /** PASS: null checks are correct with {@code ==}. */
    public boolean passNullCheck(String s) {
        return s == null;
    }

    /** PASS: primitives are correct with {@code ==}. */
    public boolean passPrimitiveCompare(int a, int b) {
        return a == b;
    }

    /** PASS: boolean primitives are correct with {@code ==}. */
    public boolean passPrimitiveBooleanCompare(boolean a, boolean b) {
        return a == b;
    }

    // ===== FAIL (should be flagged) =====

    /** FAIL: reference comparisons of Strings should typically use {@code .equals()}. */
    public boolean failStringCompare(String a, String b) {
        return a == b;
    }

    /** FAIL: wrapper types (boxed primitives) should typically use {@code .equals()}. */
    public boolean failIntegerWrapperCompare(Integer a, Integer b) {
        return a == b;
    }

    /** FAIL: collections should typically use {@code .equals()} (unless checking identity intentionally). */
    public boolean failListCompare(List<String> a, List<String> b) {
        return a == b;
    }

    /** FAIL: concrete collection types still compare by identity with {@code ==}. */
    public boolean failArrayListCompare(ArrayList<String> a, ArrayList<String> b) {
        return a == b;
    }
}

