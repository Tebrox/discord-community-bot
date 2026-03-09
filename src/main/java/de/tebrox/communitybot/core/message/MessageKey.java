package de.tebrox.communitybot.core.message;

public enum MessageKey {

    // Community / Welcome
    WELCOME_PUBLIC,
    WELCOME_DM,

    // Community / Rollen
    ROLE_PANEL,
    ROLE_ADD_SUCCESS,
    ROLE_REMOVE_SUCCESS,
    ROLE_STATUS,
    ROLE_ERROR,

    // Tickets
    TICKET_PANEL,
    TICKET_CREATED_EPHEMERAL,
    TICKET_THREAD_OPENED,
    TICKET_CLOSE_SUCCESS,
    TICKET_CREATE_ERROR,
    TICKET_CLOSE_ERROR;

    public String category() {
        return switch (this) {
            case WELCOME_PUBLIC, WELCOME_DM -> "Welcome";
            case ROLE_PANEL, ROLE_ADD_SUCCESS, ROLE_REMOVE_SUCCESS, ROLE_STATUS, ROLE_ERROR -> "Rollen";
            case TICKET_PANEL, TICKET_CREATED_EPHEMERAL, TICKET_THREAD_OPENED, TICKET_CLOSE_SUCCESS,
                 TICKET_CREATE_ERROR, TICKET_CLOSE_ERROR -> "Tickets";
        };
    }

    public String label() {
        return switch (this) {
            case WELCOME_PUBLIC -> "Welcome im öffentlichen Channel";
            case WELCOME_DM -> "Welcome per Direktnachricht";
            case ROLE_PANEL -> "Rollen-Panel";
            case ROLE_ADD_SUCCESS -> "Rolle hinzugefügt";
            case ROLE_REMOVE_SUCCESS -> "Rolle entfernt";
            case ROLE_STATUS -> "Status: eigene Rollen";
            case ROLE_ERROR -> "Fehler bei Rollenaktion";
            case TICKET_PANEL -> "Ticket-Panel";
            case TICKET_CREATED_EPHEMERAL -> "Ticket erstellt (ephemeral)";
            case TICKET_THREAD_OPENED -> "Ticket-Thread eröffnet";
            case TICKET_CLOSE_SUCCESS -> "Ticket geschlossen";
            case TICKET_CREATE_ERROR -> "Fehler beim Ticket-Erstellen";
            case TICKET_CLOSE_ERROR -> "Fehler beim Ticket-Schließen";
        };
    }
}
