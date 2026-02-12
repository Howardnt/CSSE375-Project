package fixtures;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Fixture intended to be a clear SRP violation / god class for `principle2`.
 *
 * It has:
 * - many fields (>= 8)
 * - many public methods (>= 10)
 * - low cohesion (methods touch disjoint subsets of fields)
 * - dependency fan-out (calls into several different Java packages)
 */
public class SrpGodClassFixture {

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
        // touches user fields
        userName = "u";
        userEmail = "e";
        userRole = "r";
        System.out.println(UUID.randomUUID());
    }

    public void validateUser() {
        // touches different fields
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

