package de.tebrox.communitybot.auth;

import de.tebrox.communitybot.core.config.DashboardProperties;
import de.tebrox.communitybot.core.persistence.entity.LoginAttempt;
import de.tebrox.communitybot.core.persistence.repository.LoginAttemptRepository;
import de.tebrox.communitybot.core.security.AuthService;
import de.tebrox.communitybot.core.security.AuthService.AuthResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 *
 * Tests:
 * A1 - Successful login resets failed attempts (deleteByIpAddress called)
 * A2 - Lockout after maxLoginAttempts failed attempts within window
 * A3 - IP masking: maskIp() never returns the full IP
 * A4 - cleanupOldAttempts() calls deleteOlderThan with correct cutoff
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String PLAIN_PASSWORD = "testPassword123!";
    private static final String IP = "192.168.1.100";
    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCKOUT_MINUTES = 15;

    @Mock
    private LoginAttemptRepository loginAttemptRepository;

    private PasswordEncoder passwordEncoder;
    private DashboardProperties dashboardProperties;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(4); // cost 4 for fast tests
        String hash = passwordEncoder.encode(PLAIN_PASSWORD);
        dashboardProperties = new DashboardProperties(hash, MAX_ATTEMPTS, LOCKOUT_MINUTES);
        authService = new AuthService(dashboardProperties, loginAttemptRepository, passwordEncoder);
    }

    // ------------------------------------------------------------------
    // A1: Successful login clears failed attempts
    // ------------------------------------------------------------------

    @Test
    @DisplayName("A1: Successful login calls deleteByIpAddress to reset lockout state")
    void successfulLogin_clearsFailedAttempts() {
        // Arrange: not locked out
        when(loginAttemptRepository.countFailedAttemptsSince(eq(IP), any(Instant.class))).thenReturn(0L);
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        AuthResult result = authService.attemptLogin(PLAIN_PASSWORD, IP);

        // Assert
        assertThat(result).isEqualTo(AuthResult.SUCCESS);
        verify(loginAttemptRepository).deleteByIpAddress(IP);
    }

    @Test
    @DisplayName("A1: Failed login does NOT call deleteByIpAddress")
    void failedLogin_doesNotClearAttempts() {
        when(loginAttemptRepository.countFailedAttemptsSince(eq(IP), any(Instant.class)))
                .thenReturn(0L) // first call in isLockedOut
                .thenReturn(1L); // second call for failed count
        when(loginAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResult result = authService.attemptLogin("wrongPassword", IP);

        assertThat(result).isEqualTo(AuthResult.INVALID_CREDENTIALS);
        verify(loginAttemptRepository, never()).deleteByIpAddress(any());
    }

    // ------------------------------------------------------------------
    // A2: Lockout after X failed attempts
    // ------------------------------------------------------------------

    @Test
    @DisplayName("A2: isLockedOut returns true when failed count >= maxLoginAttempts")
    void isLockedOut_returnsTrueWhenLimitReached() {
        when(loginAttemptRepository.countFailedAttemptsSince(eq(IP), any(Instant.class)))
                .thenReturn((long) MAX_ATTEMPTS);

        boolean locked = authService.isLockedOut(IP);

        assertThat(locked).isTrue();
    }

    @Test
    @DisplayName("A2: isLockedOut returns false when failed count < maxLoginAttempts")
    void isLockedOut_returnsFalseWhenBelowLimit() {
        when(loginAttemptRepository.countFailedAttemptsSince(eq(IP), any(Instant.class)))
                .thenReturn((long) (MAX_ATTEMPTS - 1));

        boolean locked = authService.isLockedOut(IP);

        assertThat(locked).isFalse();
    }

    @Test
    @DisplayName("A2: attemptLogin returns LOCKED_OUT when IP is already locked")
    void attemptLogin_returnsLockedOut_whenAlreadyLocked() {
        when(loginAttemptRepository.countFailedAttemptsSince(eq(IP), any(Instant.class)))
                .thenReturn((long) MAX_ATTEMPTS);

        AuthResult result = authService.attemptLogin(PLAIN_PASSWORD, IP);

        assertThat(result).isEqualTo(AuthResult.LOCKED_OUT);
        // No attempt should be saved when already locked
        verify(loginAttemptRepository, never()).save(any());
    }

    @Test
    @DisplayName("A2: attemptLogin returns NOW_LOCKED_OUT when this attempt triggers lockout")
    void attemptLogin_returnsNowLockedOut_whenThisAttemptTriggersLockout() {
        // First call (isLockedOut check): not yet locked
        // Second call (after saving failed attempt): now at limit
        when(loginAttemptRepository.countFailedAttemptsSince(eq(IP), any(Instant.class)))
                .thenReturn(0L)                    // isLockedOut pre-check
                .thenReturn((long) MAX_ATTEMPTS);  // failed count after saving
        when(loginAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResult result = authService.attemptLogin("wrongPassword", IP);

        assertThat(result).isEqualTo(AuthResult.NOW_LOCKED_OUT);
    }

    // ------------------------------------------------------------------
    // A3: IP masking
    // ------------------------------------------------------------------

    @Test
    @DisplayName("A3: maskIp masks last IPv4 octet")
    void maskIp_masksLastIpv4Octet() {
        String masked = AuthService.maskIp("192.168.1.100");
        assertThat(masked).isEqualTo("192.168.1.***");
        assertThat(masked).doesNotContain("100");
    }

    @Test
    @DisplayName("A3: maskIp masks last IPv6 segment")
    void maskIp_masksLastIpv6Segment() {
        String masked = AuthService.maskIp("2001:db8::1");
        assertThat(masked).endsWith(":****");
        assertThat(masked).doesNotContain("1\n");
    }

    @Test
    @DisplayName("A3: maskIp handles null gracefully")
    void maskIp_handlesNull() {
        assertThat(AuthService.maskIp(null)).isEqualTo("unknown");
    }

    @Test
    @DisplayName("A3: Full IP 192.168.1.100 is never fully present in masked output")
    void maskIp_fullIpNeverInOutput() {
        String full = "10.20.30.40";
        String masked = AuthService.maskIp(full);
        assertThat(masked).doesNotContain(full);
        assertThat(masked).doesNotContain("40");
    }

    // ------------------------------------------------------------------
    // A4: Cleanup scheduler
    // ------------------------------------------------------------------

    @Test
    @DisplayName("A4: cleanupOldAttempts deletes records older than 24h")
    void cleanupOldAttempts_deletesRecordsOlderThan24h() {
        // Call the scheduler method directly (no sleep needed)
        Instant before = Instant.now();
        authService.cleanupOldAttempts();
        Instant after = Instant.now();

        ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
        verify(loginAttemptRepository).deleteOlderThan(captor.capture());

        Instant cutoff = captor.getValue();
        // The cutoff should be approximately 24h ago
        assertThat(cutoff).isBefore(before.minusSeconds(23 * 3600));
        assertThat(cutoff).isAfter(after.minusSeconds(25 * 3600));
    }
}
