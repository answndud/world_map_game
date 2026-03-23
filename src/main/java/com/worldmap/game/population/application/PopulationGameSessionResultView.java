package com.worldmap.game.population.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PopulationGameSessionResultView(
	UUID sessionId,
	String playerNickname,
	GameSessionStatus status,
	Integer totalStages,
	Integer clearedStageCount,
	Integer totalScore,
	Integer currentStageNumber,
	Integer livesRemaining,
	LocalDateTime startedAt,
	LocalDateTime finishedAt,
	List<PopulationGameStageResultView> stages
) {
}
