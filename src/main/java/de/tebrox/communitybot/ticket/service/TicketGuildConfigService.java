package de.tebrox.communitybot.ticket.service;

import de.tebrox.communitybot.core.util.SnowflakeValidator;
import de.tebrox.communitybot.dashboard.service.DashboardDiscordService;
import de.tebrox.communitybot.ticket.persistence.entity.TicketGuildConfig;
import de.tebrox.communitybot.ticket.persistence.repository.TicketGuildConfigRepository;
import de.tebrox.communitybot.ticket.web.dto.TicketGuildConfigUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TicketGuildConfigService {

    private final TicketGuildConfigRepository repository;
    private final DashboardDiscordService dashboardDiscordService;

    public TicketGuildConfigService(
            TicketGuildConfigRepository repository,
            DashboardDiscordService dashboardDiscordService
    ) {
        this.repository = repository;
        this.dashboardDiscordService = dashboardDiscordService;
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
        validateGuildExists(guildId);

        TicketGuildConfig config = repository.findById(guildId)
                .orElseGet(() -> new TicketGuildConfig(guildId));

        if (request.ticketChannelId() != null && !request.ticketChannelId().isBlank()) {
            SnowflakeValidator.validate(request.ticketChannelId(), "ticketChannelId");
            boolean exists = dashboardDiscordService.listTextChannels(guildId).stream()
                    .anyMatch(channel -> channel.id().equals(request.ticketChannelId()));
            if (!exists) {
                throw new IllegalArgumentException("Ticket-Channel existiert in dieser Guild nicht.");
            }
            config.setTicketChannelId(request.ticketChannelId());
        } else {
            config.setTicketChannelId(null);
        }

        if (request.logChannelId() != null && !request.logChannelId().isBlank()) {
            SnowflakeValidator.validate(request.logChannelId(), "logChannelId");
            boolean exists = dashboardDiscordService.listTextChannels(guildId).stream()
                    .anyMatch(channel -> channel.id().equals(request.logChannelId()));
            if (!exists) {
                throw new IllegalArgumentException("Log-Channel existiert in dieser Guild nicht.");
            }
            config.setLogChannelId(request.logChannelId());
        } else {
            config.setLogChannelId(null);
        }

        if (request.threadType() != null) {
            config.setThreadType(request.threadType());
        }

        config.getSupportRoleIds().clear();
        if (request.supportRoleIds() != null) {
            for (String roleId : request.supportRoleIds()) {
                SnowflakeValidator.validate(roleId, "roleId");
                boolean exists = dashboardDiscordService.listRoles(guildId).stream()
                        .anyMatch(role -> role.id().equals(roleId));
                if (!exists) {
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

    private void validateGuildExists(String guildId) {
        SnowflakeValidator.validate(guildId, "guildId");
        if (dashboardDiscordService.getGuild(guildId).isEmpty()) {
            throw new IllegalArgumentException("Guild nicht gefunden.");
        }
    }
}