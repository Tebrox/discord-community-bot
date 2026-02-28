package de.tebrox.rolesbot.repository;

import de.tebrox.rolesbot.persistence.entity.WelcomedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WelcomedUserRepository extends JpaRepository<WelcomedUser, Long> {

    boolean existsByGuildIdAndUserId(String guildId, String userId);

    List<WelcomedUser> findByGuildIdOrderByWelcomedAtDesc(String guildId);

    Optional<WelcomedUser> findByGuildIdAndUserId(String guildId, String userId);

    void deleteByGuildIdAndUserId(String guildId, String userId);
}
