package de.tebrox.communitybot.community.discord.listener;

import de.tebrox.communitybot.community.config.CommunityGuildConfig;
import de.tebrox.communitybot.community.service.CommunityGuildConfigService;
import de.tebrox.communitybot.community.panel.PanelBuilder;
import de.tebrox.communitybot.community.persistence.entity.PanelState;
import de.tebrox.communitybot.core.logging.LogBuffer;
import de.tebrox.communitybot.community.service.PanelService;
import de.tebrox.communitybot.core.util.ChannelGuard;
import de.tebrox.communitybot.core.access.PermissionGuard;
import de.tebrox.communitybot.core.util.RolesSafetyChecker;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PanelAdminListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(PanelAdminListener.class);

    private final CommunityGuildConfigService configManager;
    private final PanelService panelService;
    private final LogBuffer logBuffer;

    public PanelAdminListener(CommunityGuildConfigService configManager, PanelService panelService, LogBuffer logBuffer) {
        this.configManager = configManager;
        this.panelService = panelService;
        this.logBuffer = logBuffer;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("roles")) return;

        String sub = event.getSubcommandName();
        if (sub == null) return;
        if (!sub.equals("setup") && !sub.equals("repost")) return;

        boolean repost = sub.equals("repost");

        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("❌ Nur auf einem Server verfügbar.").setEphemeral(true).queue();
            return;
        }

        String guildId = guild.getId();
        CommunityGuildConfig cfg = configManager.getConfig(guildId);
        if (cfg == null) {
            event.reply("❌ Guild nicht initialisiert.").setEphemeral(true).queue();
            return;
        }

        if (!PermissionGuard.check(event, cfg)) return;
        if (!ChannelGuard.check(event, cfg)) return;

        try {
            cfg.buttonById();
        } catch (IllegalArgumentException e) {
            event.reply("❌ Konfigurationsfehler: " + e.getMessage()).setEphemeral(true).queue();
            return;
        }

        Member selfMember = guild.getSelfMember();
        String safetyReport = RolesSafetyChecker.buildReport(guild, selfMember, cfg);
        MessageEmbed embed = PanelBuilder.buildEmbed(cfg);
        List<ActionRow> rows = PanelBuilder.buildActionRows(cfg);

        Optional<PanelState> stateOpt = panelService.findState(guildId);

        if (repost) {
            // Delete old panel message if it exists, then post new one
            if (stateOpt.isPresent() && stateOpt.get().hasState()) {
                PanelState state = stateOpt.get();
                TextChannel oldCh = guild.getTextChannelById(state.getChannelId());
                if (oldCh != null) {
                    oldCh.retrieveMessageById(state.getMessageId()).queue(
                            oldMsg -> {
                                oldMsg.delete().queue(
                                        v -> postNewPanel(event, guild, cfg, embed, rows, safetyReport, guildId),
                                        err -> {
                                            log.warn("[PanelAdmin] Could not delete old panel message: {}", err.getMessage());
                                            postNewPanel(event, guild, cfg, embed, rows, safetyReport, guildId);
                                        }
                                );
                            },
                            err -> postNewPanel(event, guild, cfg, embed, rows, safetyReport, guildId)
                    );
                } else {
                    postNewPanel(event, guild, cfg, embed, rows, safetyReport, guildId);
                }
            } else {
                postNewPanel(event, guild, cfg, embed, rows, safetyReport, guildId);
            }
        } else if (stateOpt.isPresent() && stateOpt.get().hasState()) {
            // Try to edit existing panel
            PanelState state = stateOpt.get();
            TextChannel ch = guild.getTextChannelById(state.getChannelId());
            if (ch != null) {
                ch.retrieveMessageById(state.getMessageId()).queue(
                        msg -> msg.editMessageEmbeds(embed).setComponents(rows).queue(
                                edited -> {
                                    logBuffer.info("[PanelAdmin] Panel updated for guild: " + guildId);
                                    event.reply("✅ Panel aktualisiert.\n\n" + safetyReport).setEphemeral(true).queue();
                                },
                                err -> postNewPanel(event, guild, cfg, embed, rows, safetyReport, guildId)
                        ),
                        err -> postNewPanel(event, guild, cfg, embed, rows, safetyReport, guildId)
                );
            } else {
                postNewPanel(event, guild, cfg, embed, rows, safetyReport, guildId);
            }
        } else {
            postNewPanel(event, guild, cfg, embed, rows, safetyReport, guildId);
        }
    }

    private void postNewPanel(SlashCommandInteractionEvent event, Guild guild, CommunityGuildConfig cfg,
                               MessageEmbed embed, List<ActionRow> rows,
                               String safetyReport, String guildId) {
        TextChannel target = resolveTargetChannel(guild, cfg, event);
        if (target == null) {
            event.reply("❌ Kein gültiger Channel gefunden.").setEphemeral(true).queue();
            return;
        }

        target.sendMessageEmbeds(embed).setComponents(rows).queue(
                msg -> {
                    panelService.saveState(guildId, target.getId(), msg.getId());
                    logBuffer.info("[PanelAdmin] New panel posted for guild: " + guildId);
                    event.reply("✅ Neues Panel gepostet in <#" + target.getId() + ">.\n\n" + safetyReport)
                         .setEphemeral(true).queue();
                },
                err -> event.reply("❌ Fehler beim Senden: " + err.getMessage()).setEphemeral(true).queue()
        );
    }

    private TextChannel resolveTargetChannel(Guild guild, CommunityGuildConfig cfg, SlashCommandInteractionEvent event) {
        String allowedId = cfg.getPanel().getAllowedChannelId();
        if (allowedId != null && !allowedId.isBlank() && !allowedId.startsWith("CHANNEL_ID")) {
            TextChannel ch = guild.getTextChannelById(allowedId);
            if (ch != null) return ch;
        }
        if (event.getChannel() instanceof TextChannel tc) return tc;
        return null;
    }

    /** Called from web dashboard to refresh the panel after config save. */
    public void refreshPanel(Guild guild, CommunityGuildConfig cfg) {
        String guildId = guild.getId();
        Optional<PanelState> stateOpt = panelService.findState(guildId);
        if (stateOpt.isEmpty() || !stateOpt.get().hasState()) return;

        PanelState state = stateOpt.get();
        TextChannel ch = guild.getTextChannelById(state.getChannelId());
        if (ch == null) return;

        MessageEmbed embed = PanelBuilder.buildEmbed(cfg);
        List<ActionRow> rows = PanelBuilder.buildActionRows(cfg);

        ch.retrieveMessageById(state.getMessageId()).queue(
                msg -> msg.editMessageEmbeds(embed).setComponents(rows).queue(
                        ok -> logBuffer.info("[PanelAdmin] Panel refreshed for guild: " + guildId),
                        err -> logBuffer.warn("[PanelAdmin] Panel refresh failed: " + err.getMessage())
                ),
                err -> logBuffer.warn("[PanelAdmin] Panel message not found for refresh: " + err.getMessage())
        );
    }
}
