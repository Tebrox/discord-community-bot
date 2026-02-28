package de.tebrox.rolesbot.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class SessionAuthFilter extends OncePerRequestFilter {

    public static final String SESSION_AUTH_KEY = "authenticated";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!isAlreadyAuthenticated()) {
            HttpSession session = request.getSession(false);
            if (session != null && Boolean.TRUE.equals(session.getAttribute(SESSION_AUTH_KEY))) {
                injectAuthentication(session);
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isAlreadyAuthenticated() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated();
    }

    private void injectAuthentication(HttpSession session) {
        var token = new UsernamePasswordAuthenticationToken(
                "dashboard-admin",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_DASHBOARD"))
        );
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(token);
        SecurityContextHolder.setContext(ctx);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, ctx);
    }
}
