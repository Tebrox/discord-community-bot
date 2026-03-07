package de.tebrox.communitybot.web.controller;

import de.tebrox.communitybot.security.DashboardPermission;
import de.tebrox.communitybot.service.DashboardAccessService;
import de.tebrox.communitybot.web.discord.DashboardDiscordService;
import de.tebrox.communitybot.web.dto.GuildDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bot")
public class BotApiController {

    private final DashboardDiscordService discord;
    private final DashboardAccessService accessService;

    public BotApiController(DashboardDiscordService discord,
                            DashboardAccessService accessService) {
        this.discord = discord;
        this.accessService = accessService;
    }

    @GetMapping("/guilds")
    public ResponseEntity<List<GuildDto>> getGuilds() {
        if (!accessService.canAccessDashboard()) {
            return ResponseEntity.status(403).build();
        }

        List<GuildDto> guilds = discord.listGuilds().stream()
                .filter(g -> accessService.hasGuildPermission(g.id(), DashboardPermission.VIEW_GUILD))
                .map(g -> new GuildDto(g.id(), g.name(), g.iconUrl()))
                .toList();

        return ResponseEntity.ok(guilds);
    }
}