package de.tebrox.rolesbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tebrox.rolesbot.web.filter.SessionAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

import java.util.Map;

/**
 * Spring Security configuration hardened to Ticketbot level:
 * - HSTS enabled (31536000s, includeSubDomains)
 * - AccessDeniedHandler for API endpoints → 403 JSON
 * - CSP without 'unsafe-inline' for script-src
 * - session: httpOnly, same-site=strict, migrateSession on login
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SessionAuthFilter sessionAuthFilter() {
        return new SessionAuthFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SessionAuthFilter sessionAuthFilter) throws Exception {
        CookieCsrfTokenRepository csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName("_csrf");

        ObjectMapper objectMapper = new ObjectMapper();

        http
            .addFilterBefore(sessionAuthFilter, SecurityContextHolderFilter.class)
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfRepo)
                .csrfTokenRequestHandler(csrfHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/auth/logout", "/login").permitAll()
                .requestMatchers("/css/**", "/js/**", "/favicon.ico").permitAll()
                .requestMatchers("/api/logs/stream").authenticated()
                .requestMatchers("/api/**").authenticated()
                .requestMatchers("/dashboard/**", "/").authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                // Unauthenticated → 401 JSON (API) or redirect to /login (browser)
                .authenticationEntryPoint((request, response, e) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        objectMapper.writeValue(response.getWriter(), Map.of("error", "Not authenticated"));
                    } else {
                        response.sendRedirect("/login");
                    }
                })
                // Authenticated but forbidden → 403 JSON (API) or redirect (browser)
                .accessDeniedHandler((request, response, e) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        objectMapper.writeValue(response.getWriter(), Map.of("error", "Access denied"));
                    } else {
                        response.sendRedirect("/login");
                    }
                })
            )
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .maximumSessions(5)
            )
            .headers(headers -> headers
                // CSP: no 'unsafe-inline' for script-src (aligned with Ticketbot)
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; " +
                    "script-src 'self'; " +
                    "style-src 'self' 'unsafe-inline'; " +
                    "img-src 'self' data: https://cdn.discordapp.com; " +
                    "connect-src 'self'; " +
                    "frame-ancestors 'none'"
                ))
                .frameOptions(frame -> frame.deny())
                .xssProtection(xss -> xss
                    .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                .referrerPolicy(ref -> ref
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                // HSTS: 1 year, includeSubDomains (was missing before)
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31_536_000))
                .contentTypeOptions(ct -> {})
            );

        return http.build();
    }
}
