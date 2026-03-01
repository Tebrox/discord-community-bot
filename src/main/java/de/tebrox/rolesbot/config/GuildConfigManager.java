package de.tebrox.rolesbot.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tebrox.rolesbot.persistence.entity.GuildConfigEntity;
import de.tebrox.rolesbot.repository.GuildConfigRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central manager for multi-guild configuration.
 *
 * IMPORTANT: guilds.yml is intentionally NOT used.
 * All guild configs are persisted in the database (H2/MySQL via Spring Data JPA).
 */
@Service
public class GuildConfigManager {

    private static final Logger log = LoggerFactory.getLogger(GuildConfigManager.class);

    private final GuildConfigRepository repository;
    private final ObjectMapper objectMapper;

    /** Simple in-memory cache to avoid JSON parsing on every button click. */
    private final Map<String, GuildConfig> cache = new ConcurrentHashMap<>();

    public GuildConfigManager(GuildConfigRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        log.info("[RolesBot] GuildConfigManager initialized (DB-backed). Existing guilds in DB: {}", repository.count());
    }

    /** Ensures every guild the bot is currently in has a config row. */
    @Transactional
    public void reconcile(JDA jda) {
        for (Guild guild : jda.getGuilds()) {
            ensureRegistered(guild.getId(), guild.getName());
        }
        log.info("[RolesBot] Reconcile complete. Bot guilds: {}", jda.getGuilds().size());
    }

    @Transactional
    public void onGuildJoin(String guildId, String guildName) {
        ensureRegistered(guildId, guildName);
    }

    /**
     * When leaving a guild we delete its config.
     * If you prefer keeping historic config, change this to a soft-delete flag.
     */
    @Transactional
    public void onGuildLeave(String guildId, String guildName) {
        repository.deleteById(guildId);
        cache.remove(guildId);
        log.info("[CLEANUP] Guild removed from DB: {} ({})", guildName, guildId);
    }

    public GuildConfig getConfig(String guildId) {
        GuildConfig cached = cache.get(guildId);
        if (cached != null) return cached;

        return repository.findById(guildId)
                .map(this::deserialize)
                .map(cfg -> {
                    cache.put(guildId, cfg);
                    return cfg;
                })
                .orElse(null);
    }

    public Set<String> getRegisteredGuildIds() {
        // Note: This returns guild IDs registered in DB, not necessarily those the bot is in.
        Set<String> ids = new LinkedHashSet<>();
        repository.findAll().forEach(e -> ids.add(e.getGuildId()));
        return ids;
    }

    @Transactional
    public void saveGuildConfig(String guildId, GuildConfig cfg) throws IOException {
        GuildConfigEntity entity = repository.findById(guildId)
                .orElseGet(() -> GuildConfigEntity.builder()
                        .guildId(guildId)
                        .guildName("UNKNOWN")
                        .configJson("{}")
                        .updatedAt(Instant.now())
                        .build());

        entity.setConfigJson(serialize(cfg));
        entity.setUpdatedAt(Instant.now());
        repository.save(entity);

        cache.put(guildId, cfg);
    }

    /** "Reload" now just clears the cache and optionally re-creates missing DB rows. */
    public void reload(JDA jda) {
        cache.clear();
        reconcile(jda);
        log.info("[RolesBot] Cache cleared and reconciled (DB). Rows in DB: {}", repository.count());
    }

    @Transactional
    protected void ensureRegistered(String guildId, String guildName) {
        if (repository.existsById(guildId)) {
            // Keep name up-to-date (optional, but useful in dashboard)
            repository.findById(guildId).ifPresent(e -> {
                if (!Objects.equals(e.getGuildName(), guildName)) {
                    e.setGuildName(guildName);
                    e.setUpdatedAt(Instant.now());
                    repository.save(e);
                }
            });
            return;
        }

        GuildConfig defaults = defaultGuildConfig();
        GuildConfigEntity entity = GuildConfigEntity.builder()
                .guildId(guildId)
                .guildName(guildName)
                .configJson(serializeUnchecked(defaults))
                .updatedAt(Instant.now())
                .build();
        repository.save(entity);
        cache.put(guildId, defaults);

        log.info("[AUTO-REGISTER] New guild persisted: {} ({}).", guildName, guildId);
    }

    // ------------------------------------------------------------------ JSON helpers

    private GuildConfig deserialize(GuildConfigEntity entity) {
        try {
            GuildConfig cfg = objectMapper.readValue(entity.getConfigJson(), GuildConfig.class);
            // Defensive null-fixes for older/partial JSON
            if (cfg.getPanel() == null) cfg.setPanel(new GuildConfig.PanelConfig());
            if (cfg.getButtons() == null) cfg.setButtons(new ArrayList<>());
            if (cfg.getStatusButton() == null) cfg.setStatusButton(new GuildConfig.StatusButtonConfig());
            if (cfg.getLayout() == null) cfg.setLayout(new GuildConfig.LayoutConfig());
            if (cfg.getWelcome() == null) cfg.setWelcome(new GuildConfig.WelcomeConfig());
            if (cfg.getWelcome().getEmbed() == null) cfg.getWelcome().setEmbed(new GuildConfig.EmbedConfig());
            return cfg;
        } catch (Exception e) {
            log.error("Failed to deserialize guild config for {}: {}. Falling back to defaults.",
                    entity.getGuildId(), e.getMessage());
            return defaultGuildConfig();
        }
    }

    private String serialize(GuildConfig cfg) throws JsonProcessingException {
        return objectMapper.writeValueAsString(cfg);
    }

    private String serializeUnchecked(GuildConfig cfg) {
        try { return serialize(cfg); }
        catch (JsonProcessingException e) { return "{}"; }
    }

    private GuildConfig defaultGuildConfig() {
        // Defaults are already encoded in the POJOs.
        return new GuildConfig();
    }
}