package de.tebrox.communitybot.dashboard.runtime;

import de.tebrox.communitybot.dashboard.service.DashboardDiscordService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "discord.ticket", name = "enabled", havingValue = "false")
public class DemoTicketDashboardRuntimeGateway implements TicketDashboardRuntimeGateway {

    private final DashboardDiscordService discord;

    public DemoTicketDashboardRuntimeGateway(DashboardDiscordService discord) {
        this.discord = discord;
    }

    @Override
    public boolean isAvailableForGuild(String guildId) {
        return discord.getGuild(guildId).isPresent();
    }

    @Override
    public boolean canAccessTextChannel(String guildId, String channelId) {
        return discord.listTextChannels(guildId)
                .stream()
                .anyMatch(c -> c.id().equals(channelId));
    }

    @Override
    public void repostPanel(String guildId, String channelId) {
        // no-op im Demo-Modus
    }

    @Override
    public void postTestPanel(String guildId, String channelId) {
        // no-op im Demo-Modus
    }
}
