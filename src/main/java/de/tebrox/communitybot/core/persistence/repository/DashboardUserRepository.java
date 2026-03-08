package de.tebrox.communitybot.core.persistence.repository;

import de.tebrox.communitybot.core.persistence.entity.DashboardUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DashboardUserRepository extends JpaRepository<DashboardUserEntity, Long> {
    Optional<DashboardUserEntity> findByDiscordId(String discordId);
}
