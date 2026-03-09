package de.tebrox.communitybot.core.message.service;

import de.tebrox.communitybot.core.message.DefaultMessageDefinition;
import de.tebrox.communitybot.core.message.DefaultMessageRegistry;
import de.tebrox.communitybot.core.message.MessageKey;
import de.tebrox.communitybot.core.message.persistence.entity.GuildMessageConfigEntity;
import de.tebrox.communitybot.core.message.persistence.repository.GuildMessageConfigRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GuildMessageService {

    private static final Logger log = LoggerFactory.getLogger(GuildMessageService.class);

    private final GuildMessageConfigRepository repository;
    private final DefaultMessageRegistry defaultRegistry;
    private final MessageTemplateRenderer renderer;

    public ResolvedMessage resolve(String guildId, MessageKey key, Map<String, String> placeholders) {
        DefaultMessageDefinition defaults = defaultRegistry.get(key);
        if (defaults == null) {
            throw new IllegalArgumentException("Keine Default-Definition für MessageKey vorhanden: " + key);
        }

        GuildMessageConfigEntity entity = repository.findByGuildIdAndMessageKey(guildId, key).orElse(null);
        boolean custom = entity != null;

        boolean enabled = custom ? entity.isEnabled() : defaults.isEnabled();

        String content = renderer.render(
                custom && entity.getContent() != null ? entity.getContent() : defaults.getContent(),
                placeholders
        );

        boolean embedEnabled = custom ? entity.isEmbedEnabled() : defaults.isEmbedEnabled();

        String embedTitle = renderer.render(
                custom && entity.getEmbedTitle() != null ? entity.getEmbedTitle() : defaults.getEmbedTitle(),
                placeholders
        );

        String embedDescription = renderer.render(
                custom && entity.getEmbedDescription() != null ? entity.getEmbedDescription() : defaults.getEmbedDescription(),
                placeholders
        );

        String embedFooter = renderer.render(
                custom && entity.getEmbedFooter() != null ? entity.getEmbedFooter() : defaults.getEmbedFooter(),
                placeholders
        );

        String embedColor = custom && entity.getEmbedColor() != null
                ? entity.getEmbedColor()
                : defaults.getEmbedColor();

        String thumbnailUrl = renderer.render(
                custom && entity.getThumbnailUrl() != null ? entity.getThumbnailUrl() : defaults.getThumbnailUrl(),
                placeholders
        );

        String imageUrl = renderer.render(
                custom && entity.getImageUrl() != null ? entity.getImageUrl() : defaults.getImageUrl(),
                placeholders
        );

        log.info("Resolved message key={} guildId={} custom={} enabled={}", key, guildId, custom, enabled);

        return ResolvedMessage.builder()
                .enabled(enabled)
                .content(content)
                .embedEnabled(embedEnabled)
                .embedTitle(embedTitle)
                .embedDescription(embedDescription)
                .embedFooter(embedFooter)
                .embedColor(embedColor)
                .thumbnailUrl(thumbnailUrl)
                .imageUrl(imageUrl)
                .custom(custom)
                .build();
    }

    public ResolvedMessage resolveDefault(MessageKey key, Map<String, String> placeholders) {

        DefaultMessageDefinition defaults = defaultRegistry.get(key);
        if (defaults == null) {
            throw new IllegalArgumentException("Keine Default-Definition für MessageKey vorhanden: " + key);
        }

        String content = renderer.render(defaults.getContent(), placeholders);

        String embedTitle = renderer.render(defaults.getEmbedTitle(), placeholders);

        String embedDescription = renderer.render(defaults.getEmbedDescription(), placeholders);

        String embedFooter = renderer.render(defaults.getEmbedFooter(), placeholders);

        String thumbnailUrl = renderer.render(defaults.getThumbnailUrl(), placeholders);

        String imageUrl = renderer.render(defaults.getImageUrl(), placeholders);

        log.info("Resolved DEFAULT message key={}", key);

        return ResolvedMessage.builder()
                .enabled(defaults.isEnabled())
                .content(content)
                .embedEnabled(defaults.isEmbedEnabled())
                .embedTitle(embedTitle)
                .embedDescription(embedDescription)
                .embedFooter(embedFooter)
                .embedColor(defaults.getEmbedColor())
                .thumbnailUrl(thumbnailUrl)
                .imageUrl(imageUrl)
                .custom(false)
                .build();
    }
}
