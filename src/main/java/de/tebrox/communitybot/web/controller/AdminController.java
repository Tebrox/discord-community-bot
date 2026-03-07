package de.tebrox.communitybot.web.controller;

import de.tebrox.communitybot.persistence.entity.DashboardUserEntity;
import de.tebrox.communitybot.security.DashboardPermission;
import de.tebrox.communitybot.security.PermissionBits;
import de.tebrox.communitybot.service.DashboardAccessService;
import de.tebrox.communitybot.util.SnowflakeValidator;
import de.tebrox.communitybot.web.discord.DashboardDiscordService;
import de.tebrox.communitybot.web.dto.GuildDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final DashboardAccessService accessService;
    private final DashboardDiscordService discordService;

    public AdminController(DashboardAccessService accessService,
                           DashboardDiscordService discordService) {
        this.accessService = accessService;
        this.discordService = discordService;
    }

    @ModelAttribute("isSuperadmin")
    public boolean isSuperadmin() {
        return accessService.isSuperadmin();
    }

    @GetMapping("/users")
    public String users(Model model, RedirectAttributes ra) {
        if (!requireSuperadmin(ra)) {
            return "redirect:/";
        }

        model.addAttribute("users", accessService.listUsers());
        return "admin-users";
    }

    @PostMapping("/users/add")
    public String addUser(@RequestParam String discordId,
                          HttpSession session,
                          RedirectAttributes ra) {
        if (!requireSuperadmin(ra)) {
            return "redirect:/";
        }

        try {
            SnowflakeValidator.validate(discordId, "discordId");
            accessService.createIfMissing(discordId.trim());
            ra.addFlashAttribute("success", "User zur Allowlist hinzugefügt.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Konnte User nicht hinzufügen: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/enable")
    public String enableUser(@PathVariable Long id,
                             @RequestParam boolean enabled,
                             HttpSession session,
                             RedirectAttributes ra) {

        if (!requireSuperadmin(ra)) {
            return "redirect:/";
        }

        try {
            accessService.setEnabled(id, enabled);
            ra.addFlashAttribute("success", "User-Status gespeichert.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Status konnte nicht gespeichert werden: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/guilds")
    public String editGuildPermissions(@PathVariable Long id,
                                       Model model,
                                       RedirectAttributes ra) {
        if (!requireSuperadmin(ra)) {
            return "redirect:/";
        }

        var userOpt = accessService.findUser(id);
        if (userOpt.isEmpty()) {
            ra.addFlashAttribute("error", "User nicht gefunden.");
            return "redirect:/admin/users";
        }

        DashboardUserEntity user = userOpt.get();

        List<DashboardDiscordService.GuildInfo> guilds = discordService.listGuilds().stream()
                .sorted(Comparator.comparing(
                        guild -> guild.name() == null ? "" : guild.name(),
                        String.CASE_INSENSITIVE_ORDER
                ))
                .toList();

        Map<String, Long> currentMasks = new LinkedHashMap<>();
        for (DashboardDiscordService.GuildInfo guild : guilds) {
            currentMasks.put(guild.id(), accessService.getPermissionMask(user.getId(), guild.id()));
        }

        model.addAttribute("dashboardUser", user);
        model.addAttribute("guilds", guilds);
        model.addAttribute("permissions", Arrays.asList(DashboardPermission.values()));
        model.addAttribute("currentMasks", currentMasks);
        model.addAttribute("accessService", accessService);

        return "admin-user-guilds";
    }

    @PostMapping("/users/{id}/guilds")
    public String saveGuildPermissions(@PathVariable Long id,
                                       @RequestParam Map<String, String> params,
                                       RedirectAttributes ra) {
        if (!requireSuperadmin(ra)) {
            return "redirect:/";
        }

        var userOpt = accessService.findUser(id);
        if (userOpt.isEmpty()) {
            ra.addFlashAttribute("error", "User nicht gefunden.");
            return "redirect:/admin/users";
        }

        List<DashboardPermission> allPermissions = Arrays.asList(DashboardPermission.values());

        try {
            for (DashboardDiscordService.GuildInfo guild : discordService.listGuilds()) {
                long mask = 0L;

                for (DashboardPermission permission : allPermissions) {
                    String key = "perm_" + guild.id() + "_" + permission.name();
                    if (params.containsKey(key)) {
                        mask = PermissionBits.add(mask, permission);
                    }
                }

                accessService.savePermissionMask(id, guild.id(), mask);
            }

            ra.addFlashAttribute("success", "Guild-Rechte gespeichert.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Guild-Rechte konnten nicht gespeichert werden: " + e.getMessage());
        }

        return "redirect:/admin/users/" + id + "/guilds";
    }

    private boolean requireSuperadmin(RedirectAttributes ra) {
        if (accessService.isSuperadmin()) {
            return true;
        }

        ra.addFlashAttribute("error", "Kein Zugriff auf den Admin-Bereich.");
        return false;
    }
}