package com.worldmap.game.population.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopulationGameRoundRepository extends JpaRepository<PopulationGameRound, Long> {

	Optional<PopulationGameRound> findBySessionIdAndRoundNumber(UUID sessionId, Integer roundNumber);

	List<PopulationGameRound> findAllBySessionIdOrderByRoundNumber(UUID sessionId);
}
