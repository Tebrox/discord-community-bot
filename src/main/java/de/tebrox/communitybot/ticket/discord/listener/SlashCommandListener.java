package de.tebrox.communitybot.ticket.discord.listener;

import de.tebrox.communitybot.ticket.discord.commands.TicketCommandHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "discord.ticket", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SlashCommandListener extends ListenerAdapter {

    private final TicketCommandHandler ticketCommandHandler;

    public SlashCommandListener(TicketCommandHandler ticketCommandHandler) {
        this.ticketCommandHandler = ticketCommandHandler;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if ("ticket".equals(event.getName())) {
            ticketCommandHandler.handle(event);
        }
    }
}