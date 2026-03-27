package com.worldmap.game.populationbattle.application;

import java.time.LocalDateTime;

public record PopulationBattleGameAttemptResultView(
	Integer attemptNumber,
	Integer selectedOptionNumber,
	String selectedCountryName,
	Boolean correct,
	Integer livesRemainingAfter,
	LocalDateTime attemptedAt
) {
}
