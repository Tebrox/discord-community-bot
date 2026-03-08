package de.tebrox.communitybot.dashboard.panel;

import de.tebrox.communitybot.community.config.CommunityGuildConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name="discord.enabled", havingValue = "false")
public class NoopPanelRefresher implements PanelRefresher {
    @Override
    public void refresh(String guildId, CommunityGuildConfig cfg) {}
}
