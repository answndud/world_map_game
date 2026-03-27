package com.worldmap.game.flag.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlagGameStageRepository extends JpaRepository<FlagGameStage, Long> {

	Optional<FlagGameStage> findBySessionIdAndStageNumber(UUID sessionId, Integer stageNumber);

	List<FlagGameStage> findAllBySessionIdOrderByStageNumber(UUID sessionId);

	List<FlagGameStage> findAllBySessionMemberIdAndSessionFinishedAtIsNotNullAndStatus(
		Long memberId,
		FlagGameStageStatus status
	);

	void deleteAllBySessionId(UUID sessionId);
}
