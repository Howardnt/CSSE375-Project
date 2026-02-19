package fixtures;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Fixture for {@code singleResponsibilityPrinciple} (SRP heuristic).
 *
 * PASS: small, cohesive class where methods share the same small set of state.
 * FAIL: "god class" with many fields, many public methods, low cohesion, and high dependency fan-out.
 */
public class SingleResponsibilityPrincipleFixture {
    // This class is just a container for the PASS/FAIL examples below.
}

// ===== PASS (should NOT be flagged) =====
class SrpGoodCohesiveExample {
    private final List<String> items = new ArrayList<>();
    private int count = 0;

    public void addItem(String item) {
        items.add(item);
        count++;
    }

    public void removeItem(String item) {
        if (items.remove(item)) {
            count--;
        }
    }

    public int size() {
        return count;
    }

    public boolean contains(String item) {
        return items.contains(item);
    }
}

// ===== FAIL (should be flagged) =====
class SrpBadGodClassExample {

    private String userName;
    private String userEmail;
    private String userRole;
    private String auditLogPath;
    private int retryCount;
    private boolean admin;
    private final List<String> cache = new ArrayList<>();
    private final List<String> messages = new ArrayList<>();
    private int timeoutMs;

    public void loadUserFromDb() {
        userName = "u";
        userEmail = "e";
        userRole = "r";
        System.out.println(UUID.randomUUID());
    }

    public void validateUser() {
        admin = userRole != null && userRole.equals("admin");
        System.out.println(Instant.now());
    }

    public void updateUserProfile(String newName) {
        userName = newName;
        System.out.println(Paths.get("tmp"));
    }

    public void sendWelcomeEmail() {
        messages.add("welcome:" + userEmail);
        System.out.println(UUID.randomUUID());
    }

    public void sendPasswordResetEmail() {
        messages.add("reset:" + userEmail);
        System.out.println(Instant.now());
    }

    public void queueNotification(String msg) {
        messages.add(msg);
        System.out.println(msg);
    }

    public void writeAuditLog(String event) {
        auditLogPath = "audit.log";
        System.out.println(event);
    }

    public void rotateLogs() {
        auditLogPath = "audit-" + Instant.now().toString();
        System.out.println(auditLogPath);
    }

    public void cacheValue(String value) {
        cache.add(value);
        System.out.println(cache.size());
    }

    public void clearCache() {
        cache.clear();
        System.out.println(cache.size());
    }

    public void configureTimeout(int timeoutMs) {
        this.timeoutMs = timeoutMs;
        System.out.println(this.timeoutMs);
    }

    public void resetTimeout() {
        this.timeoutMs = 1000;
        System.out.println(this.timeoutMs);
    }

    public void setRetryCount(int retries) {
        this.retryCount = retries;
        System.out.println(this.retryCount);
    }
}

