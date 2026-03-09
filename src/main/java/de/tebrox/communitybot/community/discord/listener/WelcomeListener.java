package de.tebrox.communitybot.community.discord.listener;

import de.tebrox.communitybot.community.config.CommunityGuildConfig;
import de.tebrox.communitybot.community.discord.commands.PanelBuilder;
import de.tebrox.communitybot.community.service.CommunityGuildConfigService;
import de.tebrox.communitybot.community.service.WelcomeTrackingService;
import de.tebrox.communitybot.core.message.MessageKey;
import de.tebrox.communitybot.core.message.service.GuildMessageService;
import de.tebrox.communitybot.core.message.service.ResolvedMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class WelcomeListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(WelcomeListener.class);

    private final CommunityGuildConfigService configManager;
    private final WelcomeTrackingService welcomeTrackingService;
    private final GuildMessageService guildMessageService;

    public WelcomeListener(CommunityGuildConfigService configManager, WelcomeTrackingService welcomeTrackingService, GuildMessageService guildMessageService) {
        this.configManager = configManager;
        this.welcomeTrackingService = welcomeTrackingService;
        this.guildMessageService = guildMessageService;
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        String guildId = guild.getId();

        CommunityGuildConfig cfg = configManager.getConfig(guildId);
        if (cfg == null) return;

        CommunityGuildConfig.WelcomeConfig wc = cfg.getWelcome();
        if (!wc.isEnabled()) return;

        User user = event.getUser();
        String userId = user.getId();

        if (wc.isOnlyFirstJoin() && welcomeTrackingService.isWelcomed(guildId, userId)) {
            return;
        }
        if (wc.isOnlyFirstJoin()) {
            welcomeTrackingService.markWelcomed(guildId, userId, user.getName());
        }

        TextChannel channel = guild.getTextChannelById(wc.getChannelId());
        if (channel == null) {
            log.warn("[WelcomeListener] Welcome channel not found: {} (guild: {})", wc.getChannelId(), guildId);
            return;
        }

        TextChannel channel1 = guild.getTextChannelById(wc.getChannelId());
        if(channel == null) {
            log.warn("[WelcomeListener] Welcome channel not found: {} (guild: {})", wc.getChannelId(), guildId);
            return;
        }

        String mention     = user.getAsMention();
        String userName    = user.getName();
        String tag         = user.getAsTag();
        String memberId    = user.getId();
        String serverName  = guild.getName();
        String serverId    = guild.getId();
        String memberCount = String.valueOf(guild.getMemberCount());
        String avatarUrl   = user.getEffectiveAvatarUrl();

        Map<String, String> placeholders = Map.of(
          "mention", mention,
          "user", userName,
          "tag", tag,
          "id", memberId,
          "server", serverName,
          "serverId", serverId,
          "memberCount", memberCount,
          "avatarUrl", avatarUrl
        );

        ResolvedMessage publicMessage = guildMessageService.resolve(guildId, MessageKey.WELCOME_PUBLIC, placeholders);
        if(publicMessage.isEnabled()) {
            if(publicMessage.isEmbedEnabled()) {
                EmbedBuilder eb = PanelBuilder.buildEmbed(publicMessage);
                channel.sendMessageEmbeds(eb.build()).queue(
                        msg -> scheduleDelete(msg, wc.getDeleteAfterSeconds()),
                        err -> log.error("[WelcomeListener] Failed to send welcome embed: {}", err.getMessage())
                );
            } else if(publicMessage.getContent() != null && !publicMessage.getContent().isBlank()) {
                channel.sendMessage(publicMessage.getContent()).queue(
                        msg -> scheduleDelete(msg, wc.getDeleteAfterSeconds()),
                        err -> log.error("[WelcomeListener] Failed to send welcome text: {}", err.getMessage())
                );
            }
        }

        if(wc.isSendDm()) {
            ResolvedMessage dmMessage = guildMessageService.resolve(guildId, MessageKey.WELCOME_DM, placeholders);
            if (dmMessage.isEnabled() && dmMessage.getContent() != null && !dmMessage.getContent().isBlank()) {
                user.openPrivateChannel().queue(
                        pm -> pm.sendMessage(dmMessage.getContent()).queue(null, ignored -> {}),
                        ignored -> {}
                );
            }
        }
    }

    private void scheduleDelete(Message msg, int seconds) {
        if (seconds > 0) {
            msg.delete().queueAfter(seconds, TimeUnit.SECONDS, null,
                    err -> log.warn("[WelcomeListener] Delete failed: {}", err.getMessage()));
        }
    }
}
