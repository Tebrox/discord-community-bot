package de.tebrox.communitybot.dashboard.controller;

import de.tebrox.communitybot.core.config.DashboardProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final DashboardProperties dashboardProperties;

    public AuthController(DashboardProperties dashboardProperties) {
        this.dashboardProperties = dashboardProperties;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if(dashboardProperties.demo()) {
            return "redirect:/";
        }
        model.addAttribute("loginError", error);
        return "login";
    }
}