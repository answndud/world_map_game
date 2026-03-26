package com.worldmap.game.population.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import com.worldmap.game.population.domain.PopulationGameLevel;
import java.util.List;
import java.util.UUID;

public record PopulationGameStateView(
	UUID sessionId,
	PopulationGameLevel gameLevel,
	Integer stageNumber,
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
