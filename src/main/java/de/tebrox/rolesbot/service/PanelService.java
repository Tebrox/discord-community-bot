package de.tebrox.rolesbot.service;

import de.tebrox.rolesbot.persistence.entity.PanelState;
import de.tebrox.rolesbot.repository.PanelStateRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PanelService {

    private static final Logger log = LoggerFactory.getLogger(PanelService.class);

    private final PanelStateRepository panelStateRepository;

    public Optional<PanelState> findState(String guildId) {
        return panelStateRepository.findById(guildId);
    }

    @Transactional
    public PanelState saveState(String guildId, String channelId, String messageId) {
        PanelState state = panelStateRepository.findById(guildId)
                .orElse(new PanelState());
        state.setGuildId(guildId);
        state.setChannelId(channelId);
        state.setMessageId(messageId);
        return panelStateRepository.save(state);
    }

    @Transactional
    public void deleteState(String guildId) {
        panelStateRepository.deleteById(guildId);
        log.debug("[PanelService] Deleted panel state for guild: {}", guildId);
    }
}
