package com.worldmap.game.population.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import java.util.UUID;

public record PopulationGameAnswerView(
	UUID sessionId,
	Integer roundNumber,
	String targetCountryName,
	Integer populationYear,
	Integer selectedOptionNumber,
	Long selectedPopulation,
	Integer correctOptionNumber,
	Long correctPopulation,
	Boolean correct,
	Integer awardedScore,
	Integer totalScore,
	Integer answeredRoundCount,
	Integer remainingRounds,
	Integer nextRoundNumber,
	GameSessionStatus gameStatus,
	String resultPageUrl
) {
}
