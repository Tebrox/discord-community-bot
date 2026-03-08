package de.tebrox.communitybot.core.security;

import de.tebrox.communitybot.core.config.DashboardProperties;
import de.tebrox.communitybot.core.persistence.entity.LoginAttempt;
import de.tebrox.communitybot.core.persistence.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Authentication service with:
 * - COUNT-based lockout check (no full list load)
 * - IP masking in all log messages
 * - Reset of failed attempts after successful login
 * - Scheduled cleanup of stale attempt records
 * - AuthResult enum for precise response codes
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final DashboardProperties dashboardProperties;
    private final LoginAttemptRepository loginAttemptRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Attempt login. Records the attempt, enforces lockout.
     *
     * @param password  plain-text password from request
     * @param ipAddress client IP (masked in logs)
     * @return AuthResult enum value
     */
    @Transactional
    public AuthResult attemptLogin(String password, String ipAddress) {
        if (isLockedOut(ipAddress)) {
            log.warn("[Auth] Login attempt from locked-out IP: {}", maskIp(ipAddress));
            return AuthResult.LOCKED_OUT;
        }

        // Constant-time BCrypt comparison prevents timing attacks
        boolean success = passwordEncoder.matches(password, dashboardProperties.passwordHash());

        // Always persist the attempt before branching
        loginAttemptRepository.save(new LoginAttempt(ipAddress, success));

        if (success) {
            log.info("[Auth] Successful login from IP: {}", maskIp(ipAddress));
            // Clear failed attempts so lockout window resets immediately
            loginAttemptRepository.deleteByIpAddress(ipAddress);
            return AuthResult.SUCCESS;
        }

        long failed = countRecentFailed(ipAddress);
        log.warn("[Auth] Failed login attempt #{} from IP: {}", failed, maskIp(ipAddress));

        if (failed >= dashboardProperties.maxLoginAttempts()) {
            log.warn("[Auth] IP locked out after {} failed attempts: {}", failed, maskIp(ipAddress));
            return AuthResult.NOW_LOCKED_OUT;
        }
        return AuthResult.INVALID_CREDENTIALS;
    }

    /**
     * Check whether an IP is currently locked out (COUNT query, no list).
     */
    public boolean isLockedOut(String ipAddress) {
        Instant since = Instant.now().minus(dashboardProperties.lockoutDurationMinutes(), ChronoUnit.MINUTES);
        long failed = loginAttemptRepository.countFailedAttemptsSince(ipAddress, since);
        return failed >= dashboardProperties.maxLoginAttempts();
    }

    private long countRecentFailed(String ipAddress) {
        Instant since = Instant.now().minus(dashboardProperties.lockoutDurationMinutes(), ChronoUnit.MINUTES);
        return loginAttemptRepository.countFailedAttemptsSince(ipAddress, since);
    }

    /**
     * Mask IP address for privacy-safe logging.
     * IPv4: replace last octet with ***   (192.168.1.*** )
     * IPv6: replace last segment with **** (2001:db8::**** )
     */
    public static String maskIp(String ip) {
        if (ip == null) return "unknown";
        int lastDot = ip.lastIndexOf('.');
        if (lastDot != -1) {
            return ip.substring(0, lastDot) + ".***";
        }
        int lastColon = ip.lastIndexOf(':');
        if (lastColon != -1) {
            return ip.substring(0, lastColon) + ":****";
        }
        return "***";
    }

    /**
     * Cleanup stale login attempt records every hour.
     * Runs 60s after startup, then every 3600s.
     */
    @Scheduled(fixedRate = 3_600_000, initialDelay = 60_000)
    @Transactional
    public void cleanupOldAttempts() {
        Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
        loginAttemptRepository.deleteOlderThan(cutoff);
        log.debug("[Auth] Cleaned up login attempt records older than 24h");
    }

    public enum AuthResult {
        SUCCESS,
        INVALID_CREDENTIALS,
        LOCKED_OUT,
        NOW_LOCKED_OUT
    }
}
