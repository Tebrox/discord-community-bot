package de.tebrox.communitybot.dashboard.service;

import de.tebrox.communitybot.core.access.DashboardAccessService;
import de.tebrox.communitybot.core.security.DashboardPermission;
import de.tebrox.communitybot.dashboard.dto.GuildSubNavItem;
import de.tebrox.communitybot.ticket.web.controller.TicketDashboardController;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GuildSubNavService {

    private final DashboardAccessService accessService;
    private final ObjectProvider<TicketDashboardController> ticketDashboardControllerProvider;

    public GuildSubNavService(
            DashboardAccessService accessService,
            ObjectProvider<TicketDashboardController> ticketDashboardControllerProvider
    ) {
        this.accessService = accessService;
        this.ticketDashboardControllerProvider = ticketDashboardControllerProvider;
    }

    public List<GuildSubNavItem> buildItems(String guildId) {
        List<GuildSubNavItem> items = new ArrayList<>();

        if (guildId == null || guildId.isBlank()) {
            return items;
        }

        if (accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_ROLES)) {
            items.add(new GuildSubNavItem(
                    "roles",
                    "Rollen",
                    "/guild/" + guildId + "/roles"
            ));
        }

        if (accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_WELCOME)) {
            items.add(new GuildSubNavItem(
                    "welcome",
                    "Welcome",
                    "/guild/" + guildId + "/welcome"
            ));
        }

        boolean ticketPageAvailable = ticketDashboardControllerProvider.getIfAvailable() != null;
        if (ticketPageAvailable && accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_TICKETS)) {
            items.add(new GuildSubNavItem(
                    "tickets",
                    "Tickets",
                    "/guild/" + guildId + "/tickets"
            ));
        }

        // Für spätere Guild-spezifische Logs vorbereitbar.
        // Aktuell existiert in deinem Projekt nur /logs global, keine /guild/{guildId}/logs Route.
        // Deshalb hier bewusst noch deaktiviert.
        //
        // if (accessService.hasGuildPermission(guildId, DashboardPermission.VIEW_LOGS)) {
        //     items.add(new GuildSubnavItem(
        //             "logs",
        //             "Logs",
        //             "/guild/" + guildId + "/logs"
        //     ));
        // }


         if (accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_MESSAGES)) {
             items.add(new GuildSubNavItem(
                     "messages",
                     "Nachrichten",
                     "/guild/" + guildId + "/messages"
             ));
        }

        return items;
    }

    public boolean isVisible(String guildId) {
        return !buildItems(guildId).isEmpty();
    }

    public String resolveActiveItem(String requestUri, String guildId) {
        if (guildId == null || guildId.isBlank() || requestUri == null || requestUri.isBlank()) {
            return null;
        }

        String prefix = "/guild/" + guildId;

        if (requestUri.equals(prefix) || requestUri.equals(prefix + "/")) {
            return "overview";
        }
        if (requestUri.startsWith(prefix + "/roles")) {
            return "roles";
        }
        if (requestUri.startsWith(prefix + "/welcome")) {
            return "welcome";
        }
        if (requestUri.startsWith(prefix + "/tickets")) {
            return "tickets";
        }
        if (requestUri.startsWith(prefix + "/messages")) {
            return "messages";
        }
        if (requestUri.startsWith(prefix + "/logs")) {
            return "logs";
        }

        return null;
    }
}
