package de.tebrox.communitybot.web.controller;

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

    private final JDA jda;

    public BotApiController(JDA jda) {
        this.jda = jda;
    }

    @GetMapping("/guilds")
    public ResponseEntity<List<GuildDto>> getGuilds() {
        List<GuildDto> guilds = jda.getGuilds().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(guilds);
    }

    private GuildDto toDto(Guild guild) {
        return new GuildDto(guild.getId(), guild.getName(), guild.getIconUrl());
    }
}