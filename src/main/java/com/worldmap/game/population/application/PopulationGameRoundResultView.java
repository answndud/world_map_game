package com.worldmap.game.population.application;

import java.time.LocalDateTime;

public record PopulationGameRoundResultView(
	Integer roundNumber,
	String targetCountryName,
	Integer populationYear,
	Long targetPopulation,
	Integer selectedOptionNumber,
	Long selectedPopulation,
	Integer correctOptionNumber,
	Long correctPopulation,
	Boolean correct,
	Integer awardedScore,
	LocalDateTime answeredAt
) {
}
