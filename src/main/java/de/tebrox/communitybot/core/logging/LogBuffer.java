package de.tebrox.communitybot.core.logging;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;

/**
 * In-memory ring buffer for live log display in the web dashboard.
 * Thread-safe. Max 500 entries.
 *
 * Redaction replaces sensitive patterns (Discord tokens, Bearer tokens,
 * generic secret patterns) with [REDACTED] using compiled regex patterns.
 * The naive "if contains 'password'" check has been replaced.
 */
@Service
public class LogBuffer {

    public static final int MAX_SIZE = 500;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // ------------------------------------------------------------------ Redaction patterns
    // Discord bot token: starts with alphanumeric (24 chars), dot, alphanumeric (6 chars), dot, base64 (27+ chars)
    private static final Pattern DISCORD_TOKEN_PATTERN =
            Pattern.compile("[A-Za-z0-9_-]{23,28}\\.[A-Za-z0-9_-]{6,7}\\.[A-Za-z0-9_-]{27,}");

    // Authorization / Bearer header values
    private static final Pattern BEARER_TOKEN_PATTERN =
            Pattern.compile("(?i)(Authorization:\\s*Bearer\\s+)[A-Za-z0-9_\\-\\.]+");

    // Generic secret-like key=value patterns (password=, secret=, token=, hash=, key=)
    private static final Pattern GENERIC_SECRET_PATTERN =
            Pattern.compile("(?i)((?:password|secret|token|hash|apikey|api[_-]?key)\\s*[=:]\\s*)\\S+");

    // ------------------------------------------------------------------ Buffer
    private final Deque<LogEntry> buffer = new ArrayDeque<>();

    public record LogEntry(String timestamp, String level, String message) {}

    /**
     * Append a redacted log entry. Thread-safe.
     */
    public synchronized void append(String level, String message) {
        String redacted = redact(message);
        if (buffer.size() >= MAX_SIZE) {
            buffer.pollFirst();
        }
        buffer.addLast(new LogEntry(
                LocalDateTime.now().format(FORMATTER),
                level,
                redacted
        ));
    }

    /**
     * Apply all redaction patterns in order.
     * Returns the original string if null.
     */
    public static String redact(String message) {
        if (message == null) return null;
        String result = DISCORD_TOKEN_PATTERN.matcher(message).replaceAll("[REDACTED_TOKEN]");
        result = BEARER_TOKEN_PATTERN.matcher(result).replaceAll("$1[REDACTED]");
        result = GENERIC_SECRET_PATTERN.matcher(result).replaceAll("$1[REDACTED]");
        return result;
    }

    public synchronized List<LogEntry> getAll() {
        return new ArrayList<>(buffer);
    }

    public synchronized List<LogEntry> getSince(int skipCount) {
        List<LogEntry> all = new ArrayList<>(buffer);
        if (skipCount >= all.size()) return List.of();
        return all.subList(skipCount, all.size());
    }

    public synchronized int size() {
        return buffer.size();
    }

    // Convenience helpers
    public void info(String message) { append("INFO", message); }
    public void warn(String message) { append("WARN", message); }
    public void error(String message) { append("ERROR", message); }
}
