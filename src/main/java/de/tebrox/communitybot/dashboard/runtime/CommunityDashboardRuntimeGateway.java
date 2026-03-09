package de.tebrox.communitybot.dashboard.runtime;

import de.tebrox.communitybot.community.config.CommunityGuildConfig;

public interface CommunityDashboardRuntimeGateway {
    boolean isAvailableForGuild(String guildId);

    void refreshRolePanel(String guildId, CommunityGuildConfig cfg);
}
