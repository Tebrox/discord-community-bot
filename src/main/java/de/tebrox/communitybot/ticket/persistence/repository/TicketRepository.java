package de.tebrox.communitybot.ticket.persistence.repository;

import de.tebrox.communitybot.ticket.persistence.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByThreadIdAndClosedAtIsNull(String threadId);

    long countByGuildIdAndCreatorIdAndClosedAtIsNull(String guildId, String creatorId);
}