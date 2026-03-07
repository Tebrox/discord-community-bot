package de.tebrox.communitybot.discord.core;

import de.tebrox.communitybot.discord.listeners.*;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name="discord.enabled", havingValue = "true", matchIfMissing = true)
public class DiscordListenerRegistrar {

    private static final Logger log = LoggerFactory.getLogger(DiscordListenerRegistrar.class);

    private final JDA jda;
    private final GuildLifecycleListener guildLifecycleListener;
    private final RoleButtonsListener roleButtonsListener;
    private final PanelAdminListener panelAdminListener;
    private final ReloadListener reloadListener;
    private final WelcomeListener welcomeListener;

    public DiscordListenerRegistrar(JDA jda,
                                    GuildLifecycleListener guildLifecycleListener,
                                    RoleButtonsListener roleButtonsListener,
                                    PanelAdminListener panelAdminListener,
                                    ReloadListener reloadListener,
                                    WelcomeListener welcomeListener) {
        this.jda = jda;
        this.guildLifecycleListener = guildLifecycleListener;
        this.roleButtonsListener = roleButtonsListener;
        this.panelAdminListener = panelAdminListener;
        this.reloadListener = reloadListener;
        this.welcomeListener = welcomeListener;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerListeners() {
        log.info("Registering JDA listeners...");
        jda.addEventListener(guildLifecycleListener, roleButtonsListener, panelAdminListener, reloadListener, welcomeListener);
        log.info("JDA listeners registered.");
    }
}