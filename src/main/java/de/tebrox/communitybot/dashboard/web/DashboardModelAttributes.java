package de.tebrox.communitybot.dashboard.web;

import de.tebrox.communitybot.core.security.DashboardSecurityService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class DashboardModelAttributes {

    private final DashboardSecurityService dashboardSecurityService;

    public DashboardModelAttributes(DashboardSecurityService dashboardSecurityService) {
        this.dashboardSecurityService = dashboardSecurityService;
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
}