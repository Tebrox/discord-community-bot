package de.tebrox.communitybot.dashboard.runtime;

import de.tebrox.communitybot.ticket.service.TicketService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "discord.ticket", name = "enabled", havingValue = "true", matchIfMissing = true)
public class JdaTicketDashboardRuntimeGateway implements TicketDashboardRuntimeGateway {

    private final JDA ticketJda;
    private final TicketService ticketService;

    public JdaTicketDashboardRuntimeGateway(
            @Qualifier("ticketJda") JDA ticketJda,
            TicketService ticketService
    ) {
        this.ticketJda = ticketJda;
        this.ticketService = ticketService;
    }

    @Override
    public boolean isAvailableForGuild(String guildId) {
        return ticketJda.getGuildById(guildId) != null;
    }

    @Override
    public boolean canAccessTextChannel(String guildId, String channelId) {
        Guild guild = ticketJda.getGuildById(guildId);
        return guild != null && guild.getTextChannelById(channelId) != null;
    }

    @Override
    public void repostPanel(String guildId, String channelId) {
        Guild guild = ticketJda.getGuildById(guildId);
        if (guild == null) {
            throw new IllegalStateException("Ticket guild not available: " + guildId);
        }

        TextChannel channel = guild.getTextChannelById(channelId);
        if (channel == null) {
            throw new IllegalStateException("Ticket text channel not available: " + channelId);
        }

        ticketService.postTicketPanel(guild, channel, true);
    }

    @Override
    public void postTestPanel(String guildId, String channelId) {
        Guild guild = ticketJda.getGuildById(guildId);
        if (guild == null) {
            throw new IllegalStateException("Ticket guild not available: " + guildId);
        }

        TextChannel channel = guild.getTextChannelById(channelId);
        if (channel == null) {
            throw new IllegalStateException("Ticket text channel not available: " + channelId);
        }

        ticketService.postTicketPanel(guild, channel, false);
    }
}
