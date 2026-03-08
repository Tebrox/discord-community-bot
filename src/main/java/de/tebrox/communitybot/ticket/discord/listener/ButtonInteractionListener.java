package de.tebrox.communitybot.ticket.discord.listener;

import de.tebrox.communitybot.ticket.discord.buttons.TicketButtonHandler;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class ButtonInteractionListener extends ListenerAdapter {

    private final TicketButtonHandler ticketButtonHandler;

    public ButtonInteractionListener(TicketButtonHandler ticketButtonHandler) {
        this.ticketButtonHandler = ticketButtonHandler;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        if (componentId != null && componentId.startsWith("ticket:")) {
            ticketButtonHandler.handle(event);
        }
    }
}