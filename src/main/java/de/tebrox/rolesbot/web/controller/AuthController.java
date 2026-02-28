package de.tebrox.rolesbot.web.controller;

import de.tebrox.rolesbot.service.AuthService;
import de.tebrox.rolesbot.service.AuthService.AuthResult;
import de.tebrox.rolesbot.web.filter.SessionAuthFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/auth/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        String ip = getClientIp(request);
        String password = body.getOrDefault("password", "");

        // Single call — service handles lockout check + attempt recording + reset
        AuthResult result = authService.attemptLogin(password, ip);

        return switch (result) {
            case SUCCESS -> {
                HttpSession session = request.getSession(true);
                session.setAttribute(SessionAuthFilter.SESSION_AUTH_KEY, true);
                yield ResponseEntity.ok(Map.of("success", true, "redirect", "/"));
            }
            case LOCKED_OUT, NOW_LOCKED_OUT -> ResponseEntity.status(429).body(Map.of(
                    "success", false,
                    "message", "Zu viele Fehlversuche. Bitte warte einige Minuten."
            ));
            case INVALID_CREDENTIALS -> ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Ungültiges Passwort."
            ));
        };
    }

    @PostMapping("/auth/logout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return ResponseEntity.ok(Map.of("success", true, "redirect", "/login"));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
