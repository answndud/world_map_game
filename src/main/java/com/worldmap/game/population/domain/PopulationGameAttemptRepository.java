package com.worldmap.game.population.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopulationGameAttemptRepository extends JpaRepository<PopulationGameAttempt, Long> {

	List<PopulationGameAttempt> findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(UUID sessionId);

	long countByStageSessionId(UUID sessionId);

	void deleteAllByStageSessionId(UUID sessionId);
}
