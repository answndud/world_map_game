package com.worldmap.game.location.application;

import com.worldmap.game.location.domain.LocationGameStageStatus;
import java.time.LocalDateTime;
import java.util.List;

public record LocationGameStageResultView(
	Integer stageNumber,
	String targetCountryName,
	String targetCountryIso3Code,
	LocationGameStageStatus status,
	Integer attemptCount,
	Integer hintPenalty,
	Integer awardedScore,
	LocalDateTime clearedAt,
	List<LocationGameAttemptResultView> attempts
) {
}
