package com.worldmap.game.location.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import java.util.UUID;

public record LocationGameStateView(
	UUID sessionId,
	Integer stageNumber,
	String difficultyLabel,
	Integer clearedStageCount,
	Integer totalScore,
	Integer livesRemaining,
	String targetCountryName,
	GameSessionStatus gameStatus
) {
}
