package de.tebrox.communitybot.web.panel;

import de.tebrox.communitybot.config.GuildConfig;

public interface PanelRefresher {
    void refresh(String guildId, GuildConfig cfg);
}
