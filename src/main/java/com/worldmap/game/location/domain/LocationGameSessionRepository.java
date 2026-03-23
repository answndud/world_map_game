package com.worldmap.game.location.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationGameSessionRepository extends JpaRepository<LocationGameSession, UUID> {
}
