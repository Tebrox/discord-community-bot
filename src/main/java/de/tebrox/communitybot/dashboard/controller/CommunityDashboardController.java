package de.tebrox.communitybot.dashboard.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tebrox.communitybot.community.config.CommunityGuildConfig;
import de.tebrox.communitybot.community.service.CommunityGuildConfigService;
import de.tebrox.communitybot.community.persistence.entity.WelcomedUser;
import de.tebrox.communitybot.core.security.DashboardPermission;
import de.tebrox.communitybot.core.access.DashboardAccessService;
import de.tebrox.communitybot.community.service.WelcomeTrackingService;
import de.tebrox.communitybot.core.util.SnowflakeValidator;
import de.tebrox.communitybot.dashboard.service.DashboardDiscordService;
import de.tebrox.communitybot.dashboard.panel.PanelRefresher;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
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
public class CommunityDashboardController {

    private static final Logger log = LoggerFactory.getLogger(CommunityDashboardController.class);

    private final CommunityGuildConfigService configManager;
    private final WelcomeTrackingService welcomeTrackingService;
    private final DashboardDiscordService discord;
    private final PanelRefresher panelRefresher;

    private final ObjectMapper objectMapper;

    private final DashboardAccessService accessService;

    @GetMapping("/guild/{guildId}")
    public String guildOverview(@PathVariable String guildId, Model model) {
        SnowflakeValidator.validate(guildId, "guildId");

        if(!accessService.hasGuildPermission(guildId, DashboardPermission.VIEW_GUILD)) {
            return "redirect:/?forbidden";
        }

        var guildOpt = discord.getGuild(guildId);
        CommunityGuildConfig cfg = configManager.getConfig(guildId);
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

    // ------------------------------------------------------------------ Dashboard

    @GetMapping("/legacy-dashboard")
    public String legacy_dashboard(Model model) {
        var guilds = discord.listGuilds();
        List<Map<String, Object>> guildData = new ArrayList<>();
        for (var guild : guilds) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", guild.id());
            data.put("name", guild.name());
            CommunityGuildConfig cfg = configManager.getConfig(guild.id());
            data.put("buttonCount", cfg != null ? cfg.getButtons().size() : 0);
            data.put("welcomeEnabled", cfg != null && cfg.getWelcome().isEnabled());
            guildData.add(data);
        }
        model.addAttribute("guilds", guildData);
        model.addAttribute("totalGuilds", guilds.size());
        return "legacy-dashboard";
    }

    // ------------------------------------------------------------------ Rollen / Buttons

    @GetMapping("/guild/{guildId}/roles")
    public String rolesPage(@PathVariable String guildId, Model model) throws JsonProcessingException {
        SnowflakeValidator.validate(guildId, "guildId");

        if(!accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_ROLES)) {
            return "redirect:/?forbidden";
        }

        CommunityGuildConfig cfg = configManager.getConfig(guildId);
        var guildOpt = discord.getGuild(guildId);
        if (cfg == null || guildOpt.isEmpty()) return "redirect:/";

        var guild = guildOpt.get();

        try {
            String json = objectMapper.writeValueAsString(cfg.getButtons());
            json = json.replace("</", "<\\/");
            model.addAttribute("buttonsJson", json);
        } catch(Exception e) {
            log.warn("[Dashboard] Failed to serialize buttons to JSON: {}", e.getMessage());
            model.addAttribute("buttonsJson", "[]");
        }

        Map<String, Boolean> roleFoundByButtonId = new HashMap<>();
        List<Map<String, Object>> roleGroups = new ArrayList<>();

        for (CommunityGuildConfig.ButtonConfig btn : cfg.getButtons()) {
            Map<String, Object> group = new LinkedHashMap<>();
            group.put("id", btn.getId());
            group.put("label", btn.getLabel());
            group.put("roleId", btn.getRoleId());
            group.put("style", btn.getStyle());

            var roleOpt = discord.getRole(guildId, btn.getRoleId());
            if (roleOpt.isPresent()) {
                var role = roleOpt.get();
                group.put("roleName", role.name());
                group.put("roleFound", true);

                List<Map<String, String>> members = new ArrayList<>();
                for (var member : discord.getMembersWithRole(guildId, btn.getRoleId())) {
                    Map<String, String> m = new LinkedHashMap<>();
                    m.put("username", member.username());
                    m.put("displayName", member.displayName());
                    m.put("userId", member.id());
                    members.add(m);
                }
                group.put("members", members);
            } else {
                group.put("roleName", "Nicht gefunden");
                group.put("roleFound", false);
                group.put("members", List.of());
            }
            roleFoundByButtonId.put(btn.getId(), roleOpt.isPresent());
            roleGroups.add(group);
        }

        model.addAttribute("guildId", guildId);
        model.addAttribute("guildName", guild.name());
        model.addAttribute("cfg", cfg);
        model.addAttribute("roleGroups", roleGroups);
        model.addAttribute("statusButton", cfg.getStatusButton());
        model.addAttribute("roleFoundByButtonId", roleFoundByButtonId);

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

