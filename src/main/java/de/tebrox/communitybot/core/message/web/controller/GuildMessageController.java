package de.tebrox.communitybot.core.message.web.controller;

import de.tebrox.communitybot.core.access.DashboardAccessService;
import de.tebrox.communitybot.core.message.MessageKey;
import de.tebrox.communitybot.core.message.service.GuildMessageAdminService;
import de.tebrox.communitybot.core.message.web.dto.GuildMessageConfigRequest;
import de.tebrox.communitybot.core.security.DashboardPermission;
import de.tebrox.communitybot.core.util.SnowflakeValidator;
import de.tebrox.communitybot.dashboard.service.DashboardDiscordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class GuildMessageController {

    private final DashboardAccessService accessService;
    private final DashboardDiscordService dashboardDiscordService;
    private final GuildMessageAdminService adminService;

    @GetMapping("/guild/{guildId}/messages")
    public String messagesPage(@PathVariable String guildId, Model model) {
        SnowflakeValidator.validate(guildId, "guildId");

        if (!accessService.hasGuildPermission(guildId, DashboardPermission.VIEW_GUILD)) {
            return "redirect:/?forbidden";
        }

        boolean allowed = accessService.hasGuildPermission(guildId, DashboardPermission.ADMIN_GUILD)
                || accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_MESSAGES);

        if (!allowed) {
            return "redirect:/guild/" + guildId + "?forbidden=messages";
        }

        var guildOpt = dashboardDiscordService.getGuild(guildId);
        if (guildOpt.isEmpty()) {
            return "redirect:/";
        }

        var guild = guildOpt.get();

        boolean canManageRoles = accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_ROLES);
        boolean canManageWelcome = accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_WELCOME);
        boolean canManageTickets = accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_TICKETS);
        boolean canViewLogs = accessService.hasGuildPermission(guildId, DashboardPermission.VIEW_LOGS);
        boolean canAdminGuild = accessService.hasGuildPermission(guildId, DashboardPermission.ADMIN_GUILD);
        boolean canManageMessages = accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_MESSAGES);

        model.addAttribute("guildId", guildId);
        model.addAttribute("guildName", guild.name());
        model.addAttribute("messageEntries", adminService.getEditorViews(guildId));

        model.addAttribute("canManageRoles", canManageRoles);
        model.addAttribute("canManageWelcome", canManageWelcome);
        model.addAttribute("canManageTickets", canManageTickets);
        model.addAttribute("canViewLogs", canViewLogs);
        model.addAttribute("canAdminGuild", canAdminGuild);
        model.addAttribute("canManageMessages", canManageMessages);
        model.addAttribute("activeTab", "messages");

        return "messages";
    }

    @PostMapping("/guild/{guildId}/messages/{key}/save")
    public String saveMessage(
            @PathVariable String guildId,
            @PathVariable MessageKey key,
            @ModelAttribute GuildMessageConfigRequest request,
            RedirectAttributes ra
    ) {
        SnowflakeValidator.validate(guildId, "guildId");

        boolean allowed = accessService.hasGuildPermission(guildId, DashboardPermission.ADMIN_GUILD)
                || accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_MESSAGES);

        if (!allowed) {
            return "redirect:/?forbidden";
        }

        adminService.save(guildId, key, request);
        ra.addFlashAttribute("success", "Nachricht gespeichert: " + key.label());
        ra.addFlashAttribute("selectedMessageKey", key.name());
        return "redirect:/guild/" + guildId + "/messages";
    }

    @PostMapping("/guild/{guildId}/messages/{key}/reset")
    public String resetMessage(
            @PathVariable String guildId,
            @PathVariable MessageKey key,
            RedirectAttributes ra
    ) {
        SnowflakeValidator.validate(guildId, "guildId");

        boolean allowed = accessService.hasGuildPermission(guildId, DashboardPermission.ADMIN_GUILD)
                || accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_MESSAGES);

        if (!allowed) {
            return "redirect:/?forbidden";
        }

        adminService.resetToDefault(guildId, key);
        ra.addFlashAttribute("success", "Nachricht auf Default zurückgesetzt: " + key.label());
        ra.addFlashAttribute("selectedMessageKey", key.name());
        return "redirect:/guild/" + guildId + "/messages";
    }
}
