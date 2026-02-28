package de.tebrox.rolesbot.util;

import de.tebrox.rolesbot.config.GuildConfig;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class PermissionGuard {

    private PermissionGuard() {}

    public static boolean check(SlashCommandInteractionEvent event, GuildConfig cfg) {
        Member member = event.getMember();
        if (member == null) {
            event.reply("❌ Dieser Befehl ist nur auf einem Server verfügbar.")
                 .setEphemeral(true).queue();
            return false;
        }

        String permName = cfg.getPanel().getRequirePermission();
        if (permName == null || permName.isBlank()) return true;

        Permission required;
        try {
            required = Permission.valueOf(permName.toUpperCase());
        } catch (IllegalArgumentException e) {
            event.reply("❌ Ungültige Permission konfiguriert: `" + permName + "`")
                 .setEphemeral(true).queue();
            return false;
        }

        if (!member.hasPermission(required)) {
            event.reply("❌ Du benötigst **" + required.getName() + "** um diesen Befehl auszuführen.")
                 .setEphemeral(true).queue();
            return false;
        }
        return true;
    }
}
