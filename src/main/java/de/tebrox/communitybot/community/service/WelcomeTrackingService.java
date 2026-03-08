package de.tebrox.communitybot.community.service;

import de.tebrox.communitybot.community.persistence.entity.WelcomedUser;
import de.tebrox.communitybot.community.persistence.repository.WelcomedUserRepository;
import de.tebrox.communitybot.core.util.SnowflakeValidator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WelcomeTrackingService {

    private static final Logger log = LoggerFactory.getLogger(WelcomeTrackingService.class);

    private final WelcomedUserRepository welcomedUserRepository;

    public boolean isWelcomed(String guildId, String userId) {
        return welcomedUserRepository.existsByGuildIdAndUserId(guildId, userId);
    }

    @Transactional
    public void markWelcomed(String guildId, String userId, String username) {
        if (!welcomedUserRepository.existsByGuildIdAndUserId(guildId, userId)) {
            WelcomedUser wu = new WelcomedUser(guildId, userId, username);
            welcomedUserRepository.save(wu);
            log.debug("[WelcomeTracking] Marked welcomed: {} in guild {}", userId, guildId);
        }
    }

    public List<WelcomedUser> getWelcomedUsers(String guildId) {
        SnowflakeValidator.validate(guildId, "guildId");
        return welcomedUserRepository.findByGuildIdOrderByWelcomedAtDesc(guildId);
    }

    @Transactional
    public void deleteWelcomedUser(String guildId, String userId) {
        SnowflakeValidator.validate(guildId, "guildId");
        SnowflakeValidator.validate(userId, "userId");
        welcomedUserRepository.deleteByGuildIdAndUserId(guildId, userId);
        log.info("[WelcomeTracking] Deleted welcomed record: user={} guild={}", userId, guildId);
    }
}
