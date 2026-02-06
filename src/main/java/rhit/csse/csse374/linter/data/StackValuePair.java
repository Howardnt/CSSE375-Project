package rhit.csse.csse374.linter.data;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.BasicValue;

/**
 * Helper class to represent a pair of stack values.
 * Commonly used for analyzing comparison operations.
 */
public class StackValuePair {
    private final BasicValue first;
    private final BasicValue second;

    public StackValuePair(BasicValue first, BasicValue second) {
        this.first = first;
        this.second = second;
    }

    public BasicValue getFirst() {
        return first;
    }

    public BasicValue getSecond() {
        return second;
    }

    public boolean eitherIsNull() {
        return isNullValue(first) || isNullValue(second);
    }

    public boolean bothHaveType() {
        return first != null && first.getType() != null
                && second != null && second.getType() != null;
    }

    public Type getFirstType() {
        return first != null ? first.getType() : null;
    }

    public Type getSecondType() {
        return second != null ? second.getType() : null;
    }

    private boolean isNullValue(BasicValue value) {
        return value == BasicValue.UNINITIALIZED_VALUE
                || value == null
                || value.getType() == null
                || (value.getType() != null && value.getType().getSort() == Type.VOID);
    }
}