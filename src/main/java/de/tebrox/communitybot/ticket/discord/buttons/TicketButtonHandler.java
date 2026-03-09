package de.tebrox.communitybot.ticket.discord.buttons;

import de.tebrox.communitybot.ticket.service.TicketService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.awt.Color;

@Component
@ConditionalOnProperty(prefix = "discord.ticket", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TicketButtonHandler {

    private static final Logger log = LoggerFactory.getLogger(TicketButtonHandler.class);

    private final TicketService ticketService;

    public TicketButtonHandler(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    public void handle(ButtonInteractionEvent event) {
        if (event.getGuild() == null || event.getMember() == null) {
            event.reply("Diese Aktion kann nur auf einem Server verwendet werden.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String componentId = event.getComponentId();
        if (componentId == null || componentId.isBlank()) {
            return;
        }

        String[] parts = componentId.split(":", 3);
        if (parts.length < 2) {
            log.warn("Invalid ticket button component id: {}", componentId);
            event.reply("Ungültige Ticket-Aktion.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String action = parts[1];

        switch (action) {
            case "create" -> {
                String category = parts.length >= 3 && parts[2] != null && !parts[2].isBlank()
                        ? parts[2]
                        : "default";
                handleCreate(event, category);
            }
            case "close" -> {
                String threadId = parts.length >= 3 && parts[2] != null && !parts[2].isBlank()
                        ? parts[2]
                        : event.getChannel().getId();
                handleClose(event, threadId);
            }
            default -> {
                log.warn("Unknown ticket button action: {}", action);
                event.reply("Unbekannte Ticket-Aktion.")
                        .setEphemeral(true)
                        .queue();
            }
        }
    }

    private void handleCreate(ButtonInteractionEvent event, String category) {
        String validationError = ticketService.validateTicketCreationPossible(event.getGuild());
        if (validationError != null) {
            event.reply("❌ " + validationError)
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.deferReply(true).queue();

        try {
            ticketService.createTicket(
                    event.getGuild(),
                    event.getMember(),
                    category,
                    url -> {
                        EmbedBuilder embed = new EmbedBuilder()
                                .setTitle("Ticket erstellt")
                                .setDescription("🔗 [Hier klicken, um dein Ticket zu öffnen](" + url + ")")
                                .setColor(Color.GREEN);

                        event.getHook()
                                .sendMessageEmbeds(embed.build())
                                .setEphemeral(true)
                                .queue();
                    },
                    errorMessage -> event.getHook()
                            .sendMessage("❌ " + errorMessage)
                            .setEphemeral(true)
                            .queue()
            );
        } catch (Exception e) {
            log.error(
                    "Failed to create ticket for user {} in guild {}: {}",
                    event.getUser().getId(),
                    event.getGuild().getId(),
                    e.getMessage(),
                    e
            );

            event.getHook()
                    .sendMessage("❌ Ticket konnte nicht erstellt werden.")
                    .setEphemeral(true)
                    .queue();
        }
    }

    private void handleClose(ButtonInteractionEvent event, String threadId) {
        event.deferReply(true).queue();

        try {
            ticketService.closeTicket(threadId, event.getUser().getId());

            event.getHook()
                    .sendMessage("✅ Ticket wird geschlossen.")
                    .setEphemeral(true)
                    .queue();
        } catch (Exception e) {
            log.error(
                    "Failed to close ticket {} by user {}: {}",
                    threadId,
                    event.getUser().getId(),
                    e.getMessage(),
                    e
            );

            event.getHook()
                    .sendMessage("❌ Ticket konnte nicht geschlossen werden.")
                    .setEphemeral(true)
                    .queue();
        }
    }
}