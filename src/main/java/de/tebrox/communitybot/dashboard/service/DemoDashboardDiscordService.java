package de.tebrox.communitybot.dashboard.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@ConditionalOnProperty(name="dashboard.demo", havingValue = "true")
public class DemoDashboardDiscordService implements DashboardDiscordService {

    public static final String DEMO_GUILD_ID = "123456789012345678";

    private final List<GuildInfo> guilds = List.of(
            new GuildInfo(DEMO_GUILD_ID, "OBNL • Demo Guild", null, 42),
            new GuildInfo("987654321098765432", "Test-Server", null, 7)
    );

    private final Map<String, RoleInfo> roles = new LinkedHashMap<>();
    private final Map<String, List<MemberInfo>> membersByRole = new HashMap<>();
    private final List<TextChannelInfo> channels = List.of(
            new TextChannelInfo("777777777777777777", "welcome"),
            new TextChannelInfo("888888888888888888", "announcements"),
            new TextChannelInfo("999999999999999999", "chat")
    );

    public DemoDashboardDiscordService() {
        roles.put("111111111111111111", new RoleInfo("111111111111111111", "Info-Pings"));
        roles.put("222222222222222222", new RoleInfo("222222222222222222", "Events"));
        roles.put("333333333333333333", new RoleInfo("333333333333333333", "Changelog"));
        roles.put("444444444444444444", new RoleInfo("444444444444444444", "Umfragen"));

        membersByRole.put("111111111111111111", List.of(
                new MemberInfo("5001", "Tebrox", "Tebrox"),
                new MemberInfo("5002", "PlayerTwo", "PlayerTwo"),
                new MemberInfo("5003", "Kira", "Kira ✨")
        ));

        membersByRole.put("222222222222222222", List.of(
                new MemberInfo("5002", "PlayerTwo", "PlayerTwo"),
                new MemberInfo("5004", "Noah", "Noah")
        ));

        membersByRole.put("333333333333333333", List.of(
                new MemberInfo("5001", "Erik", "Erik"),
                new MemberInfo("5005", "Mirella", "Mirella")
        ));

        membersByRole.put("444444444444444444", List.of(
                new MemberInfo("5006", "Helena", "Helena"),
                new MemberInfo("5007", "Oliver", "Oliver")
        ));
    }

    @Override
    public List<GuildInfo> listGuilds() {
        return guilds;
    }

    @Override
    public Optional<GuildInfo> getGuild(String guildId) {
        return guilds.stream().filter(g -> g.id().equals(guildId)).findFirst();
    }

    @Override
    public List<RoleInfo> listRoles(String guildId) {
        if(!DEMO_GUILD_ID.equals(guildId)) return List.of();
        return new ArrayList<>(roles.values());
    }

    @Override
    public Optional<RoleInfo> getRole(String guildId, String roleId) {
        if(!DEMO_GUILD_ID.equals(guildId)) return Optional.empty();
        return Optional.ofNullable(roles.get(roleId));
    }

    @Override
    public List<TextChannelInfo> listTextChannels(String guildId) {
        if(!DEMO_GUILD_ID.equals(guildId)) return List.of();
        return channels;
    }

    @Override
    public List<MemberInfo> getMembersWithRole(String guildId, String roleId) {
        if(!DEMO_GUILD_ID.equals(guildId)) return List.of();
        return membersByRole.getOrDefault(roleId, List.of());
    }
}
