# Milestone 2 Submission
**Project:** Java Design Linter
**Team:** Ervin Perkowski + Noah Howard
**Date:** April 3, 2026
**Branch:** `milestone2`

---

## Q1: Refactorings

### Refactoring 1: Extract Method — `CohesionAnalyzer.calculateLCOM4()` (Ervin)
- **Code Smell:** Long Method (87 lines, 3 distinct responsibilities)
- **Refactoring Applied:** Extract Function (Fowler, Ch. 6)
- **Commit:** `aebd007`
- **File:** `src/main/java/rhit/csse/csse374/linter/domain/CohesionAnalyzer.java`
- **What changed:** Split `calculateLCOM4()` into three focused methods:
  1. `extractMethodFieldUsage()` — iterates class methods and builds maps of which fields each method accesses and which other methods it calls
  2. `buildConnectivityGraph()` — creates an adjacency graph where methods are connected if they share a field or call each other
  3. `countConnectedComponents()` — BFS traversal to count connected components (the LCOM4 score)
- **Why:** The original method had three clearly separable phases of computation jammed into one method, making it hard to read and test independently. Each extracted method now has a single responsibility and a descriptive name.
- **Test:** `CohesionAnalyzerTest.java` — verifies LCOM4 scores match expected values for fixture classes

**Before:**
```java
private int calculateLCOM4(ASMClass asmClass) {
    List<String> validMethods = new ArrayList<>();
    Map<String, Set<String>> methodToFields = new HashMap<>();
    Map<String, Set<String>> methodToMethods = new HashMap<>();
    String ownerClass = asmClass.getClassName();

    // Phase 1: Build method-field and method-method usage maps
    for (ASMMethod method : asmClass.getMethods()) {
        String methodName = method.getMethodName();
        if (methodName.equals("<init>") || methodName.equals("<clinit>")) continue;
        validMethods.add(methodName);
        methodToFields.putIfAbsent(methodName, new HashSet<>());
        methodToMethods.putIfAbsent(methodName, new HashSet<>());
        for (Instruction instruction : method.getInstructions()) {
            AbstractInsnNode insnNode = instruction.getInstruction();
            if (insnNode instanceof FieldInsnNode) {
                FieldInsnNode fieldNode = (FieldInsnNode) insnNode;
                if (fieldNode.owner.equals(ownerClass)) {
                    methodToFields.get(methodName).add(fieldNode.name);
                }
            }
            if (insnNode instanceof MethodInsnNode) {
                MethodInsnNode methodCallNode = (MethodInsnNode) insnNode;
                if (methodCallNode.owner.equals(ownerClass)) {
                    methodToMethods.get(methodName).add(methodCallNode.name);
                }
            }
        }
    }

    if (validMethods.isEmpty()) return 1;

    // Phase 2: Build connectivity graph
    Map<String, Set<String>> graph = new HashMap<>();
    for (String m : validMethods) graph.put(m, new HashSet<>());
    for (int i = 0; i < validMethods.size(); i++) {
        for (int j = i + 1; j < validMethods.size(); j++) {
            String m1 = validMethods.get(i);
            String m2 = validMethods.get(j);
            boolean edgeExists = methodToMethods.get(m1).contains(m2)
                              || methodToMethods.get(m2).contains(m1);
            if (!edgeExists) {
                Set<String> sharedFields = new HashSet<>(methodToFields.get(m1));
                sharedFields.retainAll(methodToFields.get(m2));
                if (!sharedFields.isEmpty()) edgeExists = true;
            }
            if (edgeExists) {
                graph.get(m1).add(m2);
                graph.get(m2).add(m1);
            }
        }
    }

    // Phase 3: Count connected components via BFS
    int components = 0;
    Set<String> visited = new HashSet<>();
    for (String method : validMethods) {
        if (!visited.contains(method)) {
            components++;
            Queue<String> queue = new LinkedList<>();
            queue.add(method);
            visited.add(method);
            while (!queue.isEmpty()) {
                String current = queue.poll();
                for (String neighbor : graph.get(current)) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }
    }
    return components;
}
```

