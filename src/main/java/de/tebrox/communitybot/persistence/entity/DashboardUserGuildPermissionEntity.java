package de.tebrox.communitybot.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "dashboard_user_guild_permission",
        uniqueConstraints = @UniqueConstraint(name = "uk_dashboard_user_guild", columnNames = {"userId", "guildId"})
)
public class DashboardUserGuildPermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 32)
    private String guildId;

    @Column(nullable = false)
    private long permissions;

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public long getPermissions() {
        return permissions;
    }

    public void setPermissions(long permissions) {
        this.permissions = permissions;
    }
}