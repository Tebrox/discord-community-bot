package de.tebrox.communitybot.dashboard.controller;

import de.tebrox.communitybot.core.security.DashboardPermission;
import de.tebrox.communitybot.core.access.DashboardAccessService;
import de.tebrox.communitybot.dashboard.service.DashboardDiscordService;
import de.tebrox.communitybot.dashboard.dto.GuildDto;
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