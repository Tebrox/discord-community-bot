package de.tebrox.communitybot.core.util;

import de.tebrox.communitybot.community.config.CommunityGuildConfig;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class ChannelGuard {

    private ChannelGuard() {}

    public static boolean check(SlashCommandInteractionEvent event, CommunityGuildConfig cfg) {
        CommunityGuildConfig.PanelConfig panel = cfg.getPanel();
        if (!panel.isEnforceChannelLock()) return true;

        String allowed = panel.getAllowedChannelId();
        if (allowed == null || allowed.isBlank() || allowed.startsWith("CHANNEL_ID")) return true;
        if (!SnowflakeValidator.isValid(allowed)) return true;

        if (event.getChannel().getId().equals(allowed)) return true;

        event.reply("❌ Dieser Befehl darf nur in <#" + allowed + "> ausgeführt werden.")
             .setEphemeral(true).queue();
        return false;
    }
}
