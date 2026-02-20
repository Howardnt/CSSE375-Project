# Final Video Presentation Plan & Script

This document is designed to help you organize and narrate your final video presentation. It covers the three checks you built, plus the individual extra feature, explaining *how* they work visually, the *underlying bytecode logic*, and *why* they are important.

## Presentation Structure (Approx. 5-7 Minutes)

### 1. Introduction (0:30)
*   **Action:** Screen share your GUI or the project `README`.
*   **Script:** "Hi, I'm [Your Name], and today I'll be demonstrating the architectural linter I built for our final project. I'll be walking you through three distinct types of checks I implemented—a cursory check, a design pattern, and a design principle—and finally, I'll showcase my individual extra feature."

### 2. Cursory Check: PascalCase Naming Convention (1:00)
*   **Action:** Show `PascalClassName.java`. Open the GUI, select the check, and run it against a sample project that contains a poorly named class.
*   **Detailed Logic:** "Under the hood, this check hooks into the ASM `ClassNode`. It grabs the `classNode.name` (which includes the full package path separated by slashes), and strips the package away to get the simple class name. It then iterates through the characters in that string, ensuring that index 0 is an uppercase letter, and that the remaining characters are strictly letters or digits, flagging any underscores or special characters as violations."
*   **Why it's Important:** "Enforcing naming conventions is critical for team maintainability. Catching bad names (like `badClassName` or `WITH_UNDERSCORE`) at compile time ensures the codebase remains readable and consistent without requiring manual code reviews for simple formatting."

### 3. Design Pattern: Template Method (1:30)
*   **Action:** Show `TemplatePattern.java`. Show the linter successfully detecting a Template Method in a test fixture. 
*   **Detailed Logic:** "To detect a Template Pattern, the linter first inspects the `ClassNode.access` flags to confirm the class is `abstract`. If it is, it iterates through all of its concrete methods (`MethodNode`). Inside each method, it scans the bytecode instructions for `MethodInsnNode`s (method calls). If it finds a method call whose owner is the current class, it checks the class's method list to see if *that* called method is flagged as `abstract`. If it is, we've successfully hit the 'template method' criteria."
*   **Why it's Important:** "This is important because it allows developers to automatically document their architecture. By detecting Template Methods, the linter highlights intended extension points in the system, showing where subclasses are expected to plug in their specific behaviors."

### 4. Design Principle: Hollywood Principle (1:30)
*   **Action:** Show `HollywoodPrinciple.java`. Run it against a project that has a class illegally calling its superclass.
*   **Detailed Logic:** "My principle detector enforces 'Don't call us, we'll call you.' First, it looks at `ClassNode.superName` and `interfaces` to compile a list of 'high-level' dependencies. It uses Java's `ClassLoader` to resolve the method signatures of those parent classes. Then, it dives into the subclass's `AbstractInsnNode` instructions. It flags a violation if it encounters a `MethodInsnNode` where the owner matches a high-level dependency, or if it encounters a `TypeInsnNode` with a `NEW` opcode pointing to a high-level dependency, indicating illegal upward coupling."
*   **Why it's Important:** "Violating the Hollywood Principle leads to tight coupling and dependency cycles. Low-level components shouldn't hijack control flow. This check ensures that high-level abstract components remain in charge of orchestrating execution."

### 5. Individual Extra Feature: Tunable Hollywood Strategy (2:00)
*   **Action:** Show `design.puml` emphasizing the `HollywoodStrategy` interface and the aggregation arrow. Then, show the three checkboxes in the GUI.
*   **The Problem:** "For my individual extra feature, I wanted to address a flaw in my Hollywood Principle detector. Originally, it had a hardcoded threshold rule—flagging a class if it made exactly 3 or more upward calls. Providing hardcoded thresholds in architectural scanners violates the Open/Closed Principle."
*   **Detailed Logic:** "As suggested in the project prompt, I implemented a tunable rules engine using the **Strategy Pattern**. I extracted the bytecode validation loop from `HollywoodPrinciple` and placed it into a `HollywoodStrategy` interface with an `analyzeCoupling()` method. I then created three interchangeable strategies:
    *   **Threshold Strategy:** Loops through `MethodInsnNode` calls and increments a counter, only flagging if it breaches a user-defined threshold.
    *   **Strict Strategy:** Flags an error the moment a single upward method call is detected.
    *   **Instantiation-Only Strategy:** Skips scanning `MethodInsnNode` completely, and only scans `TypeInsnNode` instructions to prevent subclasses from instantiating their parents."
*   **The Demo:** "As you can see in the GUI, because I used the Strategy pattern, I was able to create lightweight wrapper classes that expose all three rule sets to the user simultaneously. The user now has complete control over how strictly the architectural boundary is enforced, all without having to rewrite the underlying ASM analysis engine."

### 6. Conclusion (0:30)
*   **Action:** Show the final passed tests or a clean output run.
*   **Script:** "By combining cursory checks, pattern detection, principle enforcement, and a highly tunable rules engine via the Strategy Pattern, this linter provides a robust, adaptable tool for monitoring software architecture. Thank you."
