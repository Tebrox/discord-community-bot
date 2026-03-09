package de.tebrox.communitybot.ticket.web.dto;

import de.tebrox.communitybot.ticket.persistence.entity.TicketGuildConfig;

import java.util.List;

public record TicketGuildConfigUpdateRequest(
        String ticketChannelId,
        String logChannelId,
        TicketGuildConfig.ThreadType threadType,
        List<String> supportRoleIds,
        List<CategoryDto> categories
) {
    public record CategoryDto(
            String label,
            String emoji,
            String description
    ) {
    }
}