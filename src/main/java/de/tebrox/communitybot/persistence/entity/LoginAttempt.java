package de.tebrox.communitybot.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Tracks login attempts per IP for lockout logic.
 * Uses Instant (UTC epoch) instead of LocalDateTime for timezone-safe comparison.
 * Table name changed to login_attempts (aligned with Ticketbot schema).
 * DB index on ip_address for efficient COUNT queries.
 */
@Entity
@Table(name = "login_attempts", indexes = {
        @Index(name = "idx_login_attempt_ip", columnList = "ip_address")
})
@Getter
@Setter
@NoArgsConstructor
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    /** UTC timestamp of the attempt. Uses Instant for reliable time comparison. */
    @Column(name = "attempt_time", nullable = false)
    private Instant attemptTime = Instant.now();

    @Column(name = "success", nullable = false)
    private boolean success;

    public LoginAttempt(String ipAddress, boolean success) {
        this.ipAddress = ipAddress;
        this.success = success;
        this.attemptTime = Instant.now();
    }
}
