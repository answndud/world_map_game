package com.worldmap.game.capital.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import java.util.List;
import java.util.UUID;

public record CapitalGameStateView(
	UUID sessionId,
	Integer stageNumber,
	Long stageId,
	Integer expectedAttemptNumber,
	String difficultyLabel,
	Integer clearedStageCount,
	Integer totalScore,
	Integer livesRemaining,
	String targetCountryName,
	List<CapitalOptionView> options,
	GameSessionStatus gameStatus
) {
}
