package de.tebrox.communitybot.core.message.persistence.repository;

import de.tebrox.communitybot.core.message.MessageKey;
import de.tebrox.communitybot.core.message.persistence.entity.GuildMessageConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuildMessageConfigRepository extends JpaRepository<GuildMessageConfigEntity, Long> {

    Optional<GuildMessageConfigEntity> findByGuildIdAndMessageKey(String guildId, MessageKey messageKey);

    List<GuildMessageConfigEntity> findAllByGuildIdOrderByMessageKeyAsc(String guildId);
}
