package de.tebrox.rolesbot.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Dashboard auth config loaded from application.yml via @ConfigurationProperties.
 * Replaces old @Value-based DashboardProperties class.
 *
 * Binds:
 *   dashboard.password-hash  → passwordHash
 *   dashboard.max-login-attempts → maxLoginAttempts
 *   dashboard.lockout-duration-minutes → lockoutDurationMinutes
 */
@Validated
@ConfigurationProperties(prefix = "dashboard")
public record DashboardProperties(
        @NotBlank(message = "DASHBOARD_PASSWORD_HASH must be set in application.yml or as env var")
        String passwordHash,

        @Positive
        int maxLoginAttempts,

        @Positive
        int lockoutDurationMinutes
) {}
