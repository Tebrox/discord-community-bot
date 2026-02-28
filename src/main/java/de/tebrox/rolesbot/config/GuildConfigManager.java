package de.tebrox.rolesbot.config;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central manager for multi-guild Discord bot configuration.
 * Thread-safe. Stores config in guilds.yml via GuildConfigLoader.
 */
@Service
public class GuildConfigManager {

    private static final Logger log = LoggerFactory.getLogger(GuildConfigManager.class);

    private final Object lock = new Object();
    private Map<String, GuildConfig> guilds;

    public GuildConfigManager() {
        guilds = GuildConfigLoader.loadOrCreate();
        log.info("[RolesBot] guilds.yml loaded. Registered guilds: {}", guilds.size());
    }

    public void reconcile(JDA jda) {
        for (Guild guild : jda.getGuilds()) {
            ensureRegistered(guild.getId(), guild.getName());
        }
        log.info("[RolesBot] Reconcile complete. Active guilds: {}", guilds.size());
    }

    public void onGuildJoin(String guildId, String guildName) {
        ensureRegistered(guildId, guildName);
    }

    public void onGuildLeave(String guildId, String guildName) {
        synchronized (lock) {
            guilds.remove(guildId);
            try { GuildConfigLoader.saveAtomic(guilds); }
            catch (IOException e) { log.error("Failed to save after guild leave: {}", e.getMessage()); }
        }
        log.info("[CLEANUP] Guild removed: {} ({})", guildName, guildId);
    }

    public GuildConfig getConfig(String guildId) {
        synchronized (lock) { return guilds.get(guildId); }
    }

    public Set<String> getRegisteredGuildIds() {
        synchronized (lock) { return new LinkedHashSet<>(guilds.keySet()); }
    }

    public void saveGuildConfig(String guildId, GuildConfig cfg) throws IOException {
        synchronized (lock) {
            guilds.put(guildId, cfg);
            GuildConfigLoader.saveAtomic(guilds);
        }
    }

    public void reload(JDA jda) {
        synchronized (lock) {
            guilds = GuildConfigLoader.reload();
        }
        reconcile(jda);
        log.info("[RolesBot] guilds.yml reloaded. Guilds: {}", guilds.size());
    }

    private void ensureRegistered(String guildId, String guildName) {
        synchronized (lock) {
            if (!guilds.containsKey(guildId)) {
                guilds.put(guildId, GuildConfigLoader.defaultGuildConfig());
                try {
                    GuildConfigLoader.saveAtomic(guilds);
                    log.info("[AUTO-REGISTER] New guild: {} ({}). Run /rolesetup.", guildName, guildId);
                } catch (IOException e) {
                    log.error("Failed to save after auto-register: {}", e.getMessage());
                }
            }
        }
    }
}
