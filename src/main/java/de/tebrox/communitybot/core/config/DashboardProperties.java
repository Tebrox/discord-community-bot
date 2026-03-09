package de.tebrox.communitybot.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Dashboard auth config loaded from application.yml via @ConfigurationProperties.
 * Replaces old @Value-based DashboardProperties class.
 * Binds:
 *   dashboard.password-hash  → passwordHash
 *   dashboard.max-login-attempts → maxLoginAttempts
 *   dashboard.lockout-duration-minutes → lockoutDurationMinutes
 */
@Validated
@ConfigurationProperties(prefix = "dashboard")
public record DashboardProperties(
        String superadminDiscordId,
        boolean demo
) {}
