package de.tebrox.communitybot.repository;

import de.tebrox.communitybot.persistence.entity.PanelState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PanelStateRepository extends JpaRepository<PanelState, String> {
}
