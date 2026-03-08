package de.tebrox.communitybot.ticket.discord.core;

import de.tebrox.communitybot.ticket.discord.commands.TicketCommandHandler;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class TicketCommandRegistrar implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TicketCommandRegistrar.class);

    private final JDA ticketJda;

    public TicketCommandRegistrar(@Qualifier("ticketJda") JDA ticketJda) {
        this.ticketJda = ticketJda;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Registering ticket slash commands...");

        ticketJda.updateCommands()
                .addCommands(TicketCommandHandler.getCommandData())
                .queue(
                        commands -> log.info("Registered {} ticket slash command(s).", commands.size()),
                        error -> log.error("Failed to register ticket slash commands: {}", error.getMessage(), error)
                );
    }
}