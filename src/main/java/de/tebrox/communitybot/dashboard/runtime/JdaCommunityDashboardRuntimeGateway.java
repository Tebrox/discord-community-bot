package de.tebrox.communitybot.dashboard.runtime;

import de.tebrox.communitybot.community.config.CommunityGuildConfig;
import de.tebrox.communitybot.community.discord.listener.PanelAdminListener;
import de.tebrox.communitybot.core.message.service.ResolvedMessage;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "discord.community", name = "enabled", havingValue = "true", matchIfMissing = true)
public class JdaCommunityDashboardRuntimeGateway implements CommunityDashboardRuntimeGateway {

    private final JDA communityJda;
    private final PanelAdminListener panelAdminListener;

    public JdaCommunityDashboardRuntimeGateway(
            @Qualifier("communityJda") JDA communityJda,
            PanelAdminListener panelAdminListener
    ) {
        this.communityJda = communityJda;
        this.panelAdminListener = panelAdminListener;
    }

    @Override
    public boolean isAvailableForGuild(String guildId) {
        return communityJda.getGuildById(guildId) != null;
    }

    @Override
    public void refreshRolePanel(String guildId, CommunityGuildConfig cfg) {
        var guild = communityJda.getGuildById(guildId);
        if (guild != null && cfg != null) {
            panelAdminListener.refreshPanel(guild, cfg);
        }
    }
}
