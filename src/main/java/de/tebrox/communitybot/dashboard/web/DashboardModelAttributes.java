package de.tebrox.communitybot.dashboard.web;

import de.tebrox.communitybot.core.security.DashboardSecurityService;
import de.tebrox.communitybot.dashboard.dto.GuildSubNavItem;
import de.tebrox.communitybot.dashboard.service.GuildSubNavService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ControllerAdvice
public class DashboardModelAttributes {

    private static final Pattern GUILD_PATH_PATTERN = Pattern.compile("^/guild/([0-9]{17,20})(/.*)?$");

    private final DashboardSecurityService dashboardSecurityService;
    private final GuildSubNavService guildSubNavService;

    public DashboardModelAttributes(DashboardSecurityService dashboardSecurityService, GuildSubNavService guildSubNavService) {
        this.dashboardSecurityService = dashboardSecurityService;
        this.guildSubNavService = guildSubNavService;
    }

    @ModelAttribute("currentDiscordId")
    public String currentDiscordId() {
        return dashboardSecurityService.getCurrentDiscordId();
    }

    @ModelAttribute("currentUsername")
    public String currentUsername() {
        return dashboardSecurityService.getCurrentUsername();
    }

    @ModelAttribute("isSuperadmin")
    public boolean isSuperadmin() {
        return dashboardSecurityService.isSuperadmin();
    }

    @ModelAttribute("guildSubNavItems")
    public List<GuildSubNavItem> guildSubNavItems(HttpServletRequest request) {
        String guildId = extractGuildId(request);
        if(guildId == null) {
            return List.of();
        }
        return guildSubNavService.buildItems(guildId);
    }

    @ModelAttribute("guildSubNavVisible")
    public boolean guildSubNavVisible(HttpServletRequest request) {
        String guildId = extractGuildId(request);
        if(guildId == null) {
            return false;
        }
        return guildSubNavService.isVisible(guildId);
    }

    @ModelAttribute("guildSubnavActive")
    public String guildSubNavActive(HttpServletRequest request) {
        String guildId = extractGuildId(request);
        if (guildId == null) {
            return null;
        }
        return guildSubNavService.resolveActiveItem(request.getRequestURI(), guildId);
    }

    private String extractGuildId(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if(uri == null || uri.isBlank()) {
            return null;
        }

        Matcher matcher = GUILD_PATH_PATTERN.matcher(uri);
        if(!matcher.matches()) {
            return null;
        }
        return matcher.group(1);
    }
}