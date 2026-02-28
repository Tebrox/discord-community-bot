package de.tebrox.rolesbot.web.controller;

import de.tebrox.rolesbot.config.GuildConfig;
import de.tebrox.rolesbot.config.GuildConfigLoader;
import de.tebrox.rolesbot.config.GuildConfigManager;
import de.tebrox.rolesbot.discord.listeners.PanelAdminListener;
import de.tebrox.rolesbot.persistence.entity.WelcomedUser;
import de.tebrox.rolesbot.service.WelcomeTrackingService;
import de.tebrox.rolesbot.util.SnowflakeValidator;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.utils.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final GuildConfigManager configManager;
    private final WelcomeTrackingService welcomeTrackingService;
    private final PanelAdminListener panelAdminListener;
    private final JDA jda;

    // ------------------------------------------------------------------ Dashboard

    @GetMapping("/")
    public String dashboard(Model model) {
        List<Guild> guilds = jda.getGuilds();
        List<Map<String, Object>> guildData = new ArrayList<>();
        for (Guild guild : guilds) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", guild.getId());
            data.put("name", guild.getName());
            GuildConfig cfg = configManager.getConfig(guild.getId());
            data.put("buttonCount", cfg != null ? cfg.getButtons().size() : 0);
            data.put("welcomeEnabled", cfg != null && cfg.getWelcome().isEnabled());
            guildData.add(data);
        }
        model.addAttribute("guilds", guildData);
        model.addAttribute("totalGuilds", guilds.size());
        return "dashboard";
    }

    // ------------------------------------------------------------------ Rollen / Buttons

    @GetMapping("/guild/{guildId}/roles")
    public String rolesPage(@PathVariable String guildId, Model model) {
        SnowflakeValidator.validate(guildId, "guildId");
        GuildConfig cfg = configManager.getConfig(guildId);
        Guild guild = jda.getGuildById(guildId);
        if (cfg == null || guild == null) return "redirect:/";

        // Ensure full member list is loaded before accessing getMembersWithRoles()
        // Timeout: 10 seconds. On failure a warning is shown in the UI.
        boolean membersLoaded = ensureMembersLoaded(guild);

        List<Map<String, Object>> roleGroups = new ArrayList<>();
        for (GuildConfig.ButtonConfig btn : cfg.getButtons()) {
            Map<String, Object> group = new LinkedHashMap<>();
            group.put("id", btn.getId());
            group.put("label", btn.getLabel());
            group.put("roleId", btn.getRoleId());
            group.put("style", btn.getStyle());

            Role role = guild.getRoleById(btn.getRoleId());
            if (role != null) {
                group.put("roleName", role.getName());
                group.put("roleFound", true);
                List<Map<String, String>> members = new ArrayList<>();
                for (Member member : guild.getMembersWithRoles(role)) {
                    Map<String, String> m = new LinkedHashMap<>();
                    m.put("username", member.getUser().getName());
                    m.put("userId", member.getId());
                    members.add(m);
                }
                group.put("members", members);
            } else {
                group.put("roleName", "Nicht gefunden");
                group.put("roleFound", false);
                group.put("members", List.of());
            }
            roleGroups.add(group);
        }

        model.addAttribute("guildId", guildId);
        model.addAttribute("guildName", guild.getName());
        model.addAttribute("cfg", cfg);
        model.addAttribute("roleGroups", roleGroups);
        model.addAttribute("statusButton", cfg.getStatusButton());
        if (!membersLoaded) {
            model.addAttribute("memberWarning",
                "⚠️ Mitgliederliste konnte nicht vollständig geladen werden (Timeout). " +
                "Die Anzeige ist möglicherweise unvollständig.");
        }
        return "roles";
    }

    @PostMapping("/guild/{guildId}/roles/save-button")
    public String saveButton(@PathVariable String guildId,
                             @RequestParam String id,
                             @RequestParam String label,
                             @RequestParam String roleId,
                             @RequestParam(defaultValue = "PRIMARY") String style,
                             RedirectAttributes ra) {
        SnowflakeValidator.validate(guildId, "guildId");
        if (id.isBlank() || label.isBlank() || roleId.isBlank()) {
            ra.addFlashAttribute("error", "Alle Felder sind Pflichtfelder.");
            return "redirect:/guild/" + guildId + "/roles";
        }
        if (id.length() > 100) {
            ra.addFlashAttribute("error", "Button-ID darf max. 100 Zeichen lang sein.");
            return "redirect:/guild/" + guildId + "/roles";
        }

        try {
            GuildConfig cfg = deepCopy(guildId);
            if (cfg == null) return "redirect:/";

            List<GuildConfig.ButtonConfig> buttons = cfg.getButtons();
            GuildConfig.ButtonConfig found = buttons.stream()
                    .filter(b -> b.getId().equals(id)).findFirst().orElse(null);
            if (found == null) {
                found = new GuildConfig.ButtonConfig();
                found.setId(id);
                buttons.add(found);
            }
            found.setLabel(label);
            found.setRoleId(roleId);
            found.setStyle(style);
            cfg.buttonById(); // validate no duplicates

            configManager.saveGuildConfig(guildId, cfg);
            refreshPanel(guildId);
            ra.addFlashAttribute("success", "Button gespeichert.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", "Validierungsfehler: " + e.getMessage());
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Speichern fehlgeschlagen: " + e.getMessage());
        }
        return "redirect:/guild/" + guildId + "/roles";
    }

    @PostMapping("/guild/{guildId}/roles/delete-button")
    public String deleteButton(@PathVariable String guildId,
                               @RequestParam String id,
                               RedirectAttributes ra) {
        SnowflakeValidator.validate(guildId, "guildId");
        try {
            GuildConfig cfg = deepCopy(guildId);
            if (cfg == null) return "redirect:/";
            cfg.getButtons().removeIf(b -> b.getId().equals(id));
            for (List<String> row : cfg.getLayout().getRows()) row.remove(id);
            cfg.getLayout().getRows().removeIf(List::isEmpty);
            configManager.saveGuildConfig(guildId, cfg);
            refreshPanel(guildId);
            ra.addFlashAttribute("success", "Button gelöscht.");
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Löschen fehlgeschlagen: " + e.getMessage());
        }
        return "redirect:/guild/" + guildId + "/roles";
    }

    @PostMapping("/guild/{guildId}/roles/save-title")
    public String savePanelTitle(@PathVariable String guildId,
                                 @RequestParam String title,
                                 RedirectAttributes ra) {
        SnowflakeValidator.validate(guildId, "guildId");
        try {
            GuildConfig cfg = deepCopy(guildId);
            if (cfg == null) return "redirect:/";
            cfg.getPanel().setTitle(title);
            configManager.saveGuildConfig(guildId, cfg);
            refreshPanel(guildId);
            ra.addFlashAttribute("success", "Panel-Titel gespeichert.");
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Speichern fehlgeschlagen: " + e.getMessage());
        }
        return "redirect:/guild/" + guildId + "/roles";
    }

    // ------------------------------------------------------------------ Welcome

    @GetMapping("/guild/{guildId}/welcome")
    public String welcomePage(@PathVariable String guildId, Model model) {
        SnowflakeValidator.validate(guildId, "guildId");
        GuildConfig cfg = configManager.getConfig(guildId);
        Guild guild = jda.getGuildById(guildId);
        if (cfg == null || guild == null) return "redirect:/";

        List<WelcomedUser> welcomedUsers = welcomeTrackingService.getWelcomedUsers(guildId);

        model.addAttribute("guildId", guildId);
        model.addAttribute("guildName", guild.getName());
        model.addAttribute("wc", cfg.getWelcome());
        model.addAttribute("welcomedUsers", welcomedUsers);
        return "welcome";
    }

    @PostMapping("/guild/{guildId}/welcome/save")
    public String saveWelcome(@PathVariable String guildId,
                              @RequestParam(required = false) String enabled,
                              @RequestParam(required = false) String channelId,
                              @RequestParam(defaultValue = "0") int deleteAfterSeconds,
                              @RequestParam(required = false) String embedEnabled,
                              @RequestParam(required = false) String embedTitle,
                              @RequestParam(required = false) String embedDescription,
                              @RequestParam(required = false, defaultValue = "#5865F2") String embedColor,
                              RedirectAttributes ra) {
        SnowflakeValidator.validate(guildId, "guildId");
        try {
            GuildConfig cfg = deepCopy(guildId);
            if (cfg == null) return "redirect:/";

            cfg.getWelcome().setEnabled("on".equals(enabled) || "true".equals(enabled));
            cfg.getWelcome().setChannelId(channelId != null ? channelId.trim() : "");
            cfg.getWelcome().setDeleteAfterSeconds(Math.max(0, deleteAfterSeconds));
            cfg.getWelcome().getEmbed().setEnabled("on".equals(embedEnabled) || "true".equals(embedEnabled));
            if (embedTitle != null) cfg.getWelcome().getEmbed().setTitle(embedTitle.trim());
            if (embedDescription != null) cfg.getWelcome().getEmbed().setDescription(embedDescription.trim());
            cfg.getWelcome().getEmbed().setColor(embedColor);

            configManager.saveGuildConfig(guildId, cfg);
            ra.addFlashAttribute("success", "Welcome-Einstellungen gespeichert.");
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Speichern fehlgeschlagen: " + e.getMessage());
        }
        return "redirect:/guild/" + guildId + "/welcome";
    }

    @PostMapping("/guild/{guildId}/welcome/delete-user")
    public String deleteWelcomedUser(@PathVariable String guildId,
                                     @RequestParam String userId,
                                     RedirectAttributes ra) {
        SnowflakeValidator.validate(guildId, "guildId");
        SnowflakeValidator.validate(userId, "userId");
        welcomeTrackingService.deleteWelcomedUser(guildId, userId);
        ra.addFlashAttribute("success", "User-Eintrag gelöscht.");
        return "redirect:/guild/" + guildId + "/welcome";
    }

    // ------------------------------------------------------------------ Internals

    /**
     * Ensures the full member list for the guild is loaded from Discord.
     * Uses guild.loadMembers().submit().get(10, SECONDS) as per requirements.
     * This must be called before guild.getMembersWithRoles() to guarantee complete data.
     *
     * @return true if loaded successfully, false on timeout/error
     */
    private boolean ensureMembersLoaded(Guild guild) {
        Task<List<Member>> task = guild.loadMembers();
        CompletableFuture<List<Member>> future = new CompletableFuture<>();

        // Task -> Future bridgen
        task.onSuccess(future::complete);
        task.onError(future::completeExceptionally);

        try {
            future.get(10, TimeUnit.SECONDS);
            return true;
        } catch (TimeoutException e) {
            // Task stoppen, damit nichts "hängen bleibt"
            try { task.cancel(); } catch (Exception ignored) {}
            log.warn("[Dashboard] Member loading timed out for guild {} ({})", guild.getName(), guild.getId());
            return false;
        } catch (Exception e) {
            log.warn("[Dashboard] Failed to load members for guild {} ({}): {}",
                    guild.getName(), guild.getId(), e.getMessage());
            return false;
        }
    }

    private GuildConfig deepCopy(String guildId) {
        GuildConfig original = configManager.getConfig(guildId);
        if (original == null) return null;
        Map<String, Object> raw = GuildConfigLoader.guildToMap(original);
        return GuildConfigLoader.parseGuild(raw);
    }

    private void refreshPanel(String guildId) {
        Guild guild = jda.getGuildById(guildId);
        GuildConfig cfg = configManager.getConfig(guildId);
        if (guild != null && cfg != null) {
            panelAdminListener.refreshPanel(guild, cfg);
        }
    }
}
