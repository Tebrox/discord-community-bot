package de.tebrox.communitybot.ticket.service;

import de.tebrox.communitybot.core.util.SnowflakeValidator;
import de.tebrox.communitybot.ticket.persistence.entity.TicketGuildConfig;
import de.tebrox.communitybot.ticket.persistence.repository.TicketGuildConfigRepository;
import de.tebrox.communitybot.ticket.web.dto.TicketGuildConfigUpdateRequest;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@ConditionalOnProperty(prefix = "discord.ticket", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TicketGuildConfigService {

    private final TicketGuildConfigRepository repository;
    private final JDA ticketJda;

    public TicketGuildConfigService(
            TicketGuildConfigRepository repository,
            @Qualifier("ticketJda") JDA ticketJda
    ) {
        this.repository = repository;
        this.ticketJda = ticketJda;
    }

    @Transactional(readOnly = true)
    public Optional<TicketGuildConfig> findByGuildId(String guildId) {
        return repository.findById(guildId);
    }

    @Transactional
    public TicketGuildConfig getOrCreate(String guildId) {
        validateGuildExists(guildId);
        return repository.findById(guildId)
                .orElseGet(() -> repository.save(new TicketGuildConfig(guildId)));
    }

    @Transactional
    public TicketGuildConfig save(String guildId, TicketGuildConfigUpdateRequest request) {
        Guild guild = validateGuildExists(guildId);

        TicketGuildConfig config = repository.findById(guildId)
                .orElseGet(() -> new TicketGuildConfig(guildId));

        if (request.ticketChannelId() != null && !request.ticketChannelId().isBlank()) {
            SnowflakeValidator.validate(request.ticketChannelId(), "ticketChannelId");
            if (guild.getTextChannelById(request.ticketChannelId()) == null) {
                throw new IllegalArgumentException("Ticket-Channel existiert in dieser Guild nicht.");
            }
            config.setTicketChannelId(request.ticketChannelId());
        } else {
            config.setTicketChannelId(null);
        }

        if (request.logChannelId() != null && !request.logChannelId().isBlank()) {
            SnowflakeValidator.validate(request.logChannelId(), "logChannelId");
            if (guild.getTextChannelById(request.logChannelId()) == null) {
                throw new IllegalArgumentException("Log-Channel existiert in dieser Guild nicht.");
            }
            config.setLogChannelId(request.logChannelId());
        } else {
            config.setLogChannelId(null);
        }

        config.setEmbedTitle(
                request.embedTitle() == null || request.embedTitle().isBlank()
                        ? "Support Ticket"
                        : request.embedTitle()
        );

        config.setEmbedDescription(
                request.embedDescription() == null || request.embedDescription().isBlank()
                        ? "Klicke auf einen Button, um ein Ticket zu eröffnen."
                        : request.embedDescription()
        );

        if (request.threadType() != null) {
            config.setThreadType(request.threadType());
        }

        config.getSupportRoleIds().clear();
        if (request.supportRoleIds() != null) {
            for (String roleId : request.supportRoleIds()) {
                SnowflakeValidator.validate(roleId, "roleId");
                if (guild.getRoleById(roleId) == null) {
                    throw new IllegalArgumentException("Support-Rolle existiert in dieser Guild nicht: " + roleId);
                }
                config.getSupportRoleIds().add(roleId);
            }
        }

        config.getCategories().clear();
        if (request.categories() != null) {
            for (TicketGuildConfigUpdateRequest.CategoryDto category : request.categories()) {
                config.getCategories().add(new TicketGuildConfig.TicketCategory(
                        category.label(),
                        category.emoji(),
                        category.description()
                ));
            }
        }

        return repository.save(config);
    }

    @Transactional
    public void saveTicketMessageId(String guildId, String messageId) {
        TicketGuildConfig config = getOrCreate(guildId);
        config.setTicketMessageId(messageId);
        repository.save(config);
    }

    private Guild validateGuildExists(String guildId) {
        SnowflakeValidator.validate(guildId, "guildId");
        Guild guild = ticketJda.getGuildById(guildId);
        if (guild == null) {
            throw new IllegalArgumentException("Guild nicht gefunden oder Ticket-Bot ist nicht auf dem Server.");
        }
        return guild;
    }
}