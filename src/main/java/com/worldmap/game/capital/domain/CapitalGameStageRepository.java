package com.worldmap.game.capital.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CapitalGameStageRepository extends JpaRepository<CapitalGameStage, Long> {

	Optional<CapitalGameStage> findBySessionIdAndStageNumber(UUID sessionId, Integer stageNumber);

	List<CapitalGameStage> findAllBySessionIdOrderByStageNumber(UUID sessionId);

	List<CapitalGameStage> findAllBySessionMemberIdAndSessionFinishedAtIsNotNullAndStatus(
		Long memberId,
		CapitalGameStageStatus status
	);

	void deleteAllBySessionId(UUID sessionId);
}