        if(!accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_ROLES)) {
            return "redirect:/?forbidden";
        }


        if (id.isBlank() || label.isBlank() || roleId.isBlank()) {
            ra.addFlashAttribute("error", "Alle Felder sind Pflichtfelder.");
            return "redirect:/guild/" + guildId + "/roles";
        }
        if (id.length() > 100) {
            ra.addFlashAttribute("error", "Button-ID darf max. 100 Zeichen lang sein.");
            return "redirect:/guild/" + guildId + "/roles";
        }

        try {
            CommunityGuildConfig cfg = deepCopy(guildId);
            if (cfg == null) return "redirect:/";

            List<CommunityGuildConfig.ButtonConfig> buttons = cfg.getButtons();
            CommunityGuildConfig.ButtonConfig found = buttons.stream()
                    .filter(b -> b.getId().equals(id)).findFirst().orElse(null);
            if (found == null) {
                found = new CommunityGuildConfig.ButtonConfig();
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

        if(!accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_ROLES)) {
            return "redirect:/?forbidden";
        }


        try {
            CommunityGuildConfig cfg = deepCopy(guildId);
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

        if(!accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_ROLES)) {
            return "redirect:/?forbidden";
        }


        try {
            CommunityGuildConfig cfg = deepCopy(guildId);
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

    @PostMapping("/guild/{guildId}/roles/save-status-button")
    public String saveStatusButton(@PathVariable String guildId,
                                   @RequestParam String id,
                                   @RequestParam String label,
                                   @RequestParam(defaultValue = "SECONDARY") String style,
                                   RedirectAttributes ra) {
        SnowflakeValidator.validate(guildId, "guildId");

        if(!accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_ROLES)) {
            return "redirect:/?forbidden";
        }


        if (label == null || label.isBlank()) {
            ra.addFlashAttribute("error", "Label darf nicht leer sein.");
            return "redirect:/guild/" + guildId + "/roles";
        }
        try {
            CommunityGuildConfig cfg = deepCopy(guildId);
            if (cfg == null) return "redirect:/";

            cfg.getStatusButton().setId(id);
            cfg.getStatusButton().setLabel(label.trim());
            cfg.getStatusButton().setStyle(style);

            configManager.saveGuildConfig(guildId, cfg);
            refreshPanel(guildId);
            ra.addFlashAttribute("success", "Status-Button gespeichert.");
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Speichern fehlgeschlagen: " + e.getMessage());
        }
        return "redirect:/guild/" + guildId + "/roles";
    }

    @PostMapping("/guild/{guildId}/roles/save-order")
    public String saveButtonOrder(@PathVariable String guildId,
                                  @RequestParam("order") String order,
                                  RedirectAttributes ra) {
        SnowflakeValidator.validate(guildId, "guildId");

        if (!accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_ROLES)) {
            return "redirect:/?forbidden";
        }

        if (order == null || order.isBlank()) {
            ra.addFlashAttribute("error", "Keine Reihenfolge übermittelt.");
            return "redirect:/guild/" + guildId + "/roles";
        }

        try {
            CommunityGuildConfig cfg = deepCopy(guildId);
            if (cfg == null) return "redirect:/";

            List<String> orderedIds = Arrays.stream(order.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();

            List<CommunityGuildConfig.ButtonConfig> existingButtons = cfg.getButtons();
            Map<String, CommunityGuildConfig.ButtonConfig> byId = new LinkedHashMap<>();
            for (CommunityGuildConfig.ButtonConfig button : existingButtons) {
                byId.put(button.getId(), button);
            }

            if (orderedIds.size() != existingButtons.size()) {
                ra.addFlashAttribute("error", "Ungültige Reihenfolge.");
                return "redirect:/guild/" + guildId + "/roles";
            }

            Set<String> uniqueIds = new LinkedHashSet<>(orderedIds);
            if (uniqueIds.size() != orderedIds.size()) {
                ra.addFlashAttribute("error", "Reihenfolge enthält doppelte IDs.");
                return "redirect:/guild/" + guildId + "/roles";
            }

            for (String id : orderedIds) {
                if (!byId.containsKey(id)) {
                    ra.addFlashAttribute("error", "Unbekannte Button-ID in Reihenfolge: " + id);
                    return "redirect:/guild/" + guildId + "/roles";
                }
            }

            List<CommunityGuildConfig.ButtonConfig> reordered = new ArrayList<>();
            for (String id : orderedIds) {
                reordered.add(byId.get(id));
            }

            cfg.setButtons(reordered);

            // Wichtig:
            // Die aktuelle UI bearbeitet die Reihenfolge der Buttons, nicht das manuelle Layout.
            // Daher wird das explizite Layout zurückgesetzt, damit effectiveLayoutRows()
            // künftig wieder die Reihenfolge aus cfg.buttons verwendet.
            if (cfg.getLayout() != null) {
                cfg.getLayout().setRows(new ArrayList<>());
            }

            configManager.saveGuildConfig(guildId, cfg);
            refreshPanel(guildId);
            ra.addFlashAttribute("success", "Button-Reihenfolge gespeichert.");
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Speichern fehlgeschlagen: " + e.getMessage());
        }

        return "redirect:/guild/" + guildId + "/roles";
    }

    // ------------------------------------------------------------------ Welcome

    @GetMapping("/guild/{guildId}/welcome")
    public String welcomePage(@PathVariable String guildId, Model model) {
        SnowflakeValidator.validate(guildId, "guildId");

        if(!accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_WELCOME)) {
            return "redirect:/?forbidden";
        }

        CommunityGuildConfig cfg = configManager.getConfig(guildId);
        var guildOpt = discord.getGuild(guildId);
        if (cfg == null || guildOpt.isEmpty()) return "redirect:/";

        var guild = guildOpt.get();
        List<WelcomedUser> welcomedUsers = welcomeTrackingService.getWelcomedUsers(guildId);

        List<Map<String, String>> channels = discord.listTextChannels(guildId).stream()
                .map(ch -> Map.of("id", ch.id(), "name", ch.name()))
                .toList();

        List<Map<String, String>> roles = discord.listRoles(guildId).stream()
                .map(r -> Map.of("id", r.id(), "name", r.name()))
                .toList();

        String channelsJson;
        try { channelsJson = objectMapper.writeValueAsString(channels); }
        catch (Exception e) { channelsJson = "[]"; }

        String rolesJson;
        try { rolesJson = objectMapper.writeValueAsString(roles); }
        catch (Exception e) { rolesJson = "[]"; }

        model.addAttribute("guildId", guildId);
        model.addAttribute("guildName", guild.name());
        model.addAttribute("wc", cfg.getWelcome());
        model.addAttribute("welcomedUsers", welcomedUsers);

        model.addAttribute("channels", channels);
        model.addAttribute("channelsJson", channelsJson);

        model.addAttribute("roles", roles);
        model.addAttribute("rolesJson", rolesJson);

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
                              @RequestParam(required = false) String embedFooter,
                              @RequestParam(required = false) String embedAvatar,
                              RedirectAttributes ra) {
        SnowflakeValidator.validate(guildId, "guildId");

        if(!accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_WELCOME)) {
            return "redirect:/?forbidden";
        }

        try {
            CommunityGuildConfig cfg = deepCopy(guildId);
            if (cfg == null) return "redirect:/";

            cfg.getWelcome().setEnabled("on".equals(enabled) || "true".equals(enabled));
            cfg.getWelcome().setChannelId(channelId != null ? channelId.trim() : "");
            cfg.getWelcome().setDeleteAfterSeconds(Math.max(0, deleteAfterSeconds));
            cfg.getWelcome().getEmbed().setEnabled("on".equals(embedEnabled) || "true".equals(embedEnabled));
            if (embedTitle != null) cfg.getWelcome().getEmbed().setTitle(embedTitle.trim());
            if (embedDescription != null) cfg.getWelcome().getEmbed().setDescription(embedDescription.trim());
            if (embedFooter != null) cfg.getWelcome().getEmbed().setFooter(embedFooter.trim());
            if (embedAvatar != null) cfg.getWelcome().getEmbed().setThumbnail(embedAvatar.trim());
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

        if(!accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_WELCOME)) {
            return "redirect:/?forbidden";
        }

        welcomeTrackingService.deleteWelcomedUser(guildId, userId);
        ra.addFlashAttribute("success", "User-Eintrag gelöscht.");
        return "redirect:/guild/" + guildId + "/welcome";
    }

    @GetMapping("/guild/{guildId}/roles/refresh")
    public String refreshRolesPanel(@PathVariable String guildId, RedirectAttributes ra) {
        SnowflakeValidator.validate(guildId, "guildId");

        if(!accessService.hasGuildPermission(guildId, DashboardPermission.MANAGE_ROLES)) {
            return "redirect:/?forbidden";
        }

        try {
            refreshPanel(guildId);
            ra.addFlashAttribute("success", "Panel wurde aktualisiert.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Aktualisieren fehlgeschlagen: " + e.getMessage());
        }

        return "redirect:/guild/" + guildId;
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

    private CommunityGuildConfig deepCopy(String guildId) {
        CommunityGuildConfig original = configManager.getConfig(guildId);
        if (original == null) return null;

        try {
            String json = objectMapper.writeValueAsString(original);
            return objectMapper.readValue(json, CommunityGuildConfig.class);
        }catch(Exception e) {
            log.warn("[Dashboard] Failed to deep-copy GuildConfig for {}: {}", guildId, e.getMessage());
            return original;
        }
    }

    private void refreshPanel(String guildId) {
        CommunityGuildConfig cfg = configManager.getConfig(guildId);
        if (cfg != null) {
            panelRefresher.refresh(guildId, cfg);
        }
    }
}
