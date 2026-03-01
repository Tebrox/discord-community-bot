package de.tebrox.rolesbot.repository;

import de.tebrox.rolesbot.persistence.entity.GuildConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuildConfigRepository extends JpaRepository<GuildConfigEntity, String> {
}
