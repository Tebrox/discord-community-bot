package de.tebrox.communitybot.web.controller;

import de.tebrox.communitybot.config.GuildConfig;
import de.tebrox.communitybot.config.GuildConfigManager;
import de.tebrox.communitybot.util.SnowflakeValidator;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class DashboardViewController {

    private final JDA jda;
    private final GuildConfigManager configManager;

    @GetMapping({"/", "/dashboard"})
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/dashboard/{guildId}")
    public String guildOverview(@PathVariable String guildId, Model model) {
        SnowflakeValidator.validate(guildId, "guildId");
        Guild guild = jda.getGuildById(guildId);
        GuildConfig cfg = configManager.getConfig(guildId);
        if (guild == null || cfg == null) return "redirect:/";

        model.addAttribute("guildId", guildId);
        model.addAttribute("guildName", guild.getName());
        model.addAttribute("memberCount", guild.getMemberCount());
        model.addAttribute("cfg", cfg);
        model.addAttribute("buttonCount", cfg.getButtons() != null ? cfg.getButtons().size() : 0);
        model.addAttribute("welcomeEnabled", cfg.getWelcome() != null && cfg.getWelcome().isEnabled());
        return "guild";
    }
}