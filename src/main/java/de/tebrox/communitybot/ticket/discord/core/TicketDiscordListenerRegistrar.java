package de.tebrox.communitybot.ticket.discord.core;

import de.tebrox.communitybot.ticket.discord.listener.ButtonInteractionListener;
import de.tebrox.communitybot.ticket.discord.listener.SlashCommandListener;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TicketDiscordListenerRegistrar {

    private static final Logger log = LoggerFactory.getLogger(TicketDiscordListenerRegistrar.class);

    private final JDA ticketJda;
    private final SlashCommandListener slashCommandListener;
    private final ButtonInteractionListener buttonInteractionListener;

    public TicketDiscordListenerRegistrar(
            @Qualifier("ticketJda") JDA ticketJda,
            SlashCommandListener slashCommandListener,
            ButtonInteractionListener buttonInteractionListener
    ) {
        this.ticketJda = ticketJda;
        this.slashCommandListener = slashCommandListener;
        this.buttonInteractionListener = buttonInteractionListener;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerListeners() {
        log.info("Registering ticket listeners...");
        ticketJda.addEventListener(slashCommandListener, buttonInteractionListener);
        log.info("Ticket listeners registered.");
    }
}