package com.worldmap.game.populationbattle.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import java.util.UUID;

public record PopulationBattleGameAnswerView(
	UUID sessionId,
	Integer stageNumber,
	String questionPrompt,
	Integer selectedOptionNumber,
	String selectedCountryName,
	Long selectedCountryPopulation,
	Integer correctOptionNumber,
	String correctCountryName,
	Long correctCountryPopulation,
	Boolean correct,
	Integer awardedScore,
	Integer totalScore,
	Integer clearedStageCount,
	Integer livesRemaining,
	Integer nextStageNumber,
	String nextDifficultyLabel,
	GameSessionStatus gameStatus,
	PopulationBattleGameAnswerOutcome outcome,
	String resultPageUrl
) {
}
