package de.tebrox.rolesbot.repository;

import de.tebrox.rolesbot.persistence.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Repository for login attempt tracking.
 *
 * Uses COUNT queries (not findBy + list.size()) for O(1) DB-side counting.
 * Provides deleteByIpAddress for clearing attempts after successful login.
 * Provides deleteOlderThan for scheduled cleanup of stale records.
 */
@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    /**
     * COUNT-based check — no full list loaded into memory.
     */
    @Query("SELECT COUNT(a) FROM LoginAttempt a " +
           "WHERE a.ipAddress = :ip AND a.success = false AND a.attemptTime > :since")
    long countFailedAttemptsSince(@Param("ip") String ip, @Param("since") Instant since);

    /**
     * Delete all attempts for an IP (called after successful login to reset lockout).
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM LoginAttempt a WHERE a.ipAddress = :ip")
    void deleteByIpAddress(@Param("ip") String ip);

    /**
     * Cleanup job: remove records older than the given cutoff timestamp.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM LoginAttempt a WHERE a.attemptTime < :before")
    void deleteOlderThan(@Param("before") Instant before);
}
