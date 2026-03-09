package de.tebrox.communitybot.community.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tebrox.communitybot.community.config.CommunityGuildConfig;
import de.tebrox.communitybot.community.persistence.entity.CommunityGuildConfigEntity;
import de.tebrox.communitybot.community.persistence.repository.GuildConfigRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class CommunityGuildConfigService {

    private static final Logger log = LoggerFactory.getLogger(CommunityGuildConfigService.class);

    private final GuildConfigRepository repository;
    private final ObjectMapper objectMapper;

    /** Simple in-memory cache to avoid JSON parsing on every button click. */
    private final Map<String, CommunityGuildConfig> cache = new ConcurrentHashMap<>();

    public CommunityGuildConfigService(GuildConfigRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        log.info("[CommunityBot] GuildConfigManager initialized (DB-backed). Existing guilds in DB: {}", repository.count());
    }

    /** Ensures every guild the bot is currently in has a config row. */
    @Transactional
    public void reconcile(@Qualifier("communityJda")JDA jda) {
        for (Guild guild : jda.getGuilds()) {
            ensureRegistered(guild.getId(), guild.getName());
        }
        log.info("[CommunityBot] Reconcile complete. Bot guilds: {}", jda.getGuilds().size());
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

    public CommunityGuildConfig getConfig(String guildId) {
        CommunityGuildConfig cached = cache.get(guildId);
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
    public void saveGuildConfig(String guildId, CommunityGuildConfig cfg) throws IOException {
        CommunityGuildConfigEntity entity = repository.findById(guildId)
                .orElseGet(() -> CommunityGuildConfigEntity.builder()
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
    public void reload(@Qualifier("communityJda") JDA jda) {
        cache.clear();
        reconcile(jda);
        log.info("[CommunityBot] Cache cleared and reconciled (DB). Rows in DB: {}", repository.count());
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

        CommunityGuildConfig defaults = defaultGuildConfig();
        CommunityGuildConfigEntity entity = CommunityGuildConfigEntity.builder()
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

    private CommunityGuildConfig deserialize(CommunityGuildConfigEntity entity) {
        try {
            CommunityGuildConfig cfg = objectMapper.readValue(entity.getConfigJson(), CommunityGuildConfig.class);
            // Defensive null-fixes for older/partial JSON
            if (cfg.getPanel() == null) cfg.setPanel(new CommunityGuildConfig.PanelConfig());
            if (cfg.getButtons() == null) cfg.setButtons(new ArrayList<>());
            if (cfg.getStatusButton() == null) cfg.setStatusButton(new CommunityGuildConfig.StatusButtonConfig());
            if (cfg.getLayout() == null) cfg.setLayout(new CommunityGuildConfig.LayoutConfig());
            if (cfg.getWelcome() == null) cfg.setWelcome(new CommunityGuildConfig.WelcomeConfig());
            return cfg;
        } catch (Exception e) {
            log.error("Failed to deserialize guild config for {}: {}. Falling back to defaults.",
                    entity.getGuildId(), e.getMessage());
            return defaultGuildConfig();
        }
    }

    private String serialize(CommunityGuildConfig cfg) throws JsonProcessingException {
        return objectMapper.writeValueAsString(cfg);
    }

    private String serializeUnchecked(CommunityGuildConfig cfg) {
        try { return serialize(cfg); }
        catch (JsonProcessingException e) { return "{}"; }
    }

    private CommunityGuildConfig defaultGuildConfig() {
        // Defaults are already encoded in the POJOs.
        return new CommunityGuildConfig();
    }
}