package de.tebrox.communitybot.ticket.service;

import de.tebrox.communitybot.ticket.persistence.entity.Ticket;
import de.tebrox.communitybot.ticket.persistence.entity.TicketGuildConfig;
import de.tebrox.communitybot.ticket.persistence.repository.TicketRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@ConditionalOnProperty(prefix="discord.ticket", name="enabled", havingValue = "true", matchIfMissing = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketGuildConfigService ticketGuildConfigService;
    private final JDA ticketJda;

    public TicketService(
            TicketRepository ticketRepository,
            TicketGuildConfigService ticketGuildConfigService,
            @Qualifier("ticketJda") JDA ticketJda
    ) {
        this.ticketRepository = ticketRepository;
        this.ticketGuildConfigService = ticketGuildConfigService;
        this.ticketJda = ticketJda;
    }

    @Transactional
    public void postTicketPanel(Guild guild, TextChannel channel, boolean deleteOldMessage) {
        TicketGuildConfig config = ticketGuildConfigService.getOrCreate(guild.getId());

        if (deleteOldMessage && config.getTicketMessageId() != null) {
            channel.deleteMessageById(config.getTicketMessageId()).queue(null, ignored -> {});
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(config.getEmbedTitle())
                .setDescription(config.getEmbedDescription())
                .setColor(Color.decode("#5865F2"));

        MessageCreateBuilder builder = new MessageCreateBuilder().setEmbeds(embed.build());

        List<TicketGuildConfig.TicketCategory> categories = config.getCategories();
        if (categories.isEmpty()) {
            builder.addActionRow(Button.primary("ticket:create:default", "🎫 Ticket öffnen"));
        } else {
            List<Button> buttons = categories.stream()
                    .limit(25)
                    .map(category -> {
                        String id = "ticket:create:" + sanitize(category.getLabel());
                        return Button.primary(id, category.getLabel());
                    })
                    .toList();

            for (int i = 0; i < buttons.size(); i += 5) {
                builder.addActionRow(buttons.subList(i, Math.min(i + 5, buttons.size())));
            }
        }

        channel.sendMessage(builder.build()).queue(message ->
                ticketGuildConfigService.saveTicketMessageId(guild.getId(), message.getId())
        );
    }

    @Transactional
    public void createTicket(
            Guild guild,
            Member member,
            String category,
            Consumer<String> onCreatedLink,
            Consumer<String> onError
    ) {
        TicketGuildConfig config = ticketGuildConfigService.getOrCreate(guild.getId());

        if (config.getTicketChannelId() == null) {
            onError.accept("Kein Ticket-Channel konfiguriert.");
            return;
        }

        TextChannel parentChannel = guild.getTextChannelById(config.getTicketChannelId());
        if (parentChannel == null) {
            onError.accept("Der konfigurierte Ticket-Channel existiert nicht mehr.");
            return;
        }

        long openTickets = ticketRepository.countByGuildIdAndCreatorIdAndClosedAtIsNull(
                guild.getId(),
                member.getId()
        );

        if (openTickets >= 3) {
            onError.accept("Du hast bereits 3 offene Tickets.");
            return;
        }

        String shortId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String safeCategory = sanitize(category == null ? "default" : category);
        String threadName = ("ticket-" + safeCategory + "-" + shortId);
        if (threadName.length() > 100) {
            threadName = threadName.substring(0, 100);
        }

        boolean privateThread = config.getThreadType() == TicketGuildConfig.ThreadType.PRIVATE_THREAD
                && parentChannel.getType() == ChannelType.TEXT;

        StringBuilder mentions = new StringBuilder(member.getAsMention());
        for (String roleId : config.getSupportRoleIds()) {
            Role role = guild.getRoleById(roleId);
            if (role != null && !role.isPublicRole()) {
                mentions.append(" ").append(role.getAsMention());
            }
        }

        parentChannel.createThreadChannel(threadName, privateThread).queue(thread -> {
            Ticket ticket = new Ticket();
            ticket.setShortId(shortId);
            ticket.setGuildId(guild.getId());
            ticket.setChannelId(parentChannel.getId());
            ticket.setThreadId(thread.getId());
            ticket.setCreatorId(member.getId());
            ticket.setCreatorName(member.getUser().getName());
            ticket.setCategory(category);
            ticketRepository.save(ticket);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Ticket #" + shortId)
                    .setDescription("Bitte beschreibe hier dein Anliegen.")
                    .setColor(Color.GREEN)
                    .setTimestamp(Instant.now());

            thread.sendMessage(mentions.toString())
                    .setEmbeds(embed.build())
                    .addActionRow(Button.danger("ticket:close:" + thread.getId(), "🔒 Ticket schließen"))
                    .queue();

            String url = "https://discord.com/channels/" + guild.getId() + "/" + thread.getId();
            onCreatedLink.accept(url);
        }, error -> onError.accept("Ticket konnte nicht erstellt werden."));
    }

    @Transactional
    public void closeTicket(String threadId, String closedByUserId) {
        Ticket ticket = ticketRepository.findByThreadIdAndClosedAtIsNull(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Offenes Ticket nicht gefunden."));

        ticket.setClosedByUserId(closedByUserId);
        ticket.setClosedAt(Instant.now());
        ticketRepository.save(ticket);

        var thread = ticketJda.getThreadChannelById(threadId);
        if (thread != null) {
            thread.getManager().setLocked(true).queue(
                    success -> thread.getManager().setArchived(true).queue(),
                    error -> thread.getManager().setArchived(true).queue()
            );
        }
    }

    public String validateTicketCreationPossible(Guild guild) {
        TicketGuildConfig config = ticketGuildConfigService.findByGuildId(guild.getId()).orElse(null);
        if (config == null || config.getTicketChannelId() == null) {
            return "Kein Ticket-Channel konfiguriert.";
        }

        if (guild.getTextChannelById(config.getTicketChannelId()) == null) {
            return "Der konfigurierte Ticket-Channel existiert nicht mehr.";
        }

        return null;
    }

    private String sanitize(String input) {
        return input == null
                ? "default"
                : input.toLowerCase()
                .replaceAll("[^a-z0-9-_]", "-")
                .replaceAll("-{2,}", "-");
    }
}