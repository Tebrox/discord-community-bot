package de.tebrox.communitybot.dashboard.panel;

import de.tebrox.communitybot.community.config.CommunityGuildConfig;

public interface PanelRefresher {
    void refresh(String guildId, CommunityGuildConfig cfg);
}
