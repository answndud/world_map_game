package com.worldmap.game.populationbattle.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopulationBattleGameStageRepository extends JpaRepository<PopulationBattleGameStage, Long> {

	Optional<PopulationBattleGameStage> findBySessionIdAndStageNumber(UUID sessionId, Integer stageNumber);

	List<PopulationBattleGameStage> findAllBySessionIdOrderByStageNumber(UUID sessionId);

	List<PopulationBattleGameStage> findAllBySessionMemberIdAndSessionFinishedAtIsNotNullAndStatus(
		Long memberId,
		PopulationBattleGameStageStatus status
	);

	void deleteAllBySessionId(UUID sessionId);
}
