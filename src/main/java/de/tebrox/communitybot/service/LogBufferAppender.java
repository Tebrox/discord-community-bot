package de.tebrox.communitybot.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Logback Appender that forwards real log events (INFO and above) into the
 * in-memory LogBuffer. Redaction is handled inside LogBuffer.redact().
 *
 * Lifecycle:
 * - Logback constructs this class early via logback-spring.xml (before Spring context)
 * - Spring later injects the ApplicationContext via ApplicationContextAware
 * - Until the context is ready, append() is a no-op (logBuffer == null guard)
 *
 * The static field approach avoids circular dependency between Logback and Spring.
 * Log level is preserved (INFO, WARN, ERROR forwarded; DEBUG/TRACE dropped).
 */
@Component
public class LogBufferAppender extends AppenderBase<ILoggingEvent> implements ApplicationContextAware {

    private static volatile LogBuffer logBuffer;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        logBuffer = ctx.getBean(LogBuffer.class);
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (logBuffer == null) return;
        // Only forward INFO and above
        if (!event.getLevel().isGreaterOrEqual(Level.INFO)) return;

        String level = event.getLevel().toString();
        // getFormattedMessage() resolves {} placeholders – no need to call LogBuffer.redact() separately
        // because LogBuffer.append() calls redact() internally.
        logBuffer.append(level, event.getFormattedMessage());
    }

    /** Package-private setter used by unit tests to inject a mock buffer. */
    static void setBuffer(LogBuffer buffer) {
        logBuffer = buffer;
    }
}
