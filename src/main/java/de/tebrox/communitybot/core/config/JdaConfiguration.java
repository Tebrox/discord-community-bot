package de.tebrox.communitybot.core.config;

import de.tebrox.communitybot.community.service.CommunityGuildConfigService;
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
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

import java.util.EnumSet;

@Configuration
@EnableConfigurationProperties({DiscordProperties.class, DashboardProperties.class})
public class JdaConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JdaConfiguration.class);

    @Bean("communityJda")
    @ConditionalOnProperty(prefix = "discord.community", name = "enabled", havingValue = "true", matchIfMissing = true)
    public JDA communityJda(DiscordProperties discordProperties) {
        DiscordProperties.BotProperties bot = discordProperties.getCommunity();
        if(!bot.isConfigured()) {
            throw new BeanCreationException("communityJda", "COMMUNITY_BOT_TOKEN fehlt oder ist leer.");
        }
        log.info("Creating community JDA...");
        return buildJda(bot.getToken(), "community");
    }

    @Bean("ticketJda")
    @ConditionalOnProperty(prefix = "discord.ticket", name="enabled", havingValue = "true", matchIfMissing = true)
    public JDA ticketJda(DiscordProperties discordProperties) {
        DiscordProperties.BotProperties bot = discordProperties.getTicket();
        if(!bot.isConfigured()) {
            throw new BeanCreationException("ticketJda", "TICKET_BOT_TOKEN fehlt oder ist leer.");
        }
        log.info("Creating ticket JDA...");
        return buildJda(bot.getToken(), "ticket");
    }

    private JDA buildJda(String token, String botKey) {
        try {
            JDA jda = JDABuilder.createDefault(token)
                    .enableIntents(
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_MESSAGE_REACTIONS,
                            GatewayIntent.MESSAGE_CONTENT
                    )
                    .enableCache(EnumSet.of(
                            CacheFlag.MEMBER_OVERRIDES,
                            CacheFlag.ROLE_TAGS
                    ))
                    .build()
                    .awaitReady();

            log.info("JDA for bot '{}' is ready as {}", botKey, jda.getSelfUser().getAsTag());
            return jda;
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BeanCreationException("Interrupted while creating JDA for bot '" + botKey + "'", e);
        } catch (Exception e) {
            throw new BeanCreationException("Failed to create JDA for bot '" + botKey + "'", e);
        }
    }
}
