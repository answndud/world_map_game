package com.worldmap.game.capital.application;

import java.time.LocalDateTime;

public record CapitalGameAttemptResultView(
	Integer attemptNumber,
	Integer selectedOptionNumber,
	String selectedCapitalCity,
	Boolean correct,
	Integer livesRemainingAfter,
	LocalDateTime attemptedAt
) {
}
