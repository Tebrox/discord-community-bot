package de.tebrox.communitybot.web.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@ConditionalOnProperty(name="dashboard.demo", havingValue = "false", matchIfMissing = true)
public class LiveDashboardDiscordService implements DashboardDiscordService {

    private final JDA jda;

    public LiveDashboardDiscordService(JDA jda) {
        this.jda = jda;
    }

    @Override
    public List<GuildInfo> listGuilds() {
        return jda.getGuilds().stream()
                .map(g -> new GuildInfo(g.getId(), g.getName(), g.getIconUrl(), g.getMemberCount()))
                .toList();
    }

    @Override
    public Optional<GuildInfo> getGuild(String guildId) {
        Guild g = jda.getGuildById(guildId);
        if (g == null) return Optional.empty();
        return Optional.of(new GuildInfo(g.getId(), g.getName(), g.getIconUrl(), g.getMemberCount()));
    }

    @Override
    public List<RoleInfo> listRoles(String guildId) {
        Guild g = jda.getGuildById(guildId);
        if (g == null) return List.of();
        return g.getRoles().stream().map(r -> new RoleInfo(r.getId(), r.getName())).toList();
    }

    @Override
    public Optional<RoleInfo> getRole(String guildId, String roleId) {
        Guild g = jda.getGuildById(guildId);
        if (g == null) return Optional.empty();
        Role r = g.getRoleById(roleId);
        if (r == null) return Optional.empty();
        return Optional.of(new RoleInfo(r.getId(), r.getName()));
    }

    @Override
    public List<TextChannelInfo> listTextChannels(String guildId) {
        Guild g = jda.getGuildById(guildId);
        if (g == null) return List.of();
        return g.getTextChannels().stream()
                .map(ch -> new TextChannelInfo(ch.getId(), ch.getName()))
                .toList();
    }

    @Override
    public List<MemberInfo> getMembersWithRole(String guildId, String roleId) {
        Guild g = jda.getGuildById(guildId);
        if (g == null) return List.of();
        Role r = g.getRoleById(roleId);
        if (r == null) return List.of();
        return g.getMembersWithRoles(r).stream()
                .map(m -> new MemberInfo(m.getId(), m.getUser().getName(), m.getEffectiveName()))
                .toList();
    }
}
