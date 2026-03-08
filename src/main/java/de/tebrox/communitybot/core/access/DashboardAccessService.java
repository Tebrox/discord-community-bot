package de.tebrox.communitybot.core.access;

import de.tebrox.communitybot.core.security.DashboardSecurityService;
import de.tebrox.communitybot.core.persistence.entity.DashboardUserEntity;
import de.tebrox.communitybot.core.persistence.entity.DashboardUserGuildPermissionEntity;
import de.tebrox.communitybot.core.persistence.repository.DashboardUserGuildPermissionRepository;
import de.tebrox.communitybot.core.persistence.repository.DashboardUserRepository;
import de.tebrox.communitybot.core.security.DashboardPermission;
import de.tebrox.communitybot.core.security.PermissionBits;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DashboardAccessService {

    private final DashboardUserRepository dashboardUserRepository;
    private final DashboardUserGuildPermissionRepository permissionRepository;
    private final DashboardSecurityService securityService;

    public DashboardAccessService(
            DashboardUserRepository dashboardUserRepository,
            DashboardUserGuildPermissionRepository permissionRepository,
            DashboardSecurityService securityService
    ) {
        this.dashboardUserRepository = dashboardUserRepository;
        this.permissionRepository = permissionRepository;
        this.securityService = securityService;
    }

    public DashboardUserEntity getCurrentUserOrNull() {
        String discordId = securityService.getCurrentDiscordId();
        if (discordId == null || discordId.isBlank()) {
            return null;
        }
        return dashboardUserRepository.findByDiscordId(discordId).orElse(null);
    }

    public boolean isSuperadmin() {
        return securityService.isSuperadmin();
    }

    public boolean canAccessDashboard() {
        if (isSuperadmin()) {
            return true;
        }

        DashboardUserEntity user = getCurrentUserOrNull();
        return user != null && user.isEnabled();
    }

    public boolean hasGuildPermission(String guildId, DashboardPermission permission) {
        if (isSuperadmin()) {
            return true;
        }

        DashboardUserEntity user = getCurrentUserOrNull();
        if (user == null || !user.isEnabled()) {
            return false;
        }

        DashboardUserGuildPermissionEntity entry = permissionRepository
                .findByUserIdAndGuildId(user.getId(), guildId)
                .orElse(null);

        return entry != null && PermissionBits.has(entry.getPermissions(), permission);
    }

    public List<DashboardUserEntity> listUsers() {
        return dashboardUserRepository.findAll();
    }

    public Optional<DashboardUserEntity> findUser(Long id) {
        return dashboardUserRepository.findById(id);
    }

    @Transactional
    public DashboardUserEntity createIfMissing(String discordId) {
        return dashboardUserRepository.findByDiscordId(discordId)
                .orElseGet(() -> {
                    DashboardUserEntity entity = new DashboardUserEntity();
                    entity.setDiscordId(discordId);
                    entity.setEnabled(true);
                    return dashboardUserRepository.save(entity);
                });
    }

    @Transactional
    public void setEnabled(Long id, boolean enabled) {
        DashboardUserEntity user = dashboardUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden: " + id));

        user.setEnabled(enabled);
        dashboardUserRepository.save(user);
    }

    public long getPermissionMask(Long userId, String guildId) {
        return permissionRepository.findByUserIdAndGuildId(userId, guildId)
                .map(DashboardUserGuildPermissionEntity::getPermissions)
                .orElse(0L);
    }

    @Transactional
    public void savePermissionMask(Long userId, String guildId, long mask) {
        DashboardUserGuildPermissionEntity entry = permissionRepository
                .findByUserIdAndGuildId(userId, guildId)
                .orElseGet(() -> {
                    DashboardUserGuildPermissionEntity e = new DashboardUserGuildPermissionEntity();
                    e.setUserId(userId);
                    e.setGuildId(guildId);
                    return e;
                });

        entry.setPermissions(mask);
        permissionRepository.save(entry);
    }

    public boolean hasPermission(Long mask, DashboardPermission permission) {
        return mask != null && PermissionBits.has(mask, permission);
    }
}