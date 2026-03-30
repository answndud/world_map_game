package com.worldmap.game.population.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import java.util.List;
import java.util.UUID;

public record PopulationGameStateView(
	UUID sessionId,
	Integer stageNumber,
	Long stageId,
	Integer expectedAttemptNumber,
	String difficultyLabel,
	Integer clearedStageCount,
	Integer totalScore,
	Integer livesRemaining,
	String targetCountryName,
	Integer populationYear,
	List<PopulationOptionView> options,
	GameSessionStatus gameStatus
) {
}
