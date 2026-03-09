package de.tebrox.communitybot.ticket.discord.buttons;

import de.tebrox.communitybot.community.discord.commands.PanelBuilder;
import de.tebrox.communitybot.core.message.MessageKey;
import de.tebrox.communitybot.core.message.service.GuildMessageService;
import de.tebrox.communitybot.core.message.service.ResolvedMessage;
import de.tebrox.communitybot.ticket.service.TicketService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "discord.ticket", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TicketButtonHandler {

    private static final Logger log = LoggerFactory.getLogger(TicketButtonHandler.class);

    private final TicketService ticketService;
    private final GuildMessageService guildMessageService;

    public TicketButtonHandler(TicketService ticketService, GuildMessageService guildMessageService) {
        this.ticketService = ticketService;
        this.guildMessageService = guildMessageService;
    }

    public void handle(ButtonInteractionEvent event) {
        if (event.getGuild() == null || event.getMember() == null) {
            ResolvedMessage msg = guildMessageService.resolveDefault(
                    MessageKey.TICKET_CREATE_ERROR,
                    Map.of("error", "Diese Aktion kann nur auf einem Server verwendet werden.")
            );
            event.reply(msg.getContent()).setEphemeral(true).queue();
        }

        String componentId = event.getComponentId();
        if (componentId == null || componentId.isBlank()) {
            return;
        }

        String[] parts = componentId.split(":", 3);
        if (parts.length < 2) {
            log.warn("Invalid ticket button component id: {}", componentId);
            reply(event, MessageKey.TICKET_CREATE_ERROR, Map.of("error", "Ungültige Ticket-Aktion."));
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
                reply(event, MessageKey.TICKET_CREATE_ERROR, Map.of("error", "Unbekannte Ticket-Aktion."));
            }
        }
    }

    private void handleCreate(ButtonInteractionEvent event, String category) {
        String validationError = ticketService.validateTicketCreationPossible(event.getGuild());
        if (validationError != null) {
            reply(event, MessageKey.TICKET_CREATE_ERROR, Map.of("error", validationError));
            return;
        }

        event.deferReply(true).queue();

        try {
            ticketService.createTicket(
                    event.getGuild(),
                    event.getMember(),
                    category,
                    url -> {
                        ResolvedMessage message = guildMessageService.resolve(
                                event.getGuild().getId(),
                                MessageKey.TICKET_CREATED_EPHEMERAL,
                                Map.of(
                                        "url", url,
                                        "user", event.getUser().getName(),
                                        "mention", event.getUser().getAsMention(),
                                        "category", category,
                                        "ticketId", ""
                                )
                        );

                        if (message.isEmbedEnabled()) {
                            event.getHook()
                                    .sendMessageEmbeds(PanelBuilder.buildEmbed(message).build())
                                    .setEphemeral(true)
                                    .queue();
                        } else {
                            event.getHook()
                                    .sendMessage(message.getContent() == null ? "" : message.getContent())
                                    .setEphemeral(true)
                                    .queue();
                        }
                    },
                    errorMessage -> {
                        ResolvedMessage message = guildMessageService.resolve(
                                event.getGuild().getId(),
                                MessageKey.TICKET_CREATE_ERROR,
                                Map.of("error", errorMessage)
                        );
                        event.getHook()
                                .sendMessage(message.getContent() == null ? "" : message.getContent())
                                .setEphemeral(true)
                                .queue();
                    }
            );
        } catch (Exception e) {
            log.error(
                    "Failed to create ticket for user {} in guild {}: {}",
                    event.getUser().getId(),
                    event.getGuild().getId(),
                    e.getMessage(),
                    e
            );

            ResolvedMessage message = guildMessageService.resolve(
                    event.getGuild().getId(),
                    MessageKey.TICKET_CREATE_ERROR,
                    Map.of("error", "Ticket konnte nicht erstellt werden.")
            );
            event.getHook()
                    .sendMessage(message.getContent() == null ? "" : message.getContent())
                    .setEphemeral(true)
                    .queue();
        }
    }

    private void handleClose(ButtonInteractionEvent event, String threadId) {
        event.deferReply(true).queue();

        try {
            ticketService.closeTicket(threadId, event.getUser().getId());

            ResolvedMessage message = guildMessageService.resolve(
                    event.getGuild().getId(),
                    MessageKey.TICKET_CLOSE_SUCCESS,
                    Map.of("ticketId", threadId)
            );
            event.getHook()
                    .sendMessage(message.getContent() == null ? "" : message.getContent())
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

            ResolvedMessage message = guildMessageService.resolve(
                    event.getGuild().getId(),
                    MessageKey.TICKET_CLOSE_ERROR,
                    Map.of("error", "Ticket konnte nicht geschlossen werden.")
            );
            event.getHook()
                    .sendMessage(message.getContent() == null ? "" : message.getContent())
                    .setEphemeral(true)
                    .queue();
        }
    }

    private void reply(ButtonInteractionEvent event, MessageKey key, Map<String, String> placeholders) {
        ResolvedMessage message = guildMessageService.resolve(event.getGuild().getId(), key, placeholders);
        event.reply(message.getContent() == null ? "" : message.getContent())
                .setEphemeral(true)
                .queue();
    }
}