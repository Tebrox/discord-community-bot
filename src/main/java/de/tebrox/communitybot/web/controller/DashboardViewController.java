package de.tebrox.communitybot.web.controller;

import de.tebrox.communitybot.config.GuildConfig;
import de.tebrox.communitybot.config.GuildConfigManager;
import de.tebrox.communitybot.util.SnowflakeValidator;
import de.tebrox.communitybot.web.discord.DashboardDiscordService;
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

    private final DashboardDiscordService discord;
    private final GuildConfigManager configManager;

    @GetMapping({"/", "/dashboard"})
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/dashboard/{guildId}")
    public String guildOverview(@PathVariable String guildId, Model model) {
        SnowflakeValidator.validate(guildId, "guildId");

        var guildOpt = discord.getGuild(guildId);
        GuildConfig cfg = configManager.getConfig(guildId);
        if (guildOpt.isEmpty() || cfg == null) return "redirect:/";

        var guild = guildOpt.get();
        model.addAttribute("guildId", guildId);
        model.addAttribute("guildName", guild.name());
        model.addAttribute("memberCount", guild.memberCount());
        model.addAttribute("cfg", cfg);
        model.addAttribute("buttonCount", cfg.getButtons() != null ? cfg.getButtons().size() : 0);
        model.addAttribute("welcomeEnabled", cfg.getWelcome() != null && cfg.getWelcome().isEnabled());
        return "guild";
    }
}