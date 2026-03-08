package de.tebrox.communitybot.ticket.persistence.repository;

import de.tebrox.communitybot.ticket.persistence.entity.TicketGuildConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketGuildConfigRepository extends JpaRepository<TicketGuildConfig, String> {
}