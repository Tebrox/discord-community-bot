package de.tebrox.communitybot.ticket.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_id", nullable = false, unique = true, length = 16)
    private String shortId;

    @Column(name = "guild_id", nullable = false, length = 20)
    private String guildId;

    @Column(name = "channel_id", nullable = false, length = 20)
    private String channelId;

    @Column(name = "thread_id", nullable = false, unique = true, length = 20)
    private String threadId;

    @Column(name = "creator_id", nullable = false, length = 20)
    private String creatorId;

    @Column(name = "creator_name", length = 100)
    private String creatorName;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "closed_by_user_id", length = 20)
    private String closedByUserId;

    public Ticket() {
    }

    public Long getId() {
        return id;
    }

    public String getShortId() {
        return shortId;
    }

    public void setShortId(String shortId) {
        this.shortId = shortId;
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public String getClosedByUserId() {
        return closedByUserId;
    }

    public void setClosedByUserId(String closedByUserId) {
        this.closedByUserId = closedByUserId;
    }
}