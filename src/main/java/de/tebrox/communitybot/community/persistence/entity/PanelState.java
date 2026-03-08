package de.tebrox.communitybot.community.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "panel_state")
@Getter @Setter @NoArgsConstructor
public class PanelState {

    @Id
    @Column(name = "guild_id", nullable = false, length = 20)
    private String guildId;

    @Column(name = "channel_id", length = 20)
    private String channelId;

    @Column(name = "message_id", length = 20)
    private String messageId;

    public PanelState(String guildId, String channelId, String messageId) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.messageId = messageId;
    }

    public boolean hasState() {
        return channelId != null && !channelId.isBlank()
                && messageId != null && !messageId.isBlank();
    }
}