**After:**
```java
private int calculateLCOM4(ASMClass asmClass) {
    List<String> validMethods = new ArrayList<>();
    Map<String, Set<String>> methodToFields = new HashMap<>();
    Map<String, Set<String>> methodToMethods = new HashMap<>();

    extractMethodFieldUsage(asmClass, validMethods, methodToFields, methodToMethods);

    if (validMethods.isEmpty()) return 1;

    Map<String, Set<String>> graph = buildConnectivityGraph(validMethods, methodToFields, methodToMethods);

    return countConnectedComponents(validMethods, graph);
}

private void extractMethodFieldUsage(ASMClass asmClass, List<String> validMethods,
        Map<String, Set<String>> methodToFields, Map<String, Set<String>> methodToMethods) {
    // ... Phase 1 logic extracted here
}

private Map<String, Set<String>> buildConnectivityGraph(List<String> validMethods,
        Map<String, Set<String>> methodToFields, Map<String, Set<String>> methodToMethods) {
    // ... Phase 2 logic extracted here
}

private int countConnectedComponents(List<String> validMethods, Map<String, Set<String>> graph) {
    // ... Phase 3 logic extracted here
}
```

### Refactoring 2: Extract Method — `ASMMethod` Duplicate Analysis Blocks (Ervin)
- **Code Smell:** Duplicate Code (two nearly identical try/catch analysis blocks)
- **Refactoring Applied:** Extract Function (Fowler, Ch. 6)
- **Commit:** `6f044d3`
- **File:** `src/main/java/rhit/csse/csse374/linter/data/ASMMethod.java`
- **What changed:** The constructor had two copy-pasted blocks that each created an `Analyzer`, called `analyze()`, caught `AnalyzerException`, and assigned results to fields. The only difference was the `Interpreter` type (`BasicInterpreter` vs `SimpleVerifier`). Extracted:
  1. `performAnalysis(Interpreter, String, MethodNode)` — generic analysis method that works with any interpreter
  2. `AnalysisResult` inner class — holds the frames array and success boolean as a return value
- **Why:** The two blocks were identical except for the interpreter argument. Any future bug fix or change would need to be applied in two places. The extracted method eliminates this duplication.
- **Test:** `ASMMethodTest.java` — 6 tests verifying analysis succeeds, instructions are non-null, names are correct

**Before:**
```java
public ASMMethod(String className, MethodNode methodNode) {
    this.className = className;
    this.methodName = methodNode.name;
    this.methodNode = methodNode;

    // Basic analysis (copy-pasted block 1)
    Frame<BasicValue>[] basicFramesTemp = null;
    boolean basicSucceeded = false;
    try {
        Analyzer<BasicValue> analyzer = new Analyzer<>(new BasicInterpreter());
        basicFramesTemp = analyzer.analyze(className, methodNode);
        basicSucceeded = true;
    } catch (AnalyzerException e) {
        basicFramesTemp = null;
        basicSucceeded = false;
    }
    this.basicFrames = basicFramesTemp;
    this.basicAnalysisSucceeded = basicSucceeded;
    this.basicInstructions = buildInstructions(methodNode, basicFramesTemp);

    // Simple analysis (copy-pasted block 2 — nearly identical)
    Frame<BasicValue>[] simpleFramesTemp = null;
    boolean simpleSucceeded = false;
    try {
        Analyzer<BasicValue> analyzer = new Analyzer<>(new SimpleVerifier());
        simpleFramesTemp = analyzer.analyze(className, methodNode);
        simpleSucceeded = true;
    } catch (AnalyzerException e) {
        simpleFramesTemp = null;
        simpleSucceeded = false;
    }
    this.simpleFrames = simpleFramesTemp;
    this.simpleAnalysisSucceeded = simpleSucceeded;
    this.simpleInstructions = buildInstructions(methodNode, simpleFramesTemp);
}
```

**After:**
```java
public ASMMethod(String className, MethodNode methodNode) {
    this.className = className;
    this.methodName = methodNode.name;
    this.methodNode = methodNode;

    AnalysisResult basicResult = performAnalysis(new BasicInterpreter(), className, methodNode);
    this.basicFrames = basicResult.frames;
    this.basicAnalysisSucceeded = basicResult.succeeded;
    this.basicInstructions = buildInstructions(methodNode, basicResult.frames);

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
```

### Refactoring 3: Move Method — `CohesionAnalyzer` Feature Envy (Ervin)
- **Code Smell:** Feature Envy (`CohesionAnalyzer` accessed `ASMClass` internals to check interface/abstract)
- **Refactoring Applied:** Move Function (Fowler, Ch. 8)
- **Commit:** `a2e261c`
- **Files:**
  - `src/main/java/rhit/csse/csse374/linter/domain/CohesionAnalyzer.java`
  - `src/main/java/rhit/csse/csse374/linter/data/ASMClass.java`
