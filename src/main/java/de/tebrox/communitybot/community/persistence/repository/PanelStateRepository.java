package de.tebrox.communitybot.community.persistence.repository;

import de.tebrox.communitybot.community.persistence.entity.PanelState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PanelStateRepository extends JpaRepository<PanelState, String> {
}
