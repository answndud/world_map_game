package com.worldmap.game.population.application;

import java.time.LocalDateTime;

public record PopulationGameAttemptResultView(
	Integer attemptNumber,
	Integer selectedOptionNumber,
	Long selectedPopulation,
	String selectedOptionLabel,
	Boolean correct,
	Integer livesRemainingAfter,
	LocalDateTime attemptedAt
) {
}
