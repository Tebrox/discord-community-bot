package de.tebrox.communitybot.web.panel;

import de.tebrox.communitybot.config.GuildConfig;
import de.tebrox.communitybot.discord.listeners.PanelAdminListener;
import net.dv8tion.jda.api.JDA;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
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
    public void refresh(String guildId, GuildConfig cfg) {
        var guild = jda.getGuildById(guildId);
        if(guild != null && cfg != null) {
            panelAdminListener.refreshPanel(guild, cfg);
        }
    }
}
