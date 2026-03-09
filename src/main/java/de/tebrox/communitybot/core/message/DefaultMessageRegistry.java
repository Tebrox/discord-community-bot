package de.tebrox.communitybot.core.message;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class DefaultMessageRegistry {

    private final Map<MessageKey, DefaultMessageDefinition> defaults = new EnumMap<>(MessageKey.class);

    public DefaultMessageRegistry() {
        defaults.put(MessageKey.WELCOME_PUBLIC, DefaultMessageDefinition.builder()
                .enabled(true)
                .content("Willkommen {mention} auf **{server}**!")
                .embedEnabled(true)
                .embedTitle("Willkommen, {mention}!")
                .embedDescription("**{user}** ist dem Server **{server}** beigetreten.\nMitglieder: **{memberCount}**")
                .embedFooter("User ID: {id}")
                .embedColor("#5865F2")
                .thumbnailUrl("{avatarUrl}")
                .allowContent(true)
                .allowEmbed(true)
                .placeholders(List.of("mention", "user", "tag", "id", "server", "memberCount", "avatarUrl"))
                .build());

        defaults.put(MessageKey.WELCOME_DM, DefaultMessageDefinition.builder()
                .enabled(false)
                .content("Willkommen auf **{server}**!")
                .embedEnabled(false)
                .allowContent(true)
                .allowEmbed(false)
                .placeholders(List.of("user", "server", "mention"))
                .build());

        defaults.put(MessageKey.ROLE_PANEL, DefaultMessageDefinition.builder()
                .enabled(true)
                .content(null)
                .embedEnabled(true)
                .embedTitle("Benachrichtigungs-Rollen")
                .embedDescription("Wähle unten die Rollen aus, die du haben möchtest.")
                .embedColor("#5865F2")
                .allowContent(false)
                .allowEmbed(true)
                .placeholders(List.of("server"))
                .build());

        defaults.put(MessageKey.ROLE_ADD_SUCCESS, DefaultMessageDefinition.builder()
                .enabled(true)
                .content("Die Rolle **{role}** wurde dir hinzugefügt.")
                .embedEnabled(false)
                .allowContent(true)
                .allowEmbed(false)
                .placeholders(List.of("role", "mention", "user"))
                .build());

        defaults.put(MessageKey.ROLE_REMOVE_SUCCESS, DefaultMessageDefinition.builder()
                .enabled(true)
                .content("Die Rolle **{role}** wurde dir entfernt.")
                .embedEnabled(false)
                .allowContent(true)
                .allowEmbed(false)
                .placeholders(List.of("role", "mention", "user"))
                .build());

        defaults.put(MessageKey.ROLE_STATUS, DefaultMessageDefinition.builder()
                .enabled(true)
                .content("Deine aktuellen Rollen: {roles}")
                .embedEnabled(false)
                .allowContent(true)
                .allowEmbed(false)
                .placeholders(List.of("roles", "mention", "user"))
                .build());

        defaults.put(MessageKey.ROLE_ERROR, DefaultMessageDefinition.builder()
                .enabled(true)
                .content("Die Rollen-Aktion konnte nicht verarbeitet werden.")
                .embedEnabled(false)
                .allowContent(true)
                .allowEmbed(false)
                .placeholders(List.of("role", "reason"))
                .build());

        defaults.put(MessageKey.TICKET_PANEL, DefaultMessageDefinition.builder()
                .enabled(true)
                .content(null)
                .embedEnabled(true)
                .embedTitle("Support-Tickets")
                .embedDescription("Klicke auf den Button unten, um ein Ticket zu erstellen.")
                .embedColor("#5865F2")
                .allowContent(false)
                .allowEmbed(true)
                .placeholders(List.of("server"))
                .build());

        defaults.put(MessageKey.TICKET_CREATED_EPHEMERAL, DefaultMessageDefinition.builder()
                .enabled(true)
                .content("Dein Ticket wurde erstellt: {url}")
                .embedEnabled(false)
                .allowContent(true)
                .allowEmbed(false)
                .placeholders(List.of("url", "ticketId", "mention"))
                .build());

        defaults.put(MessageKey.TICKET_THREAD_OPENED, DefaultMessageDefinition.builder()
                .enabled(true)
                .content("{mention}")
                .embedEnabled(true)
                .embedTitle("Ticket #{ticketId}")
                .embedDescription("Bitte beschreibe hier dein Anliegen.")
                .embedColor("#5865F2")
                .allowContent(true)
                .allowEmbed(true)
                .placeholders(List.of("mention", "ticketId", "user", "server"))
                .build());

        defaults.put(MessageKey.TICKET_CLOSE_SUCCESS, DefaultMessageDefinition.builder()
                .enabled(true)
                .content("Das Ticket wurde geschlossen.")
                .embedEnabled(false)
                .allowContent(true)
                .allowEmbed(false)
                .placeholders(List.of("ticketId"))
                .build());

        defaults.put(MessageKey.TICKET_CREATE_ERROR, DefaultMessageDefinition.builder()
                .enabled(true)
                .content("Beim Erstellen des Tickets ist ein Fehler aufgetreten.")
                .embedEnabled(false)
                .allowContent(true)
                .allowEmbed(false)
                .placeholders(List.of("reason"))
                .build());

        defaults.put(MessageKey.TICKET_CLOSE_ERROR, DefaultMessageDefinition.builder()
                .enabled(true)
                .content("Beim Schließen des Tickets ist ein Fehler aufgetreten.")
                .embedEnabled(false)
                .allowContent(true)
                .allowEmbed(false)
                .placeholders(List.of("reason"))
                .build());
    }

    public DefaultMessageDefinition get(MessageKey key) {
        return defaults.get(key);
    }

    public Map<MessageKey, DefaultMessageDefinition> getAll() {
        return defaults;
    }
}