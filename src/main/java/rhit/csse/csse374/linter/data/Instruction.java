package rhit.csse.csse374.linter.data;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

/**
 * Represents a single bytecode instruction with its analysis context.
 * Provides convenient methods for accessing stack values and instruction properties.
 */
public class Instruction {
    private final AbstractInsnNode instruction;
    private final Frame<BasicValue> frame;
    private final int index;

    public Instruction(AbstractInsnNode instruction, Frame<BasicValue> frame, int index) {
        this.instruction = instruction;
        this.frame = frame;
        this.index = index;
    }

    public int getOpcode() {
        return instruction.getOpcode();
    }

    public int getIndex() {
        return index;
    }

    public boolean isReferenceComparison() {
        int opcode = instruction.getOpcode();
        return opcode == Opcodes.IF_ACMPEQ || opcode == Opcodes.IF_ACMPNE;
    }

    public boolean isJumpInstruction() {
        return instruction instanceof JumpInsnNode;
    }

    public boolean hasFrameData() {
        return frame != null;
    }

    /**
     * Gets the value at a specific position on the stack (0 = top).
     * Returns null if frame data is unavailable or position is invalid.
     */
    public BasicValue getStackValue(int positionFromTop) {
        if (frame == null || frame.getStackSize() <= positionFromTop) {
            return null;
        }
        return frame.getStack(frame.getStackSize() - 1 - positionFromTop);
    }

    /**
     * Gets the top two stack values (typically used for comparison operations).
     * Returns null if insufficient stack depth.
     */
    public StackValuePair getTopTwoStackValues() {
        if (frame == null || frame.getStackSize() < 2) {
            return null;
        }
        BasicValue top = frame.getStack(frame.getStackSize() - 1);
        BasicValue second = frame.getStack(frame.getStackSize() - 2);
        return new StackValuePair(top, second);
    }

    // Keep access to raw instruction for advanced use cases
    public AbstractInsnNode getInstruction() {
        return instruction;
    }

    public Frame<BasicValue> getFrame() {
        return frame;
    }
}