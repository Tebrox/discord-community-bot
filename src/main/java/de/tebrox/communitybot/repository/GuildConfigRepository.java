package de.tebrox.communitybot.repository;

import de.tebrox.communitybot.persistence.entity.GuildConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuildConfigRepository extends JpaRepository<GuildConfigEntity, String> {
}
