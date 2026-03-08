package de.tebrox.communitybot.community.discord.listener;

import de.tebrox.communitybot.community.config.CommunityGuildConfig;
import de.tebrox.communitybot.community.service.CommunityGuildConfigService;
import de.tebrox.communitybot.core.logging.LogBuffer;
import de.tebrox.communitybot.core.util.ChannelGuard;
import de.tebrox.communitybot.core.access.PermissionGuard;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name="discord.enabled", havingValue = "true", matchIfMissing = true)
public class ReloadListener extends ListenerAdapter {

    private final CommunityGuildConfigService configManager;
    private final JDA jda;
    private final LogBuffer logBuffer;

    public ReloadListener(CommunityGuildConfigService configManager, JDA jda, LogBuffer logBuffer) {
        this.configManager = configManager;
        this.jda = jda;
        this.logBuffer = logBuffer;
    }

    // JDA is injected lazily to avoid circular dependency – set after JDA is ready
    public void setJda(JDA jda) {
        // handled via constructor injection in JdaConfiguration
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("community")) return;

        String sub = event.getSubcommandName();
        if (sub == null || !sub.equals("reload")) return;

        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("❌ Nur auf einem Server verfügbar.").setEphemeral(true).queue();
            return;
        }

        String guildId = guild.getId();
        CommunityGuildConfig oldCfg = configManager.getConfig(guildId);
        if (oldCfg == null) {
            event.reply("❌ Guild nicht initialisiert.").setEphemeral(true).queue();
            return;
        }

        if (!PermissionGuard.check(event, oldCfg)) return;
        if (!ChannelGuard.check(event, oldCfg)) return;

        try {
            configManager.reload(jda);
            CommunityGuildConfig newCfg = configManager.getConfig(guildId);
            if (newCfg != null) {
                newCfg.buttonById();
            }
            logBuffer.info("[Reload] Cache cleared + reconciled from DB by " + event.getUser().getAsTag() + " in guild " + guildId);
            event.reply("✅ Cache geleert & DB synchronisiert.").setEphemeral(true).queue();
        } catch (IllegalArgumentException e) {
            event.reply("❌ Konfigurationsfehler: " + e.getMessage()).setEphemeral(true).queue();
        } catch (Exception e) {
            event.reply("❌ Fehler beim Laden: `" + e.getMessage() + "`").setEphemeral(true).queue();
        }
    }
}