- **What changed:** Moved the `isInterface()` and `isAbstract()` logic from `CohesionAnalyzer` into `ASMClass`, where the data lives. `CohesionAnalyzer` now calls `cls.isInterface()` and `cls.isAbstract()` instead of reaching into `ClassNode` internals.
- **Why:** The original code was a textbook Feature Envy smell — `CohesionAnalyzer` was doing bit-masking on `ASMClass`'s internal `ClassNode.access` field. The class that owns the data should own the behavior.
- **Test:** `ASMClassTest.java` — verifies `isInterface()` and `isAbstract()` return correct values for different class types

**Before (in CohesionAnalyzer.java):**
```java
@Override
public List<Violation> checkClass(ASMClass cls) {
    List<Violation> violations = new ArrayList<>();

    // Feature Envy: reaching into ASMClass internals to check access flags
    int access = cls.getClassNode().access;
    if ((access & Opcodes.ACC_INTERFACE) != 0 || (access & Opcodes.ACC_ABSTRACT) != 0) {
        return violations;
    }

    int lcom4Score = calculateLCOM4(cls);
    // ...
}
```

**After (in CohesionAnalyzer.java):**
```java
@Override
public List<Violation> checkClass(ASMClass cls) {
    List<Violation> violations = new ArrayList<>();

    // Now delegates to ASMClass — the class that owns the data
    if (cls.isInterface() || cls.isAbstract()) {
        return violations;
    }

    int lcom4Score = calculateLCOM4(cls);
    // ...
}
```

**After (new methods in ASMClass.java):**
```java
public boolean isInterface() {
    return (classNode.access & Opcodes.ACC_INTERFACE) != 0;
}

public boolean isAbstract() {
    return (classNode.access & Opcodes.ACC_ABSTRACT) != 0;
}
```

### Refactoring 4: Extract Method — `LinterGuiFrame.onRun()` (Noah)
<!-- Noah: fill in details here -->

### Refactoring 5: Pull Up Method — Hollywood Strategy Duplicate Logic (Noah)
<!-- Noah: fill in details here -->

### Refactoring 6: Extract Class + Rename — `singleResponsibilityPrinciple` (Noah)
<!-- Noah: fill in details here -->

---

## Q2: Features

### Feature 1: SeverityLevel Enum (Ervin)
- **Commit:** `4f47f7e`
- **Files:**
  - Created `src/main/java/rhit/csse/csse374/linter/domain/SeverityLevel.java`
  - Modified `src/main/java/rhit/csse/csse374/linter/domain/Violation.java`
  - Modified `src/main/java/rhit/csse/csse374/linter/presentation/gui/ResultsAccordionPanel.java`
  - Modified `src/main/java/rhit/csse/csse374/linter/presentation/gui/SeverityCellRenderer.java`
- **What it does:** Adds a `SeverityLevel` enum with three levels: `ERROR`, `WARNING`, `INFO`. Each level carries a display color for the GUI. The `Violation` class now stores a `SeverityLevel` field. The GUI displays severity as a colored label and provides a severity filter dropdown. A `fromString()` parser handles string-to-enum conversion with safe defaults.
- **Design decisions:**
  - Backwards-compatible: the existing 2-arg `Violation(message, location)` constructor defaults to `WARNING`
  - Colors are stored on the enum itself to keep presentation logic centralized
  - `fromString()` is lenient (returns `WARNING` for unrecognized input) to avoid crashes from bad data
- **Tests:**
  - `SeverityLevelTest.java` — 9 tests (enum values, colors, fromString parsing)
  - `ViolationTest.java` — 8 tests (constructors, severity storage, toString format)

### Feature 2: Export Results to JSON (Noah)
<!-- Noah: fill in details here -->

---

## Q3: Exploratory Testing

### Session 1 — Ervin
<!-- 
INSTRUCTIONS FOR ERVIN:
Run your linter on a Java project you haven't tested it on before (e.g., a classmate's 
CSSE220 project, an open-source project, or a sample project from another course).

