package com.worldmap.game.location.application;

import java.time.LocalDateTime;

public record LocationGameAttemptResultView(
	Integer attemptNumber,
	String selectedCountryName,
	String selectedCountryIso3Code,
	Boolean correct,
	Integer livesRemainingAfter,
	LocalDateTime attemptedAt
) {
}
