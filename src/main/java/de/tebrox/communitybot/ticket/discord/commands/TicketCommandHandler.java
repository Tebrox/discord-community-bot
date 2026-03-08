package de.tebrox.communitybot.ticket.discord.commands;

import de.tebrox.communitybot.ticket.service.TicketGuildConfigService;
import de.tebrox.communitybot.ticket.service.TicketService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.stereotype.Component;

@Component
public class TicketCommandHandler {

    private final TicketService ticketService;
    private final TicketGuildConfigService ticketGuildConfigService;

    public TicketCommandHandler(
            TicketService ticketService,
            TicketGuildConfigService ticketGuildConfigService
    ) {
        this.ticketService = ticketService;
        this.ticketGuildConfigService = ticketGuildConfigService;
    }

    public void handle(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) {
            event.reply("Dieser Befehl kann nur auf einem Server verwendet werden.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String subcommand = event.getSubcommandName();
        if (subcommand == null || subcommand.isBlank()) {
            event.reply("Bitte gib einen Unterbefehl an.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (event.getMember() == null || !event.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            event.reply("Du hast keine Berechtigung für diesen Befehl.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        switch (subcommand) {
            case "post" -> handlePost(event, false);
            case "repost" -> handlePost(event, true);
            default -> event.reply("Unbekannter Unterbefehl.")
                    .setEphemeral(true)
                    .queue();
        }
    }

    private void handlePost(SlashCommandInteractionEvent event, boolean deleteOldMessage) {
        event.deferReply(true).queue();

        TextChannel targetChannel = resolveTargetChannel(event);
        if (targetChannel == null) {
            return;
        }

        ticketService.postTicketPanel(event.getGuild(), targetChannel, deleteOldMessage);

        String message = deleteOldMessage
                ? "Ticket-Panel wurde neu gepostet in " + targetChannel.getAsMention()
                : "Ticket-Panel wurde gepostet in " + targetChannel.getAsMention();

        event.getHook().sendMessage(message).queue();
    }

    private TextChannel resolveTargetChannel(SlashCommandInteractionEvent event) {
        if (event.getOption("channel") != null) {
            if (!event.getOption("channel").getChannelType().isMessage()) {
                event.getHook().sendMessage("Bitte wähle einen Text-Channel aus.").queue();
                return null;
            }
            return event.getOption("channel").getAsChannel().asTextChannel();
        }

        var configOpt = ticketGuildConfigService.findByGuildId(event.getGuild().getId());
        if (configOpt.isEmpty() || configOpt.get().getTicketChannelId() == null || configOpt.get().getTicketChannelId().isBlank()) {
            event.getHook().sendMessage("Kein Ticket-Channel konfiguriert.").queue();
            return null;
        }

        TextChannel configuredChannel = event.getGuild().getTextChannelById(configOpt.get().getTicketChannelId());
        if (configuredChannel == null) {
            event.getHook().sendMessage("Der konfigurierte Ticket-Channel existiert nicht mehr.").queue();
            return null;
        }

        return configuredChannel;
    }

    public static CommandData getCommandData() {
        return Commands.slash("ticket", "Verwalte den Ticket-Bot")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))
                .addSubcommands(
                        new SubcommandData("post", "Postet das Ticket-Panel")
                                .addOption(OptionType.CHANNEL, "channel", "Ziel-Textchannel (optional)", false),
                        new SubcommandData("repost", "Postet das Ticket-Panel neu und ersetzt die alte Nachricht")
                                .addOption(OptionType.CHANNEL, "channel", "Ziel-Textchannel (optional)", false)
                );
    }
}