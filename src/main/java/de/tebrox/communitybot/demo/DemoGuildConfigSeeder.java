package de.tebrox.communitybot.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tebrox.communitybot.config.GuildConfig;
import de.tebrox.communitybot.persistence.entity.GuildConfigEntity;
import de.tebrox.communitybot.repository.GuildConfigRepository;
import de.tebrox.communitybot.web.discord.DemoDashboardDiscordService;
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

        GuildConfig cfg = new GuildConfig();
        cfg.setButtons(List.of(
                btn("info", "Info-Pings", "111111111111111111", "PRIMARY"),
                btn("events", "Events", "222222222222222222", "SUCCESS"),
                btn("changelog", "Changelog", "333333333333333333", "SECONDARY"),
                btn("polls", "Umfragen", "444444444444444444", "DANGER")
        ));

        cfg.getWelcome().setEnabled(true);
        cfg.getWelcome().setChannelId("777777777777777777");
        cfg.getWelcome().getEmbed().setEnabled(true);
        cfg.getWelcome().getEmbed().setTitle("Willkommen (Demo)");
        cfg.getWelcome().getEmbed().setDescription("Hey {mention}, willkommen auf {server}! (Demo)");

        String json = om.writeValueAsString(cfg);

        GuildConfigEntity e = GuildConfigEntity.builder()
                .guildId(guildId)
                .guildName("DEV • Demo Guild")
                .configJson(json)
                .updatedAt(Instant.now())
                .build();

        repo.save(e);
    }

    private static GuildConfig.ButtonConfig btn(String id, String label, String roleId, String style) {
        GuildConfig.ButtonConfig b = new GuildConfig.ButtonConfig();
        b.setId(id);
        b.setLabel(label);
        b.setRoleId(roleId);
        b.setStyle(style);

        return b;
    }
}
