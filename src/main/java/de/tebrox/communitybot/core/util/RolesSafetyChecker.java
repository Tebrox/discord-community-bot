package de.tebrox.communitybot.core.util;

import de.tebrox.communitybot.community.config.CommunityGuildConfig;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.*;

public final class RolesSafetyChecker {

    private RolesSafetyChecker() {}

    public static String buildReport(Guild guild, Member selfMember, CommunityGuildConfig cfg) {
        StringBuilder sb = new StringBuilder();
        sb.append("**🔍 Safety-Check Report** (`").append(guild.getName()).append("`)\n\n");

        boolean hasManageRoles = selfMember.hasPermission(Permission.MANAGE_ROLES);
        sb.append(hasManageRoles ? "✅" : "❌")
          .append(" Bot hat `MANAGE_ROLES`: **").append(hasManageRoles).append("**\n");

        sb.append("\n**Button-IDs:**\n");
        Set<String> seenIds = new HashSet<>();
        boolean hasDuplicates = false;
        for (CommunityGuildConfig.ButtonConfig btn : cfg.getButtons()) {
            if (!seenIds.add(btn.getId())) {
                sb.append("❌ Doppelte ID: `").append(btn.getId()).append("`\n");
                hasDuplicates = true;
            }
        }
        if (!hasDuplicates) sb.append("✅ Keine doppelten Button-IDs\n");

        sb.append("\n**Rollen-Hierarchie:**\n");
        for (CommunityGuildConfig.ButtonConfig btn : cfg.getButtons()) {
            String roleId = btn.getRoleId();
            if (roleId == null || roleId.isBlank() || roleId.startsWith("ROLE_ID")) {
                sb.append("⚠️ Button `").append(btn.getId()).append("` hat Platzhalter-RoleId\n");
                continue;
            }
            Role role = guild.getRoleById(roleId);
            if (role == null) {
                sb.append("❌ Rolle `").append(roleId).append("` (Button: `").append(btn.getId()).append("`) nicht gefunden\n");
            } else {
                boolean canInteract = selfMember.canInteract(role);
                sb.append(canInteract ? "✅" : "❌")
                  .append(" `").append(role.getName()).append("` (").append(roleId).append(")")
                  .append(" – canInteract: **").append(canInteract).append("**\n");
            }
        }

        sb.append("\n**Layout:**\n");
        List<String> layoutErrors = cfg.validateLayout();
        if (layoutErrors.isEmpty()) sb.append("✅ Layout valide\n");
        else for (String err : layoutErrors) sb.append("❌ ").append(err).append("\n");

        sb.append("\n**ID-Längen (max 100):**\n");
        boolean allLen = true;
        for (CommunityGuildConfig.ButtonConfig btn : cfg.getButtons()) {
            if (btn.getId().length() > 100) {
                sb.append("❌ ID zu lang: `").append(btn.getId()).append("`\n");
                allLen = false;
            }
        }
        if (cfg.getStatusButton() != null && cfg.getStatusButton().getId().length() > 100) {
            sb.append("❌ StatusButton-ID zu lang\n");
            allLen = false;
        }
        if (allLen) sb.append("✅ Alle IDs im Limit\n");

        return sb.toString();
    }
}
