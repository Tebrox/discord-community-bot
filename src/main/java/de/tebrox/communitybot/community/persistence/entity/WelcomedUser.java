package de.tebrox.communitybot.community.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "welcomed_user",
       uniqueConstraints = @UniqueConstraint(columnNames = {"guild_id", "user_id"}))
@Getter @Setter @NoArgsConstructor
public class WelcomedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guild_id", nullable = false, length = 20)
    private String guildId;

    @Column(name = "user_id", nullable = false, length = 20)
    private String userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "welcomed_at", nullable = false)
    private LocalDateTime welcomedAt;

    public WelcomedUser(String guildId, String userId, String username) {
        this.guildId = guildId;
        this.userId = userId;
        this.username = username;
        this.welcomedAt = LocalDateTime.now();
    }
}