Document the following:
1. **Target project:** What project did you lint? How big is it (# classes)?
2. **Setup:** How did you point the linter at it? Any configuration needed?
3. **Findings:** What violations did the linter report? Were they correct?
4. **False positives:** Did the linter flag anything that isn't actually a problem? Why?
5. **False negatives:** Did you notice problems the linter missed? What checks would catch them?
6. **Bugs found:** Did the linter crash or behave unexpectedly on any input?
7. **Usability notes:** Was the output clear? Was it easy to understand what to fix?
8. **Improvements identified:** Based on this session, what would you change or add?

Write 1-2 paragraphs per item above. Be specific — include class names, violation messages, 
and screenshots if possible.
-->

### Session 2 — Noah
<!-- Noah: fill in using the same format above -->

---

## Q4: Test Plans

### Performance Test Plan
<!--
INSTRUCTIONS FOR ERVIN:
Document how you would test that the linter performs acceptably. Cover:

1. **What to measure:** Execution time for full analysis, memory usage, startup time
2. **Test scenarios:**
   - Small project: ~5 classes → expected time < 2 seconds
   - Medium project: ~50 classes → expected time < 10 seconds  
   - Large project: ~200+ classes → expected time < 30 seconds
3. **How to measure:** Use System.nanoTime() around analysis calls, or use JMH benchmarks
4. **Acceptance criteria:** Define what "too slow" means for each scenario
5. **Bottleneck analysis:** Which checks are likely slowest? (e.g., CohesionAnalyzer's 
   BFS on large classes, ASM analysis with SimpleVerifier)
6. **Tools:** JVisualVM for memory profiling, Maven Surefire for test timing

Example format:

| Test Case | Input Size | Expected Time | Metric |
|-----------|-----------|---------------|--------|
| Single class analysis | 1 class, 10 methods | < 500ms | Wall clock |
| Full project scan | 50 classes | < 10s | Wall clock |
| Memory under load | 200 classes | < 512MB heap | Peak heap |
-->

### Security Test Plan
<!--
INSTRUCTIONS FOR ERVIN:
Document how you would test that the linter handles untrusted input safely. Cover:

1. **Threat model:** The linter analyzes arbitrary .class files. A malicious .class file 
   could attempt to exploit the analysis.
2. **Test scenarios:**
   - Malformed .class files (truncated, corrupted bytes) → should not crash, should 
     report graceful error
   - Extremely large .class files (huge method bodies) → should not cause OutOfMemoryError
   - .class files with unusual bytecode (e.g., obfuscated code) → should handle gracefully
   - Path traversal in class names → should not write/read outside expected directories
3. **File system safety:** The linter should only READ files, never write to the analyzed 
   project directory
4. **Dependency vulnerabilities:** Run `mvn dependency:tree` and check for known CVEs in 
   ASM or other dependencies (use OWASP dependency-check plugin)
5. **Output safety:** If results are displayed in HTML/GUI, ensure violation messages 
   can't inject scripts (XSS in Swing is unlikely but document why)

Example format:

| Test Case | Input | Expected Behavior |
|-----------|-------|-------------------|
| Corrupted .class file | Random bytes named Test.class | Graceful error, no crash |
| Giant method | 10,000 instruction method | Completes within timeout |
| Path traversal name | Class named "../../etc/passwd" | Rejected or sanitized |
-->

---

## CI/CD
- **GitHub Actions:** `.github/workflows/ci.yml` — runs `mvn test` on every push/PR
- **Commit:** `64f6a9c`
- **Test results:** 48 tests, 0 failures

---

## Test Summary

| Test File | # Tests | What it covers |
|-----------|---------|----------------|
| `ViolationTest.java` | 8 | Violation constructors, severity, toString |
| `SeverityLevelTest.java` | 9 | Enum values, colors, fromString parsing |
| `ASMMethodTest.java` | 6 | Analysis, instructions, naming |
| `ASMClassTest.java` | 6 | isInterface, isAbstract, getMethods |
| `CohesionAnalyzerTest.java` | 5 | LCOM4 scoring on fixture classes |
| `EqualsCheckerTest.java` | 4 | Equals/hashCode check |
| `PascalClassNameTest.java` | 3 | Naming convention check |
| `MethodTooLongTest.java` | 3 | Long method detection |
| `TemplatePatternTest.java` | 2 | Template pattern detection |
| `DecoratorPatternTest.java` | 2 | Decorator pattern detection |
| **Total** | **48** | |
