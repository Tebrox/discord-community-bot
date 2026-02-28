package de.tebrox.rolesbot.discord.listeners;

import de.tebrox.rolesbot.config.GuildConfig;
import de.tebrox.rolesbot.config.GuildConfigManager;
import de.tebrox.rolesbot.service.WelcomeTrackingService;
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
import java.util.concurrent.TimeUnit;

@Component
public class WelcomeListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(WelcomeListener.class);

    private final GuildConfigManager configManager;
    private final WelcomeTrackingService welcomeTrackingService;

    public WelcomeListener(GuildConfigManager configManager, WelcomeTrackingService welcomeTrackingService) {
        this.configManager = configManager;
        this.welcomeTrackingService = welcomeTrackingService;
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        String guildId = guild.getId();

        GuildConfig cfg = configManager.getConfig(guildId);
        if (cfg == null) return;

        GuildConfig.WelcomeConfig wc = cfg.getWelcome();
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

        String mention     = user.getAsMention();
        String userName    = user.getName();
        String tag         = user.getAsTag();
        String memberId    = user.getId();
        String serverName  = guild.getName();
        String memberCount = String.valueOf(guild.getMemberCount());
        String avatarUrl   = user.getEffectiveAvatarUrl();

        if (wc.getEmbed().isEnabled()) {
            GuildConfig.EmbedConfig ec = wc.getEmbed();
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(resolve(ec.getTitle(), mention, userName, tag, memberId, serverName, memberCount, avatarUrl));
            eb.setDescription(resolve(ec.getDescription(), mention, userName, tag, memberId, serverName, memberCount, avatarUrl));

            String footer = resolve(ec.getFooter(), mention, userName, tag, memberId, serverName, memberCount, avatarUrl);
            if (footer != null && !footer.isBlank()) eb.setFooter(footer);

            String thumbnail = resolve(ec.getThumbnail(), mention, userName, tag, memberId, serverName, memberCount, avatarUrl);
            if (thumbnail != null && !thumbnail.isBlank() && thumbnail.startsWith("http")) {
                eb.setThumbnail(thumbnail);
            }

            try {
                eb.setColor(new Color(Integer.parseInt(ec.getColor().replace("#", ""), 16)));
            } catch (Exception ignored) {
                eb.setColor(new Color(0x5865F2));
            }

            channel.sendMessageEmbeds(eb.build()).queue(
                    msg -> scheduleDelete(msg, wc.getDeleteAfterSeconds()),
                    err -> log.error("[WelcomeListener] Failed to send welcome: {}", err.getMessage())
            );
        } else if (wc.getMessage() != null && !wc.getMessage().isBlank()) {
            String text = resolve(wc.getMessage(), mention, userName, tag, memberId, serverName, memberCount, avatarUrl);
            channel.sendMessage(text).queue(
                    msg -> scheduleDelete(msg, wc.getDeleteAfterSeconds()),
                    err -> log.error("[WelcomeListener] Failed to send welcome: {}", err.getMessage())
            );
        }

        if (wc.isSendDm() && wc.getMessage() != null && !wc.getMessage().isBlank()) {
            String dmText = resolve(wc.getMessage(), mention, userName, tag, memberId, serverName, memberCount, avatarUrl);
            user.openPrivateChannel().queue(
                    pm -> pm.sendMessage(dmText).queue(null, ignored -> {}),
                    ignored -> {}
            );
        }
    }

    private void scheduleDelete(Message msg, int seconds) {
        if (seconds > 0) {
            msg.delete().queueAfter(seconds, TimeUnit.SECONDS, null,
                    err -> log.warn("[WelcomeListener] Delete failed: {}", err.getMessage()));
        }
    }

    private String resolve(String t, String mention, String user, String tag,
                           String id, String server, String memberCount, String avatarUrl) {
        if (t == null) return "";
        return t.replace("{mention}", mention)
                .replace("{user}", user)
                .replace("{tag}", tag)
                .replace("{id}", id)
                .replace("{server}", server)
                .replace("{memberCount}", memberCount)
                .replace("{avatarUrl}", avatarUrl);
    }
}
