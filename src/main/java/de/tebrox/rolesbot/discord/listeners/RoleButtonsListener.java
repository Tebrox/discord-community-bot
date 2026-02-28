package de.tebrox.rolesbot.discord.listeners;

import de.tebrox.rolesbot.config.GuildConfig;
import de.tebrox.rolesbot.config.GuildConfigManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoleButtonsListener extends ListenerAdapter {

    private final GuildConfigManager configManager;

    public RoleButtonsListener(GuildConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("❌ Nicht auf einem Server.").setEphemeral(true).queue();
            return;
        }

        String guildId = guild.getId();
        GuildConfig cfg = configManager.getConfig(guildId);
        if (cfg == null) {
            event.reply("❌ Guild nicht initialisiert.").setEphemeral(true).queue();
            return;
        }

        String buttonId = event.getComponentId();

        if (buttonId.equals(cfg.getStatusButton().getId())) {
            handleStatusButton(event, cfg, guild);
            return;
        }

        Map<String, GuildConfig.ButtonConfig> byId;
        try {
            byId = cfg.buttonById();
        } catch (IllegalArgumentException e) {
            event.reply("❌ Konfigurationsfehler: " + e.getMessage()).setEphemeral(true).queue();
            return;
        }
        if (!byId.containsKey(buttonId)) return;

        handleRoleToggle(event, byId.get(buttonId), guild);
    }

    private void handleRoleToggle(ButtonInteractionEvent event, GuildConfig.ButtonConfig btnCfg, Guild guild) {
        Member selfMember = guild.getSelfMember();

        if (!selfMember.hasPermission(Permission.MANAGE_ROLES)) {
            event.reply("❌ Bot fehlt die Berechtigung `MANAGE_ROLES`.").setEphemeral(true).queue();
            return;
        }

        Role role = guild.getRoleById(btnCfg.getRoleId());
        if (role == null) {
            event.reply("❌ Rolle nicht gefunden (ID: `" + btnCfg.getRoleId() + "`). Bitte Admin kontaktieren.")
                 .setEphemeral(true).queue();
            return;
        }

        if (!selfMember.canInteract(role)) {
            event.reply("❌ Bot kann diese Rolle nicht verwalten (Hierarchie): `" + role.getName() + "`")
                 .setEphemeral(true).queue();
            return;
        }

        Member member = event.getMember();
        if (member == null) {
            event.reply("❌ Mitglied nicht gefunden.").setEphemeral(true).queue();
            return;
        }

        boolean hasRole = member.getRoles().contains(role);

        if (hasRole) {
            guild.removeRoleFromMember(member, role).queue(
                    v -> event.reply("❌ Rolle **" + role.getName() + "** entfernt.").setEphemeral(true).queue(),
                    err -> event.reply("❌ Fehler: " + err.getMessage()).setEphemeral(true).queue()
            );
        } else {
            guild.addRoleToMember(member, role).queue(
                    v -> event.reply("✅ Rolle **" + role.getName() + "** hinzugefügt.").setEphemeral(true).queue(),
                    err -> event.reply("❌ Fehler: " + err.getMessage()).setEphemeral(true).queue()
            );
        }
    }

    private void handleStatusButton(ButtonInteractionEvent event, GuildConfig cfg, Guild guild) {
        Member member = event.getMember();
        if (member == null) {
            event.reply("❌ Fehler beim Abrufen.").setEphemeral(true).queue();
            return;
        }

        Set<String> memberRoleIds = member.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        StringBuilder sb = new StringBuilder("**📋 Deine Rollen:**\n\n");
        for (GuildConfig.ButtonConfig btn : cfg.getButtons()) {
            boolean has = memberRoleIds.contains(btn.getRoleId());
            Role role = guild.getRoleById(btn.getRoleId());
            String roleName = role != null ? role.getName() : btn.getLabel();
            sb.append(has ? "✅" : "❌").append(" ").append(roleName).append("\n");
        }

        event.reply(sb.toString()).setEphemeral(true).queue();
    }
}
