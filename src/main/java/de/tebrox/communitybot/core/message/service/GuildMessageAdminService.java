package de.tebrox.communitybot.core.message.service;

import de.tebrox.communitybot.core.message.DefaultMessageDefinition;
import de.tebrox.communitybot.core.message.DefaultMessageRegistry;
import de.tebrox.communitybot.core.message.MessageKey;
import de.tebrox.communitybot.core.message.persistence.entity.GuildMessageConfigEntity;
import de.tebrox.communitybot.core.message.persistence.repository.GuildMessageConfigRepository;
import de.tebrox.communitybot.core.message.web.dto.GuildMessageConfigRequest;
import de.tebrox.communitybot.core.message.web.dto.GuildMessageEditorView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GuildMessageAdminService {

    private final GuildMessageConfigRepository repository;
    private final DefaultMessageRegistry defaultRegistry;

    public List<GuildMessageEditorView> getEditorViews(String guildId) {
        List<GuildMessageEditorView> result = new ArrayList<>();

        for (MessageKey key : MessageKey.values()) {
            DefaultMessageDefinition defaults = defaultRegistry.get(key);
            GuildMessageConfigEntity entity = repository.findByGuildIdAndMessageKey(guildId, key).orElse(null);

            result.add(GuildMessageEditorView.from(guildId, key, defaults, entity));
        }

        return result;
    }

    public GuildMessageEditorView getEditorView(String guildId, MessageKey key) {
        DefaultMessageDefinition defaults = defaultRegistry.get(key);
        GuildMessageConfigEntity entity = repository.findByGuildIdAndMessageKey(guildId, key).orElse(null);
        return GuildMessageEditorView.from(guildId, key, defaults, entity);
    }

    @Transactional
    public void save(String guildId, MessageKey key, GuildMessageConfigRequest request) {
        GuildMessageConfigEntity entity = repository.findByGuildIdAndMessageKey(guildId, key)
                .orElseGet(() -> GuildMessageConfigEntity.builder()
                        .guildId(guildId)
                        .messageKey(key)
                        .build());

        entity.setEnabled(request.isEnabled());
        entity.setContent(request.getContent());
        entity.setEmbedEnabled(request.isEmbedEnabled());
        entity.setEmbedTitle(request.getEmbedTitle());
        entity.setEmbedDescription(request.getEmbedDescription());
        entity.setEmbedFooter(request.getEmbedFooter());
        entity.setEmbedColor(request.getEmbedColor());
        entity.setThumbnailUrl(request.getThumbnailUrl());
        entity.setImageUrl(request.getImageUrl());
        entity.setUpdatedAt(Instant.now());

        repository.save(entity);
    }

    @Transactional
    public void resetToDefault(String guildId, MessageKey key) {
        repository.findByGuildIdAndMessageKey(guildId, key).ifPresent(repository::delete);
    }
}
