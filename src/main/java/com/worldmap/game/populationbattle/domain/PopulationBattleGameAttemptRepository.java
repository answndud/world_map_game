package com.worldmap.game.populationbattle.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopulationBattleGameAttemptRepository extends JpaRepository<PopulationBattleGameAttempt, Long> {

	List<PopulationBattleGameAttempt> findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(UUID sessionId);

	long countByStageSessionId(UUID sessionId);

	void deleteAllByStageSessionId(UUID sessionId);
}
