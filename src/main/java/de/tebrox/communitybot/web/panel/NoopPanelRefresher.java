package de.tebrox.communitybot.web.panel;

import de.tebrox.communitybot.config.GuildConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name="discord.enabled", havingValue = "false")
public class NoopPanelRefresher implements PanelRefresher {
    @Override
    public void refresh(String guildId, GuildConfig cfg) {}
}
