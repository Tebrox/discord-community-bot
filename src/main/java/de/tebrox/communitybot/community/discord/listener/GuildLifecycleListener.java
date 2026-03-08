package de.tebrox.communitybot.community.discord.listener;

import de.tebrox.communitybot.community.service.CommunityGuildConfigService;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class GuildLifecycleListener extends ListenerAdapter {

    private final CommunityGuildConfigService configManager;

    public GuildLifecycleListener(CommunityGuildConfigService configManager) {
        this.configManager = configManager;
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        configManager.onGuildJoin(event.getGuild().getId(), event.getGuild().getName());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        configManager.onGuildLeave(event.getGuild().getId(), event.getGuild().getName());
    }
}
