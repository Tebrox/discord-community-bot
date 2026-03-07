package de.tebrox.communitybot.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "guild_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuildConfigEntity {
    /** Discord guild snowflake */
    @Id
    @Column(name = "guild_id", length = 32, nullable = false)
    private String guildId;

    @Column(name = "guild_name", length = 255, nullable = false)
    private String guildName;

    /** JSON representation of {@code de.tebrox.config.communitybot.GuildConfig}. */
    @Lob
    @Column(name = "config_json", nullable = false, columnDefinition = "TEXT")
    private String configJson;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
