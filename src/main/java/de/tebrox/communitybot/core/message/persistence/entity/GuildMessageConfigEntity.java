package de.tebrox.communitybot.core.message.persistence.entity;

import de.tebrox.communitybot.core.message.MessageKey;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "guild_message_config",
        uniqueConstraints = @UniqueConstraint(columnNames = {"guild_id", "message_key"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuildMessageConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guild_id", nullable = false, length = 32)
    private String guildId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_key", nullable = false, length = 100)
    private MessageKey messageKey;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Lob
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "embed_enabled", nullable = false)
    private boolean embedEnabled;

    @Column(name = "embed_title", length = 256)
    private String embedTitle;

    @Lob
    @Column(name = "embed_description", columnDefinition = "TEXT")
    private String embedDescription;

    @Column(name = "embed_footer", length = 2048)
    private String embedFooter;

    @Column(name = "embed_color", length = 16)
    private String embedColor;

    @Column(name = "thumbnail_url", length = 2048)
    private String thumbnailUrl;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
