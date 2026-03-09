package de.tebrox.communitybot.dashboard.runtime;

import de.tebrox.communitybot.community.config.CommunityGuildConfig;
import de.tebrox.communitybot.dashboard.service.DashboardDiscordService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "discord.community", name = "enabled", havingValue = "false")
public class DemoCommunityDashboardRuntimeGateway implements CommunityDashboardRuntimeGateway {

    private final DashboardDiscordService discord;

    public DemoCommunityDashboardRuntimeGateway(DashboardDiscordService discord) {
        this.discord = discord;
    }

    @Override
    public boolean isAvailableForGuild(String guildId) {
        return discord.getGuild(guildId).isPresent();
    }

    @Override
    public void refreshRolePanel(String guildId, CommunityGuildConfig cfg) {
        // no-op im Demo-Modus
    }
}
