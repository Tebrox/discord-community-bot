package de.tebrox.communitybot.community.discord.listener;

import de.tebrox.communitybot.community.config.CommunityGuildConfig;
import de.tebrox.communitybot.community.service.CommunityGuildConfigService;
import de.tebrox.communitybot.core.message.MessageKey;
import de.tebrox.communitybot.core.message.service.GuildMessageService;
import de.tebrox.communitybot.core.message.service.ResolvedMessage;
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

    private final CommunityGuildConfigService configManager;
    private final GuildMessageService guildMessageService;

    public RoleButtonsListener(CommunityGuildConfigService configManager, GuildMessageService guildMessageService) {
        this.configManager = configManager;
        this.guildMessageService = guildMessageService;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            replyDefault(event, MessageKey.ROLE_ERROR, Map.of("error", "Nicht auf dem Server"));
            return;
        }

        String guildId = guild.getId();
        CommunityGuildConfig cfg = configManager.getConfig(guildId);
        if (cfg == null) {
            reply(event, guildId, MessageKey.ROLE_ERROR, Map.of("error", "Guild nicht initialisiert."));
            return;
        }

        String buttonId = event.getComponentId();

        if (buttonId.equals(cfg.getStatusButton().getId())) {
            handleStatusButton(event, cfg, guild);
            return;
        }

        Map<String, CommunityGuildConfig.ButtonConfig> byId;
        try {
            byId = cfg.buttonById();
        } catch (IllegalArgumentException e) {
            reply(event, guildId, MessageKey.ROLE_ERROR, Map.of("error", "Konfigurationsfehler: " + e.getMessage()));
            return;
        }
        if (!byId.containsKey(buttonId)) return;
        handleRoleToggle(event, byId.get(buttonId), guild);
    }

    private void handleRoleToggle(ButtonInteractionEvent event, CommunityGuildConfig.ButtonConfig btnCfg, Guild guild) {
        Member selfMember = guild.getSelfMember();
        String guildId = guild.getId();

        if (!selfMember.hasPermission(Permission.MANAGE_ROLES)) {
            reply(event, guildId, MessageKey.ROLE_ERROR, Map.of("error", "Bot fehlt die Berechtigung `MANAGE_ROLES`."));
            return;
        }

        Role role = guild.getRoleById(btnCfg.getRoleId());
        if (role == null) {
            reply(event, guildId, MessageKey.ROLE_ERROR, Map.of("error", "Rolle nicht gefunden (ID: `" + btnCfg.getRoleId() + "`). Bitte Admin kontaktieren.", "role", btnCfg.getLabel()));
            return;
        }

        if (!selfMember.canInteract(role)) {
            reply(event, guildId, MessageKey.ROLE_ERROR, Map.of("error", "Bot kann diese Rolle nicht verwalten (Hierarchie): `" + role.getName() + "`", "role", role.getName()));
            return;
        }

        Member member = event.getMember();
        if (member == null) {
            reply(event, guildId, MessageKey.ROLE_ERROR, Map.of("error", "Mitglied nicht gefunden."));
            return;
        }

        boolean hasRole = member.getRoles().contains(role);

        if (hasRole) {
            guild.removeRoleFromMember(member, role).queue(
                    v -> reply(event, guildId, MessageKey.ROLE_REMOVE_SUCCESS, Map.of(
                            "role", role.getName(),
                            "user", member.getUser().getName(),
                            "mention", member.getAsMention()
                    )),
                    err -> reply(event, guildId, MessageKey.ROLE_ERROR, Map.of("error", err.getMessage(), "role", role.getName()))
            );
        } else {
            guild.addRoleToMember(member, role).queue(
                    v -> reply(event, guildId, MessageKey.ROLE_ADD_SUCCESS, Map.of(
                            "role", role.getName(),
                            "user", member.getUser().getName(),
                            "mention", member.getAsMention()
                    )),
                    err -> reply(event, guildId, MessageKey.ROLE_ERROR, Map.of("error", err.getMessage(), "role", role.getName()))
            );
        }
    }

    private void handleStatusButton(ButtonInteractionEvent event, CommunityGuildConfig cfg, Guild guild) {
        Member member = event.getMember();
        if (member == null) {
            reply(event, guild.getId(), MessageKey.ROLE_ERROR, Map.of("error", "Fehler beim Abrufen."));
            return;
        }

        Set<String> memberRoleIds = member.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        StringBuilder roles = new StringBuilder();
        for (CommunityGuildConfig.ButtonConfig btn : cfg.getButtons()) {
            boolean has = memberRoleIds.contains(btn.getRoleId());
            Role role = guild.getRoleById(btn.getRoleId());
            String roleName = role != null ? role.getName() : btn.getLabel();
            roles.append(has ? "✅" : "❌").append(" ").append(roleName).append("\n");
        }

        reply(event, guild.getId(), MessageKey.ROLE_STATUS, Map.of(
                "roles", roles.toString().trim(),
                "user", member.getUser().getName(),
                "mention", member.getAsMention()
        ));
    }

    private void reply(ButtonInteractionEvent event, String guildId, MessageKey key, Map<String, String> placeholders) {
        ResolvedMessage message = guildMessageService.resolve(guildId, key, placeholders);
        String content = message.getContent() != null && !message.getContent().isBlank()
                ? message.getContent()
                : "Nachricht nicht konfiguriert.";
        event.reply(content).setEphemeral(true).queue();
    }

    private void replyDefault(ButtonInteractionEvent event, MessageKey key, Map<String, String> placeholders) {
        ResolvedMessage message = guildMessageService.resolveDefault(key, placeholders);
        String content = message.getContent() != null && !message.getContent().isBlank()
                ? message.getContent()
                : "Nachricht nicht konfiguriert.";
        event.reply(content).setEphemeral(true).queue();
    }
}
