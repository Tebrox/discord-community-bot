package de.tebrox.communitybot.dashboard.service;

import java.util.List;
import java.util.Optional;

public interface DashboardDiscordService {

    List<GuildInfo> listGuilds();
    Optional<GuildInfo> getGuild(String guildId);

    List<RoleInfo> listRoles(String guildId);
    Optional<RoleInfo> getRole(String guildId, String roleId);

    List<TextChannelInfo> listTextChannels(String guildId);

    List<MemberInfo> getMembersWithRole(String guildId, String roleId);

    record GuildInfo(String id, String name, String iconUrl, int memberCount) {}
    record RoleInfo(String id, String name) {}
    record TextChannelInfo(String id, String name) {}
    record MemberInfo(String id, String username, String displayName) {}
}
