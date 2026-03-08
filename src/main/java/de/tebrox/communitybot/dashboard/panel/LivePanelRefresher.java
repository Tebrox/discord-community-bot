package de.tebrox.communitybot.dashboard.panel;

import de.tebrox.communitybot.community.config.CommunityGuildConfig;
import de.tebrox.communitybot.community.discord.listener.PanelAdminListener;
import net.dv8tion.jda.api.JDA;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name="discord.enabled", havingValue = "true", matchIfMissing = true)
public class LivePanelRefresher implements PanelRefresher {

    private final JDA jda;
    private final PanelAdminListener panelAdminListener;

    public LivePanelRefresher(JDA jda, PanelAdminListener panelAdminListener) {
        this.jda = jda;
        this.panelAdminListener = panelAdminListener;
    }

    @Override
    public void refresh(String guildId, CommunityGuildConfig cfg) {
        var guild = jda.getGuildById(guildId);
        if(guild != null && cfg != null) {
            panelAdminListener.refreshPanel(guild, cfg);
        }
    }
}
