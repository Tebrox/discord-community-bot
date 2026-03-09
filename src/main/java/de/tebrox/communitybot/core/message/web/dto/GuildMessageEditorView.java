package de.tebrox.communitybot.core.message.web.dto;

import de.tebrox.communitybot.core.message.DefaultMessageDefinition;
import de.tebrox.communitybot.core.message.MessageKey;
import de.tebrox.communitybot.core.message.persistence.entity.GuildMessageConfigEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GuildMessageEditorView {
    private String guildId;
    private MessageKey key;
    private String category;
    private String label;

    private boolean custom;

    private boolean enabled;
    private String content;
    private boolean embedEnabled;
    private String embedTitle;
    private String embedDescription;
    private String embedFooter;
    private String embedColor;
    private String thumbnailUrl;
    private String imageUrl;

    private boolean allowContent;
    private boolean allowEmbed;
    private List<String> placeholders;

    public static GuildMessageEditorView from(
            String guildId,
            MessageKey key,
            DefaultMessageDefinition defaults,
            GuildMessageConfigEntity entity
    ) {
        boolean custom = entity != null;

        return GuildMessageEditorView.builder()
                .guildId(guildId)
                .key(key)
                .category(key.category())
                .label(key.label())
                .custom(custom)
                .enabled(custom ? entity.isEnabled() : defaults.isEnabled())
                .content(custom && entity.getContent() != null ? entity.getContent() : defaults.getContent())
                .embedEnabled(custom ? entity.isEmbedEnabled() : defaults.isEmbedEnabled())
                .embedTitle(custom && entity.getEmbedTitle() != null ? entity.getEmbedTitle() : defaults.getEmbedTitle())
                .embedDescription(custom && entity.getEmbedDescription() != null ? entity.getEmbedDescription() : defaults.getEmbedDescription())
                .embedFooter(custom && entity.getEmbedFooter() != null ? entity.getEmbedFooter() : defaults.getEmbedFooter())
                .embedColor(custom && entity.getEmbedColor() != null ? entity.getEmbedColor() : defaults.getEmbedColor())
                .thumbnailUrl(custom && entity.getThumbnailUrl() != null ? entity.getThumbnailUrl() : defaults.getThumbnailUrl())
                .imageUrl(custom && entity.getImageUrl() != null ? entity.getImageUrl() : defaults.getImageUrl())
                .allowContent(defaults.isAllowContent())
                .allowEmbed(defaults.isAllowEmbed())
                .placeholders(defaults.getPlaceholders())
                .build();
    }
}
