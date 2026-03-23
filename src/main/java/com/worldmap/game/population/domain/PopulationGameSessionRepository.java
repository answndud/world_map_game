package com.worldmap.game.population.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopulationGameSessionRepository extends JpaRepository<PopulationGameSession, UUID> {
}
