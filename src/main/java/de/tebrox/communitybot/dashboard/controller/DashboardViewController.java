package de.tebrox.communitybot.dashboard.controller;

import de.tebrox.communitybot.community.config.CommunityGuildConfig;
import de.tebrox.communitybot.community.service.CommunityGuildConfigService;
import de.tebrox.communitybot.core.security.DashboardPermission;
import de.tebrox.communitybot.core.access.DashboardAccessService;
import de.tebrox.communitybot.core.util.SnowflakeValidator;
import de.tebrox.communitybot.dashboard.service.DashboardDiscordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class DashboardViewController {

    private final DashboardDiscordService discord;
    private final CommunityGuildConfigService configManager;
    private final DashboardAccessService accessService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard() {
        if (!accessService.canAccessDashboard()) {
            return "redirect:/login?error=not-allowed";
        }
        return "dashboard";
    }

    @GetMapping("/guild/{guildId}")
    public String guildOverview(@PathVariable String guildId, Model model) {
        SnowflakeValidator.validate(guildId, "guildId");

        if(!accessService.hasGuildPermission(guildId, DashboardPermission.VIEW_GUILD)) {
            return "redirect:/?forbidden";
        }

        var guildOpt = discord.getGuild(guildId);
        CommunityGuildConfig cfg = configManager.getConfig(guildId);
        if (guildOpt.isEmpty() || cfg == null) return "redirect:/";

        var guild = guildOpt.get();

        boolean canManageRoles = accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_ROLES);
        boolean canManageWelcome = accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_WELCOME);
        boolean canManageTickets = accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_TICKETS);
        boolean canViewLogs = accessService.hasGuildPermission(guildId, DashboardPermission.VIEW_LOGS);
        boolean canAdminGuild = accessService.hasGuildPermission(guildId, DashboardPermission.ADMIN_GUILD);
        boolean canManageMessages = accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_MESSAGES);

        model.addAttribute("canManageRoles", canManageRoles);
        model.addAttribute("canManageWelcome", canManageWelcome);
        model.addAttribute("canManageTickets", canManageTickets);
        model.addAttribute("canViewLogs", canViewLogs);
        model.addAttribute("canAdminGuild", canAdminGuild);
        model.addAttribute("canManageMessages", canManageMessages);

        model.addAttribute("guildId", guildId);
        model.addAttribute("guildName", guild.name());
        model.addAttribute("memberCount", guild.memberCount());
        model.addAttribute("cfg", cfg);
        model.addAttribute("buttonCount", cfg.getButtons() != null ? cfg.getButtons().size() : 0);
        model.addAttribute("welcomeEnabled", cfg.getWelcome() != null && cfg.getWelcome().isEnabled());
        return "guild";
    }
}