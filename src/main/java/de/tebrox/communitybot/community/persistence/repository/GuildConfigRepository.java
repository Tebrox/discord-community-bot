package de.tebrox.communitybot.community.persistence.repository;

import de.tebrox.communitybot.community.persistence.entity.CommunityGuildConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuildConfigRepository extends JpaRepository<CommunityGuildConfigEntity, String> {
}
