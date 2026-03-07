package de.tebrox.communitybot.config;

import de.tebrox.communitybot.discord.listeners.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

@ConditionalOnProperty(name="discord.enabled", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableConfigurationProperties({DiscordProperties.class, DashboardProperties.class})
public class JdaConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JdaConfiguration.class);

    private JDA jda;

    @Bean
    public JDA jda(
            DiscordProperties discordProperties,
            GuildConfigManager configManager
    ) throws InterruptedException {

        log.info("[CommunityBot] Initializing JDA (createLight + GUILD_MEMBERS + MemberCachePolicy.ALL)...");

        this.jda = JDABuilder.createLight(
                        discordProperties.token(),
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS
                )
                .disableCache(
                        CacheFlag.ACTIVITY,
                        CacheFlag.CLIENT_STATUS,
                        CacheFlag.EMOJI,
                        CacheFlag.STICKER,
                        CacheFlag.ONLINE_STATUS,
                        CacheFlag.SCHEDULED_EVENTS,
                        CacheFlag.VOICE_STATE
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setActivity(Activity.watching("Community"))
                .build()
                .awaitReady();

        log.info("[CommunityBot] JDA ready. Logged in as: {}", jda.getSelfUser().getAsTag());

        configManager.reconcile(jda);

        jda.updateCommands().addCommands(
                Commands.slash("roles", "Rollenpanel verwalten")
                        .addSubcommands(
                                new SubcommandData("setup", "Erstellt oder editiert das Rollenpanel"),
                                new SubcommandData("repost", "Postet das Rollenpanel neu (mit Löschung des alten)")
                        )
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED),

                Commands.slash("community", "CommunityBot verwalten")
                        .addSubcommands(
                                new SubcommandData("reload", "Leert den Cache und synchronisiert Guild-Configs aus der DB")
                        )
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
        ).queue();

        log.info("[CommunityBot] Slash commands registered. Active guilds: {}", jda.getGuilds().size());
        return jda;
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosed() {
        if (jda != null) {
            log.info("[CommunityBot] Shutting down JDA gracefully...");
            jda.shutdown();
        }
    }
}
