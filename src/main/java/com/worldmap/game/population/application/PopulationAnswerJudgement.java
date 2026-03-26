package com.worldmap.game.population.application;

public record PopulationAnswerJudgement(
	boolean correct,
	int awardedScore,
	Double errorRatePercent,
	PopulationGamePrecisionBand precisionBand
) {
}
