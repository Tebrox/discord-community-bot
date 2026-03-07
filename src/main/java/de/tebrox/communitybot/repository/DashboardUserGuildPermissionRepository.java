package de.tebrox.communitybot.repository;

import de.tebrox.communitybot.persistence.entity.DashboardUserGuildPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DashboardUserGuildPermissionRepository extends JpaRepository<DashboardUserGuildPermissionEntity, Long> {
    List<DashboardUserGuildPermissionEntity> findAllByUserId(Long userId);
    Optional<DashboardUserGuildPermissionEntity> findByUserIdAndGuildId(Long userId, String guildId);
}
