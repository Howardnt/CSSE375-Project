package rhit.csse.csse374.linter.data;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single method with pre-analyzed frame data.
 * Performs ASM analysis once and caches the results for efficient access.
 * Runs both BasicInterpreter (always succeeds, no type info) and SimpleVerifier
 * (may fail on external classes, but provides rich type info when it succeeds).
 */
public class ASMMethod {
    private final String className;
    private final String methodName;
    private final MethodNode methodNode;

    // Basic analysis - always succeeds, no type info
    private final Frame<BasicValue>[] basicFrames;
    private final List<Instruction> basicInstructions;
    private final boolean basicAnalysisSucceeded;

    // Simple analysis - may fail, but provides type info
    private final Frame<BasicValue>[] simpleFrames;
    private final List<Instruction> simpleInstructions;
    private final boolean simpleAnalysisSucceeded;

    /**CODE REFACTORED: Ervin 4/3/2026 - Extract Method for Duplicate Code
    Extracted performAnalysis() to eliminate the two nearly identical
    analysis blocks for BasicInterpreter and SimpleVerifier.
    **/
    public ASMMethod(String className, MethodNode methodNode) {
        this.className = className;
        this.methodName = methodNode.name;
        this.methodNode = methodNode;

        // Basic analysis (always succeeds, no type info)
        AnalysisResult basicResult = performAnalysis(new BasicInterpreter(), className, methodNode);
        this.basicFrames = basicResult.frames;
        this.basicAnalysisSucceeded = basicResult.succeeded;
        this.basicInstructions = buildInstructions(methodNode, basicResult.frames);

        // Simple analysis (may fail, but provides type info)
        AnalysisResult simpleResult = performAnalysis(new SimpleVerifier(), className, methodNode);
        this.simpleFrames = simpleResult.frames;
        this.simpleAnalysisSucceeded = simpleResult.succeeded;
        this.simpleInstructions = buildInstructions(methodNode, simpleResult.frames);
    }

    private static class AnalysisResult {
        final Frame<BasicValue>[] frames;
        final boolean succeeded;

        AnalysisResult(Frame<BasicValue>[] frames, boolean succeeded) {
            this.frames = frames;
            this.succeeded = succeeded;
        }
    }

    private AnalysisResult performAnalysis(Interpreter<BasicValue> interpreter,
            String className, MethodNode methodNode) {
        try {
            Analyzer<BasicValue> analyzer = new Analyzer<>(interpreter);
            Frame<BasicValue>[] frames = analyzer.analyze(className, methodNode);
            return new AnalysisResult(frames, true);
        } catch (AnalyzerException e) {
            return new AnalysisResult(null, false);
        }
    }

    private List<Instruction> buildInstructions(MethodNode methodNode, Frame<BasicValue>[] frames) {
        List<Instruction> instructions = new ArrayList<>();
        AbstractInsnNode[] insnArray = methodNode.instructions.toArray();
        for (int i = 0; i < insnArray.length; i++) {
            Frame<BasicValue> frame = (frames != null && i < frames.length) ? frames[i] : null;
            instructions.add(new Instruction(insnArray[i], frame, i));
        }
        return instructions;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getFullMethodName() {
        return className + "." + methodName;
    }

    /** Returns instructions backed by BasicInterpreter frames (always available). */
    public List<Instruction> getInstructions() {
        return Collections.unmodifiableList(basicInstructions);
    }

    /** Returns instructions backed by SimpleVerifier frames (type info available, may be empty on failure). */
    public List<Instruction> getInstructionsWithTypeInfo() {
        return Collections.unmodifiableList(simpleInstructions);
    }

    /** True if BasicInterpreter analysis succeeded (almost always true). */
    public boolean isAnalysisSuccessful() {
        return basicAnalysisSucceeded;
    }

    /** True if SimpleVerifier analysis succeeded (type info is available). */
    public boolean isTypeInfoAvailable() {
        return simpleAnalysisSucceeded;
    }

    public MethodNode getMethodNode() {
        return methodNode;
    }

    public Frame<BasicValue>[] getFrames() {
        return basicFrames;
    }

    public Frame<BasicValue>[] getSimpleFrames() {
        return simpleFrames;
    }
}