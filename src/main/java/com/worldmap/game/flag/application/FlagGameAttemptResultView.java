package com.worldmap.game.flag.application;

import java.time.LocalDateTime;

public record FlagGameAttemptResultView(
	Integer attemptNumber,
	Integer selectedOptionNumber,
	String selectedCountryName,
	Boolean correct,
	Integer livesRemainingAfter,
	LocalDateTime attemptedAt
) {
}
