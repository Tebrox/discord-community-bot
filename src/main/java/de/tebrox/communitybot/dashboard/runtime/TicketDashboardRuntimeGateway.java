package de.tebrox.communitybot.dashboard.runtime;

public interface TicketDashboardRuntimeGateway {

    boolean isAvailableForGuild(String guildId);

    boolean canAccessTextChannel(String guildId, String channelId);

    void repostPanel(String guildId, String channelId);

    void postTestPanel(String guildId, String channelId);
}
