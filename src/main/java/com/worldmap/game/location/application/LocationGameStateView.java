package com.worldmap.game.location.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import com.worldmap.game.location.domain.LocationGameLevel;
import java.util.UUID;

public record LocationGameStateView(
	UUID sessionId,
	LocationGameLevel gameLevel,
	Integer stageNumber,
	String difficultyLabel,
	Integer clearedStageCount,
	Integer totalScore,
	Integer livesRemaining,
	String targetCountryName,
	GameSessionStatus gameStatus
) {
}
