package security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import rhit.csse.csse374.linter.data.JsonLinterConfigLoader;
import rhit.csse.csse374.linter.data.LinterConfig;
import rhit.csse.csse374.linter.presentation.LinterService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lisa Crispin Q4 (security-facing technology) tests.
 *
 * The linter accepts user-controlled paths and JSON content. These tests
 * exercise hostile-input scenarios — bad paths, malformed/oversized config,
 * corrupted bytecode — and assert the system fails *bounded*: a clear
 * exception or a no-op result, never a JVM-level crash, hang, or
 * uncontrolled resource consumption.
 */
public class LinterSecurityTest {

    private static final long DOS_BUDGET_MS = 5_000;

    //--- Config loader: input validation ---

    @Test
    void configLoader_nullPath_failsWithIllegalArgument() {
        //Null is a programmer error, not user input — but the contract should
        //surface it predictably rather than NPE deep in the I/O layer.
        assertThrows(IllegalArgumentException.class,
                () -> new JsonLinterConfigLoader().load(null));
    }

    @Test
    void configLoader_nonExistentFile_failsWithIOException() {
        Path missing = Path.of("definitely-does-not-exist-12345.json");
        assertThrows(IOException.class, () -> new JsonLinterConfigLoader().load(missing));
    }

    //--- Config loader: malformed input doesn't leak internals ---

    @Test
    void configLoader_malformedJson_errorMessageIsBoundedAndSafe() {
        //Verifies the parser failure surfaces as ConfigParseException with a
        //user-meaningful message and not, say, a raw stack trace dumped into
        //the message body that could confuse an end user or expose internals
        //of the JSON-P provider.
        JsonLinterConfigLoader.ConfigParseException ex = assertThrows(
                JsonLinterConfigLoader.ConfigParseException.class,
                () -> new JsonLinterConfigLoader().parse("{ this is not json"));
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().toLowerCase().contains("malformed")
                        || ex.getMessage().toLowerCase().contains("json"),
                "Error message should be user-meaningful, got: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("at jakarta.json.")
                        || ex.getMessage().contains("at org.eclipse.parsson."),
                "Error message must not splice in a raw provider stack trace");
    }

    //--- Config loader: DoS resistance ---

    @Test
    void configLoader_oversizedMalformedJson_failsBoundedNotHangs(@TempDir Path tmp) throws IOException {
        Path big = tmp.resolve("big.json");
        StringBuilder sb = new StringBuilder(1_500_000);
        for (int i = 0; i < 250_000; i++) {
            sb.append("[[[[[[");
        }
        Files.writeString(big, sb.toString());

        long start = System.nanoTime();
        Throwable thrown = assertThrows(RuntimeException.class,
                () -> new JsonLinterConfigLoader().load(big));
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertTrue(elapsedMs < DOS_BUDGET_MS,
                "Parsing a 1.5 MB malformed blob should fail within "
                        + DOS_BUDGET_MS + " ms, took " + elapsedMs + " ms");
        assertNotNull(thrown.getMessage(),
                "Even on a denial-of-service attempt the error message must be set");
    }

    //--- Linter service: hostile filesystem inputs ---

    @Test
    void linterService_corruptedClassBytesInTarget_failsBoundedNotCrashes(@TempDir Path tmp) throws IOException {
        //Plant a file that LOOKS like a class file (right extension) but is
        //random bytes. ASM's ClassReader normally throws unchecked
        //exceptions on malformed bytecode; the pipeline must either skip the
        //file or surface a bounded RuntimeException — never a JVM-killing
        //error like StackOverflowError or unbounded recursion.
        Path evil = tmp.resolve("Evil.class");
        Files.write(evil, new byte[]{
                (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF,
                0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
        });

        LinterService service = new LinterService();
        LinterService.Request request = new LinterService.Request(
                tmp.toString(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        long start = System.nanoTime();
        try {
            LinterService.Response response = service.run(request);
            //Acceptable outcome 1: the bad file was skipped, run produced an
            //empty (or partial) result safely.
            assertNotNull(response);
            assertNotNull(response.result());
        } catch (RuntimeException expected) {
            //Acceptable outcome 2: bounded RuntimeException with a message.
            assertNotNull(expected.getMessage(),
                    "Failure on corrupt bytecode must carry a message; got bare " + expected.getClass());
        }
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        assertTrue(elapsedMs < DOS_BUDGET_MS,
                "A corrupted class file must not cause an unbounded scan; took " + elapsedMs + " ms");
    }

    @Test
    void linterService_nonExistentTargetPath_returnsEmptyResultWithoutLeak() {
        //Pointing at a path that does not exist must not surface filesystem
        //internals or stack traces in the return value, and must not scan
        //arbitrary unrelated locations. Current behavior: ConvertToASM
        //returns an empty class list and the run completes cleanly with
        //zero classes loaded — exactly the safe fail-closed shape we want.
        LinterService service = new LinterService();
        LinterService.Request request = new LinterService.Request(
                "/absolutely/nonexistent/path/aaa/bbb/ccc",
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        LinterService.Response response = service.run(request);

        assertNotNull(response);
        assertEquals(0, response.result().getTotalClasses(),
                "A bogus path must produce an empty result, not silently scan something else");
        assertFalse(response.result().hasAnyViolations(),
                "An empty result cannot have violations");
    }

    //--- Config loader: defense in depth ---

    @Test
    void configLoader_unexpectedTopLevelArray_failsClearly() {
        //A JSON array at the top level instead of an object should be
        //rejected with a clear error, not silently coerced or used to drive
        //downstream behavior.
        JsonLinterConfigLoader.ConfigParseException ex = assertThrows(
                JsonLinterConfigLoader.ConfigParseException.class,
                () -> new JsonLinterConfigLoader().parse("[\"checks\"]"));
        assertNotNull(ex.getMessage());
    }

    @Test
    void configLoader_boobyTrappedEnabledField_doesNotBypassToTrue() {
        //An attacker who controls the config file might put a non-boolean
        //"enabled" value hoping it coerces to true. Verify the loader
        //rejects it instead of fail-open.
        String json = """
                {
                  "checks": {
                    "EqualsChecker": { "enabled": "true" }
                  }
                }
                """;
        assertThrows(JsonLinterConfigLoader.ConfigParseException.class,
                () -> new JsonLinterConfigLoader().parse(json),
                "String 'true' must not be silently coerced to a boolean — fail closed instead");
    }

    @Test
    void configLoader_validInputAfterAttackAttempt_stillWorks() {
        //Sanity: confirm the loader keeps working after the hostile cases —
        //i.e. the parser is not left in some shared bad state.
        LinterConfig config = new JsonLinterConfigLoader().parse("""
                { "checks": { "EqualsChecker": { "enabled": false } } }
                """);
        assertFalse(config.isEnabled("EqualsChecker"));
        assertTrue(config.isEnabled("UnlistedCheck"));
    }
}
