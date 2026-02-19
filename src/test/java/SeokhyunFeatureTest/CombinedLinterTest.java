package SeokhyunFeatureTest;

import rhit.csse.csse374.linter.domain.CheckResult;
import rhit.csse.csse374.linter.domain.ConvertToASM;
import rhit.csse.csse374.linter.domain.Violation;
import rhit.csse.csse374.linter.domain.PascalClassName;
import rhit.csse.csse374.linter.domain.TemplatePattern;
import rhit.csse.csse374.linter.domain.TemplateMethodInfo;
import rhit.csse.csse374.linter.domain.HollywoodPrinciple;

import java.util.List;

//Jack Traversa (with Claude assistance in accordance with the requirements document)

/**
 * Combined test runner for PascalCase Cursory, Template Pattern, and Hollywood
 * Principle checks.
 *
 * This loads compiled fixture .class files from
 * out/SeokhyunFeatureTest/fixtures/
 * and runs each checker against them, printing results and comparing expected
 * vs actual violation counts.
 */
public class CombinedLinterTest {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("  COMBINED LINTER TEST RUNNER");
        System.out.println("  Testing: PascalCase, Template Pattern, Hollywood Principle");
        System.out.println("=".repeat(70));

        // Load all fixture classes from the fixtures directory
        String fixturesPath = "out/SeokhyunFeatureTest/fixtures";
        System.out.println("\nLoading fixtures from: " + fixturesPath);

        ConvertToASM converter = new ConvertToASM(fixturesPath);

        // ===== TEST 1: PascalCase Cursory Check =====
        runSection("1. PascalCase Cursory Check");
        int expectedPascal = 3; // badClassName, BADCONSTANTNAME, With_Underscore
        System.out.println("Expected violations: " + expectedPascal);
        System.out.println("  - badClassName: starts with lowercase");
        System.out.println("  - BADCONSTANTNAME: all-caps name (>3 chars)");
        System.out.println("  - With_Underscore: contains underscore");
        System.out.println("Should NOT flag: PascalCaseFixture, GoodClassName, URL, MyHttpClient");
        System.out.println();

        PascalClassName pascalChecker = new PascalClassName();
        CheckResult pascalResult = pascalChecker.run(converter.toASMProject());
        printResult(pascalResult);
        compareResult("PascalCase", expectedPascal, pascalResult.getViolations().size());

        // ===== TEST 2: Template Pattern Check =====
        runSection("2. Template Pattern Check");
        int expectedTemplate = 1; // Only AbstractGame should be detected
        System.out.println("Expected detections: " + expectedTemplate);
        System.out.println("  - AbstractGame: play() calls abstract methods initialize(), startPlay(), endPlay()");
        System.out.println("Should NOT detect: Cricket (concrete class), NotATemplate (no template method)");
        System.out.println();

        TemplatePattern templateChecker = new TemplatePattern();
        CheckResult templateResult = templateChecker.run(converter.toASMProject());
        printResult(templateResult);

        // Print detailed template method info
        List<TemplateMethodInfo> detectedPatterns = templateChecker.getDetectedPatterns();
        if (!detectedPatterns.isEmpty()) {
            System.out.println("TEMPLATE METHOD DETAILS:");
            for (TemplateMethodInfo info : detectedPatterns) {
                System.out.println("  " + info.toString());
                System.out.println("  " + info.formatDetails().replace("\n", "\n  "));
            }
            System.out.println();
        }
        compareResult("Template Pattern", expectedTemplate, templateResult.getViolations().size());

        // ===== TEST 3: Hollywood Principle Check =====
        runSection("3. Hollywood Principle Check");
        int expectedHollywood = 2; // BadLowLevel (upward calls) + InstantiatesParent (instantiation)
        System.out.println("Expected violations: " + expectedHollywood);
        System.out.println("  - BadLowLevel: makes >=3 distinct upward calls to HighLevelService interface");
        System.out.println("  - InstantiatesParent: instantiates its own superclass ParentComponent");
        System.out.println("Should NOT flag: GoodLowLevel (<3 upward calls)");
        System.out.println();

        HollywoodPrinciple hollywoodChecker = new HollywoodPrinciple();
        CheckResult hollywoodResult = hollywoodChecker.run(converter.toASMProject());
        printResult(hollywoodResult);
        compareResult("Hollywood Principle", expectedHollywood, hollywoodResult.getViolations().size());

        // ===== FINAL SUMMARY =====
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println("  FINAL SUMMARY");
        System.out.println("=".repeat(70));
        System.out.printf("  PascalCase:          %d violation(s)  (expected %d)%n",
                pascalResult.getViolations().size(), expectedPascal);
        System.out.printf("  Template Pattern:    %d detection(s)  (expected %d)%n",
                templateResult.getViolations().size(), expectedTemplate);
        System.out.printf("  Hollywood Principle: %d violation(s)  (expected %d)%n",
                hollywoodResult.getViolations().size(), expectedHollywood);
        System.out.println("-".repeat(70));
        System.out.printf("  Tests PASSED: %d / %d%n", totalPassed, totalPassed + totalFailed);
        if (totalFailed > 0) {
            System.out.printf("  Tests FAILED: %d / %d%n", totalFailed, totalPassed + totalFailed);
        }
        System.out.println("=".repeat(70));
        System.out.println("\nAll tests complete!");
    }

    private static void runSection(String title) {
        System.out.println();
        System.out.println("-".repeat(70));
        System.out.println("  " + title);
        System.out.println("-".repeat(70));
    }

    private static void printResult(CheckResult result) {
        System.out.println(result);
        System.out.println();

        if (result.hasViolations()) {
            System.out.println("VIOLATIONS/DETECTIONS:");
            for (Violation violation : result.getViolations()) {
                System.out.println("  * " + violation);
            }
        } else {
            System.out.println("No violations found.");
        }

        if (result.hasAnalysisErrors()) {
            System.out.println("\nANALYSIS ERRORS:");
            for (String error : result.getAnalysisErrors()) {
                System.out.println("  * " + error);
            }
        }
        System.out.println();
    }

    private static void compareResult(String testName, int expected, int actual) {
        if (actual == expected) {
            System.out.println(">>> " + testName + ": PASS (expected " + expected + ", got " + actual + ")");
            totalPassed++;
        } else {
            System.out.println(">>> " + testName + ": FAIL (expected " + expected + ", got " + actual + ")");
            totalFailed++;
        }
    }
}
