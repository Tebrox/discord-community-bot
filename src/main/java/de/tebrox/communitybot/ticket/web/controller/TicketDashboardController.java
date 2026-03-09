package de.tebrox.communitybot.ticket.web.controller;

import de.tebrox.communitybot.core.access.DashboardAccessService;
import de.tebrox.communitybot.core.security.DashboardPermission;
import de.tebrox.communitybot.core.util.SnowflakeValidator;
import de.tebrox.communitybot.dashboard.runtime.TicketDashboardRuntimeGateway;
import de.tebrox.communitybot.dashboard.service.DashboardDiscordService;
import de.tebrox.communitybot.ticket.persistence.entity.Ticket;
import de.tebrox.communitybot.ticket.persistence.entity.TicketGuildConfig;
import de.tebrox.communitybot.ticket.service.TicketGuildConfigService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class TicketDashboardController {

    private final DashboardAccessService accessService;
    private final DashboardDiscordService dashboardDiscordService;
    private final TicketGuildConfigService ticketGuildConfigService;
    private final TicketDashboardRuntimeGateway ticketRuntimeGateway;

    public TicketDashboardController(
            DashboardAccessService accessService,
            DashboardDiscordService dashboardDiscordService,
            TicketGuildConfigService ticketGuildConfigService,
            TicketDashboardRuntimeGateway ticketRuntimeGateway
    ) {
        this.accessService = accessService;
        this.dashboardDiscordService = dashboardDiscordService;
        this.ticketGuildConfigService = ticketGuildConfigService;
        this.ticketRuntimeGateway = ticketRuntimeGateway;
    }

    @GetMapping("/guild/{guildId}/tickets")
    public String ticketSettings(@PathVariable String guildId, Model model) {
        SnowflakeValidator.validate(guildId, "guildId");

        if (!accessService.hasGuildPermission(guildId, DashboardPermission.VIEW_GUILD)) {
            return "redirect:/?forbidden";
        }

        if (!accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_TICKETS)) {
            return "redirect:/guild/" + guildId + "?forbidden=tickets";
        }

        var dashboardGuildOpt = dashboardDiscordService.getGuild(guildId);
        if (dashboardGuildOpt.isEmpty()) {
            return "redirect:/";
        }

        var dashboardGuild = dashboardGuildOpt.get();

        TicketGuildConfig config = ticketGuildConfigService.getOrCreate(guildId);

        boolean canManageRoles = accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_ROLES);
        boolean canManageWelcome = accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_WELCOME);
        boolean canManageTickets = accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_TICKETS);
        boolean canViewLogs = accessService.hasGuildPermission(guildId, DashboardPermission.VIEW_LOGS);
        boolean canAdminGuild = accessService.hasGuildPermission(guildId, DashboardPermission.ADMIN_GUILD);

        model.addAttribute("guildId", guildId);
        model.addAttribute("guildName", dashboardGuild.name());
        model.addAttribute("ticketCfg", config);
        model.addAttribute("ticketBotAvailable", ticketRuntimeGateway.isAvailableForGuild(guildId));

        model.addAttribute("textChannels", dashboardDiscordService.listTextChannels(guildId));
        model.addAttribute("roles", dashboardDiscordService.listRoles(guildId));

        model.addAttribute("canManageRoles", canManageRoles);
        model.addAttribute("canManageWelcome", canManageWelcome);
        model.addAttribute("canManageTickets", canManageTickets);
        model.addAttribute("canViewLogs", canViewLogs);
        model.addAttribute("canAdminGuild", canAdminGuild);

        model.addAttribute("activeTab", "tickets");

        return "ticket-settings";
    }
}