package com.worldmap.game.location.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import com.worldmap.game.location.domain.LocationGameLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record LocationGameSessionResultView(
	UUID sessionId,
	String playerNickname,
	GameSessionStatus status,
	LocationGameLevel gameLevel,
	Integer totalStages,
	Integer clearedStageCount,
	Integer totalAttemptCount,
	Integer firstTryClearCount,
	Integer totalScore,
	Integer currentStageNumber,
	Integer livesRemaining,
	LocalDateTime startedAt,
	LocalDateTime finishedAt,
	List<LocationGameStageResultView> stages
) {
}
