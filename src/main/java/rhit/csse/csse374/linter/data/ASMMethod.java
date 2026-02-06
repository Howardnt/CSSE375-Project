package rhit.csse.csse374.linter.data;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SimpleVerifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single method with pre-analyzed frame data.
 * Performs ASM analysis once and caches the results for efficient access.
 */
public class ASMMethod {
    private final String className;
    private final String methodName;
    private final MethodNode methodNode;
    private final Frame<BasicValue>[] frames;
    private final List<Instruction> instructions;
    private final boolean analysisSucceeded;
    private final String analysisError;

    public ASMMethod(String className, MethodNode methodNode) {
        this.className = className;
        this.methodName = methodNode.name;
        this.methodNode = methodNode;

        Frame<BasicValue>[] analyzedFrames = null;
        String error = null;
        boolean succeeded = false;

        try {
            SimpleVerifier verifier = new SimpleVerifier();
            Analyzer<BasicValue> analyzer = new Analyzer<>(verifier);
            analyzedFrames = analyzer.analyze(className, methodNode);
            succeeded = true;
        } catch (AnalyzerException e) {
            error = "Analysis failed: " + e.getMessage();
        }

        this.frames = analyzedFrames;
        this.analysisSucceeded = succeeded;
        this.analysisError = error;

        // Pre-process instructions with their frames
        this.instructions = new ArrayList<>();
        AbstractInsnNode[] insnArray = methodNode.instructions.toArray();
        for (int i = 0; i < insnArray.length; i++) {
            Frame<BasicValue> frame = (analyzedFrames != null && i < analyzedFrames.length)
                    ? analyzedFrames[i]
                    : null;
            this.instructions.add(new Instruction(insnArray[i], frame, i));
        }
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

    public List<Instruction> getInstructions() {
        return Collections.unmodifiableList(instructions);
    }

    public boolean isAnalysisSuccessful() {
        return analysisSucceeded;
    }

    public String getAnalysisError() {
        return analysisError;
    }

    // Keep access to raw data for advanced use cases
    public MethodNode getMethodNode() {
        return methodNode;
    }

    public Frame<BasicValue>[] getFrames() {
        return frames;
    }
}