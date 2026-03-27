package com.worldmap.game.populationbattle.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PopulationBattleGameSessionResultView(
	UUID sessionId,
	String playerNickname,
	GameSessionStatus status,
	Integer totalStages,
	Integer clearedStageCount,
	Integer totalAttemptCount,
	Integer firstTryClearCount,
	Integer totalScore,
	Integer currentStageNumber,
	Integer livesRemaining,
	LocalDateTime startedAt,
	LocalDateTime finishedAt,
	List<PopulationBattleGameStageResultView> stages
) {
}
