package de.tebrox.rolesbot.discord.core;

import de.tebrox.rolesbot.discord.listeners.ReloadListener;
import de.tebrox.rolesbot.discord.listeners.RoleButtonsListener;
import de.tebrox.rolesbot.discord.listeners.PanelAdminListener;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DiscordListenerRegistrar {

    private static final Logger log = LoggerFactory.getLogger(DiscordListenerRegistrar.class);

    private final JDA jda;
    private final RoleButtonsListener roleButtonsListener;
    private final PanelAdminListener panelAdminListener;
    private final ReloadListener reloadListener;

    public DiscordListenerRegistrar(JDA jda,
                                    RoleButtonsListener roleButtonsListener,
                                    PanelAdminListener panelAdminListener,
                                    ReloadListener reloadListener) {
        this.jda = jda;
        this.roleButtonsListener = roleButtonsListener;
        this.panelAdminListener = panelAdminListener;
        this.reloadListener = reloadListener;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerListeners() {
        log.info("Registering JDA listeners...");
        jda.addEventListener(roleButtonsListener, panelAdminListener, reloadListener);
        log.info("JDA listeners registered.");
    }
}