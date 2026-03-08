package de.tebrox.communitybot.dashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            Model model) {
        model.addAttribute("loginError", error);
        return "login";
    }
}