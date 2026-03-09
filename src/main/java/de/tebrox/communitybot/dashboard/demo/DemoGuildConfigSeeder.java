package de.tebrox.communitybot.dashboard.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tebrox.communitybot.community.config.CommunityGuildConfig;
import de.tebrox.communitybot.community.persistence.entity.CommunityGuildConfigEntity;
import de.tebrox.communitybot.community.persistence.repository.GuildConfigRepository;
import de.tebrox.communitybot.dashboard.service.DemoDashboardDiscordService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@ConditionalOnProperty(name="dashboard.demo", havingValue = "true")
public class DemoGuildConfigSeeder implements CommandLineRunner {

    private final GuildConfigRepository repo;
    private final ObjectMapper om;

    public DemoGuildConfigSeeder(GuildConfigRepository repo, ObjectMapper om) {
        this.repo = repo;
        this.om = om;
    }

    @Override
    public void run(String... args) throws Exception {
        String guildId = DemoDashboardDiscordService.DEMO_GUILD_ID;
        if(repo.existsById(guildId)) return;

        CommunityGuildConfig cfg = new CommunityGuildConfig();
        cfg.setButtons(List.of(
                btn("info", "Info-Pings", "111111111111111111", "PRIMARY"),
                btn("events", "Events", "222222222222222222", "SUCCESS"),
                btn("changelog", "Changelog", "333333333333333333", "SECONDARY"),
                btn("polls", "Umfragen", "444444444444444444", "DANGER")
        ));

        cfg.getWelcome().setEnabled(true);
        cfg.getWelcome().setChannelId("777777777777777777");

        String json = om.writeValueAsString(cfg);

        CommunityGuildConfigEntity e = CommunityGuildConfigEntity.builder()
                .guildId(guildId)
                .guildName("DEV • Demo Guild")
                .configJson(json)
                .updatedAt(Instant.now())
                .build();

        repo.save(e);
    }

    private static CommunityGuildConfig.ButtonConfig btn(String id, String label, String roleId, String style) {
        CommunityGuildConfig.ButtonConfig b = new CommunityGuildConfig.ButtonConfig();
        b.setId(id);
        b.setLabel(label);
        b.setRoleId(roleId);
        b.setStyle(style);

        return b;
    }
}
