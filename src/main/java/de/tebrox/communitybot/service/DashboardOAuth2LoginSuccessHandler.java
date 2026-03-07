package de.tebrox.communitybot.service;

import de.tebrox.communitybot.config.DashboardProperties;
import de.tebrox.communitybot.persistence.entity.DashboardUserEntity;
import de.tebrox.communitybot.repository.DashboardUserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class DashboardOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final DashboardUserRepository dashboardUserRepository;
    private final DashboardProperties dashboardProperties;

    public DashboardOAuth2LoginSuccessHandler(
            DashboardUserRepository dashboardUserRepository,
            DashboardProperties dashboardProperties
    ) {
        this.dashboardUserRepository = dashboardUserRepository;
        this.dashboardProperties = dashboardProperties;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof OAuth2User user)) {
            denyLogin(request, response, "invalid-oauth-user");
            return;
        }

        String discordId = stringAttr(user, "id");
        String username = stringAttr(user, "username");
        String avatar = stringAttr(user, "avatar");

        if (discordId == null || discordId.isBlank()) {
            denyLogin(request, response, "missing-discord-id");
            return;
        }

        String superadminId = dashboardProperties.superadminDiscordId();
        boolean isSuperadmin = superadminId != null
                && !superadminId.isBlank()
                && superadminId.equals(discordId);

        DashboardUserEntity entity = dashboardUserRepository.findByDiscordId(discordId).orElse(null);

        if (entity == null) {
            if (!isSuperadmin) {
                denyLogin(request, response, "not-allowlisted");
                return;
            }

            entity = new DashboardUserEntity();
            entity.setDiscordId(discordId);
            entity.setEnabled(true);
        }

        if (!isSuperadmin && !entity.isEnabled()) {
            denyLogin(request, response, "disabled");
            return;
        }

        entity.setUsername(username);
        entity.setAvatarUrl(buildAvatarUrl(discordId, avatar));
        entity.setLastLoginAt(Instant.now());

        if (isSuperadmin) {
            entity.setEnabled(true);
        }

        dashboardUserRepository.save(entity);
        response.sendRedirect("/");
    }

    private void denyLogin(HttpServletRequest request, HttpServletResponse response, String reason) throws IOException {
        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect("/login?error=" + reason);
    }

    private static String stringAttr(OAuth2User user, String key) {
        Object value = user.getAttributes().get(key);
        return value != null ? String.valueOf(value) : null;
    }

    private static String buildAvatarUrl(String discordId, String avatarHash) {
        if (discordId == null || discordId.isBlank() || avatarHash == null || avatarHash.isBlank()) {
            return null;
        }
        return "https://cdn.discordapp.com/avatars/" + discordId + "/" + avatarHash + ".png";
    }
}