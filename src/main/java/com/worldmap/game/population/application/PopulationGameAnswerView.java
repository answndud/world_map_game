package com.worldmap.game.population.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import com.worldmap.game.population.domain.PopulationGameLevel;
import java.util.UUID;

public record PopulationGameAnswerView(
	UUID sessionId,
	PopulationGameLevel gameLevel,
	Integer stageNumber,
	String targetCountryName,
	Integer populationYear,
	Integer selectedOptionNumber,
	Long selectedPopulation,
	String selectedOptionLabel,
	Integer correctOptionNumber,
	Long correctPopulation,
	String correctOptionLabel,
	Boolean correct,
	Integer awardedScore,
	Integer totalScore,
	Integer clearedStageCount,
	Integer livesRemaining,
	Integer nextStageNumber,
	String nextDifficultyLabel,
	Double errorRatePercent,
	PopulationGamePrecisionBand precisionBand,
	GameSessionStatus gameStatus,
	PopulationGameAnswerOutcome outcome,
	String resultPageUrl
) {
}
