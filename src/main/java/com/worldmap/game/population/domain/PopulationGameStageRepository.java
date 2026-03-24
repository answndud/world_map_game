package com.worldmap.game.population.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopulationGameStageRepository extends JpaRepository<PopulationGameStage, Long> {

	Optional<PopulationGameStage> findBySessionIdAndStageNumber(UUID sessionId, Integer stageNumber);

	List<PopulationGameStage> findAllBySessionIdOrderByStageNumber(UUID sessionId);

	List<PopulationGameStage> findAllBySessionMemberIdAndSessionFinishedAtIsNotNullAndStatus(
		Long memberId,
		PopulationGameStageStatus status
	);

	void deleteAllBySessionId(UUID sessionId);
}
