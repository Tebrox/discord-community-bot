package de.tebrox.communitybot.web.controller;

import de.tebrox.communitybot.web.discord.DashboardDiscordService;
import de.tebrox.communitybot.web.dto.GuildDto;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bot")
public class BotApiController {

    private final DashboardDiscordService discord;

    public BotApiController(DashboardDiscordService discord) {
        this.discord = discord;
    }

    @GetMapping("/guilds")
    public ResponseEntity<List<GuildDto>> getGuilds() {
        List<GuildDto> guilds = discord.listGuilds().stream()
                .map(g -> new GuildDto(g.id(), g.name(), g.iconUrl()))
                .toList();
        return ResponseEntity.ok(guilds);
    }

    private GuildDto toDto(Guild guild) {
        return new GuildDto(guild.getId(), guild.getName(), guild.getIconUrl());
    }
}