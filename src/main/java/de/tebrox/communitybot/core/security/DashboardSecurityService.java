package de.tebrox.communitybot.core.security;

import de.tebrox.communitybot.core.config.DashboardProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class DashboardSecurityService {

    private final DashboardProperties dashboardProperties;

    public DashboardSecurityService(DashboardProperties dashboardProperties) {
        this.dashboardProperties = dashboardProperties;
    }

    public boolean isDemoMode() {
        return dashboardProperties.demo();
    }

    public String getCurrentDiscordId() {
        if (isDemoMode()) {
            return dashboardProperties.superadminDiscordId() != null
                    && !dashboardProperties.superadminDiscordId().isBlank()
                    ? dashboardProperties.superadminDiscordId()
                    : "dev-superadmin";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken oauth)) {
            return null;
        }

        Object principal = oauth.getPrincipal();
        if (!(principal instanceof OAuth2User user)) {
            return null;
        }

        Object id = user.getAttributes().get("id");
        return id != null ? String.valueOf(id) : null;
    }

    public String getCurrentUsername() {
        if (isDemoMode()) {
            return "Local Dev";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken oauth)) {
            return null;
        }

        Object principal = oauth.getPrincipal();
        if (!(principal instanceof OAuth2User user)) {
            return null;
        }

        Object username = user.getAttributes().get("username");
        return username != null ? String.valueOf(username) : null;
    }

    public boolean isSuperadmin() {
        String current = getCurrentDiscordId();
        String configured = dashboardProperties.superadminDiscordId();
        return current != null && configured != null && !configured.isBlank() && configured.equals(current);
    }
}