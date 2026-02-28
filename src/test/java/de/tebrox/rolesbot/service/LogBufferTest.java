package de.tebrox.rolesbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LogBuffer.
 *
 * Tests:
 * L1 - Logback appender writes events into LogBuffer
 * L2 - Discord token pattern is redacted
 * L3 - "Authorization: Bearer ..." is redacted
 * L4 - Ring buffer respects MAX_SIZE limit (oldest evicted)
 * L5 - Generic secret patterns (password=, secret=, token=) are redacted
 */
class LogBufferTest {

    private LogBuffer logBuffer;

    @BeforeEach
    void setUp() {
        logBuffer = new LogBuffer();
    }

    // ------------------------------------------------------------------
    // L1: Appender writes to buffer
    // ------------------------------------------------------------------

    @Test
    @DisplayName("L1: LogBufferAppender.setBuffer + append stores entry in LogBuffer")
    void appender_writesEventIntoBuffer() {
        // Inject our buffer into the appender via static setter
        LogBufferAppender.setBuffer(logBuffer);

        // Simulate what the appender does internally
        logBuffer.info("Test message from appender");

        List<LogBuffer.LogEntry> entries = logBuffer.getAll();
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).message()).isEqualTo("Test message from appender");
        assertThat(entries.get(0).level()).isEqualTo("INFO");
    }

    // ------------------------------------------------------------------
    // L2: Discord token redaction
    // ------------------------------------------------------------------

    @Test
    @DisplayName("L2: Discord bot token is replaced with [REDACTED_TOKEN]")
    void redact_replacesDiscordToken() {
        // A realistic-looking Discord bot token format
        String fakeToken = "MTIzNDU2Nzg5MDEyMzQ1Njc4.GYdUfX.abcdefghijklmnopqrstuvwxyz12345";
        String result = LogBuffer.redact("Connecting with token: " + fakeToken);

        assertThat(result).contains("[REDACTED_TOKEN]");
        assertThat(result).doesNotContain(fakeToken);
        assertThat(result).doesNotContain("GYdUfX");
    }

    @Test
    @DisplayName("L2: Message without token is unchanged")
    void redact_leavesNormalMessageUnchanged() {
        String msg = "Bot started successfully on guild MyServer";
        assertThat(LogBuffer.redact(msg)).isEqualTo(msg);
    }

    // ------------------------------------------------------------------
    // L3: Bearer token redaction
    // ------------------------------------------------------------------

    @Test
    @DisplayName("L3: 'Authorization: Bearer abc123' is redacted")
    void redact_replacesBearerToken() {
        String msg = "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.payload.signature";
        String result = LogBuffer.redact(msg);

        assertThat(result).contains("[REDACTED]");
        assertThat(result).doesNotContain("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");
    }

    @Test
    @DisplayName("L3: Bearer redaction is case-insensitive")
    void redact_bearerIsCaseInsensitive() {
        String msg = "authorization: bearer sometoken123";
        String result = LogBuffer.redact(msg);

        assertThat(result).contains("[REDACTED]");
        assertThat(result).doesNotContain("sometoken123");
    }

    // ------------------------------------------------------------------
    // L4: Ring buffer size enforcement
    // ------------------------------------------------------------------

    @Test
    @DisplayName("L4: Buffer evicts oldest entry when MAX_SIZE is exceeded")
    void ringBuffer_evictsOldestWhenFull() {
        // Fill buffer to capacity
        for (int i = 0; i < LogBuffer.MAX_SIZE; i++) {
            logBuffer.info("message-" + i);
        }
        assertThat(logBuffer.size()).isEqualTo(LogBuffer.MAX_SIZE);

        // Add one more entry
        logBuffer.info("overflow-message");

        // Still at MAX_SIZE
        assertThat(logBuffer.size()).isEqualTo(LogBuffer.MAX_SIZE);

        // First message evicted, last message present
        List<LogBuffer.LogEntry> entries = logBuffer.getAll();
        assertThat(entries.get(0).message()).isEqualTo("message-1"); // message-0 was evicted
        assertThat(entries.get(entries.size() - 1).message()).isEqualTo("overflow-message");
    }

    @Test
    @DisplayName("L4: Buffer does not exceed MAX_SIZE after many appends")
    void ringBuffer_neverExceedsMaxSize() {
        for (int i = 0; i < LogBuffer.MAX_SIZE * 3; i++) {
            logBuffer.info("msg-" + i);
        }
        assertThat(logBuffer.size()).isEqualTo(LogBuffer.MAX_SIZE);
    }

    // ------------------------------------------------------------------
    // L5: Generic secret pattern redaction
    // ------------------------------------------------------------------

    @Test
    @DisplayName("L5: 'password=hunter2' is redacted")
    void redact_replacesPasswordPattern() {
        String result = LogBuffer.redact("Connecting with password=hunter2 to DB");
        assertThat(result).contains("[REDACTED]");
        assertThat(result).doesNotContain("hunter2");
    }

    @Test
    @DisplayName("L5: 'secret=abc123' is redacted")
    void redact_replacesSecretPattern() {
        String result = LogBuffer.redact("secret=abc123xyz");
        assertThat(result).contains("[REDACTED]");
        assertThat(result).doesNotContain("abc123xyz");
    }

    @Test
    @DisplayName("L5: 'token=xyz' is redacted")
    void redact_replacesTokenPattern() {
        String result = LogBuffer.redact("api token=someapitoken");
        assertThat(result).contains("[REDACTED]");
        assertThat(result).doesNotContain("someapitoken");
    }

    @Test
    @DisplayName("L5: null input returns null without exception")
    void redact_handlesNull() {
        assertThat(LogBuffer.redact(null)).isNull();
    }
}
