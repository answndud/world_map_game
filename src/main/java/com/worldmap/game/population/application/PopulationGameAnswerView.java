package com.worldmap.game.population.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import java.util.UUID;

public record PopulationGameAnswerView(
	UUID sessionId,
	Integer stageNumber,
	String targetCountryName,
	Integer populationYear,
	Integer selectedOptionNumber,
	Long selectedPopulation,
	Integer correctOptionNumber,
	Long correctPopulation,
	Boolean correct,
	Integer awardedScore,
	Integer totalScore,
	Integer clearedStageCount,
	Integer livesRemaining,
	Integer nextStageNumber,
	String nextDifficultyLabel,
	GameSessionStatus gameStatus,
	PopulationGameAnswerOutcome outcome,
	String resultPageUrl
) {
}
