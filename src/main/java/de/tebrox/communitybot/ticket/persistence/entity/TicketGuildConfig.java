package de.tebrox.communitybot.ticket.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ticket_guild_config")
public class TicketGuildConfig {

    @Id
    @Column(name = "guild_id", nullable = false, length = 20)
    private String guildId;

    @Column(name = "ticket_channel_id", length = 20)
    private String ticketChannelId;

    @Column(name = "log_channel_id", length = 20)
    private String logChannelId;

    @Column(name = "ticket_message_id", length = 20)
    private String ticketMessageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "thread_type", nullable = false, length = 32)
    private ThreadType threadType = ThreadType.PUBLIC_THREAD;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "ticket_guild_support_roles",
            joinColumns = @JoinColumn(name = "guild_id")
    )
    @Column(name = "role_id", nullable = false, length = 20)
    private List<String> supportRoleIds = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "ticket_guild_categories",
            joinColumns = @JoinColumn(name = "guild_id")
    )
    @OrderColumn(name = "sort_order")
    private List<TicketCategory> categories = new ArrayList<>();

    public TicketGuildConfig() {
    }

    public TicketGuildConfig(String guildId) {
        this.guildId = guildId;
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public String getTicketChannelId() {
        return ticketChannelId;
    }

    public void setTicketChannelId(String ticketChannelId) {
        this.ticketChannelId = ticketChannelId;
    }

    public String getLogChannelId() {
        return logChannelId;
    }

    public void setLogChannelId(String logChannelId) {
        this.logChannelId = logChannelId;
    }

    public String getTicketMessageId() {
        return ticketMessageId;
    }

    public void setTicketMessageId(String ticketMessageId) {
        this.ticketMessageId = ticketMessageId;
    }

    public ThreadType getThreadType() {
        return threadType;
    }

    public void setThreadType(ThreadType threadType) {
        this.threadType = threadType;
    }

    public List<String> getSupportRoleIds() {
        return supportRoleIds;
    }

    public void setSupportRoleIds(List<String> supportRoleIds) {
        this.supportRoleIds = supportRoleIds;
    }

    public List<TicketCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<TicketCategory> categories) {
        this.categories = categories;
    }

    public enum ThreadType {
        PRIVATE_THREAD,
        PUBLIC_THREAD
    }

    @Embeddable
    public static class TicketCategory {

        @NotBlank
        @Size(max = 80)
        @Column(name = "label", nullable = false, length = 80)
        private String label;

        @Size(max = 16)
        @Column(name = "emoji", length = 16)
        private String emoji;

        @Size(max = 100)
        @Column(name = "description", length = 100)
        private String description;

        public TicketCategory() {
        }

        public TicketCategory(String label, String emoji, String description) {
            this.label = label;
            this.emoji = emoji;
            this.description = description;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getEmoji() {
            return emoji;
        }

        public void setEmoji(String emoji) {
            this.emoji = emoji;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}