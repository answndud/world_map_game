package com.worldmap.game.location.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import com.worldmap.game.location.domain.LocationGameLevel;
import java.util.UUID;

public record LocationGameAnswerView(
	UUID sessionId,
	LocationGameLevel gameLevel,
	Integer stageNumber,
	String targetCountryName,
	String selectedCountryName,
	String selectedCountryIso3Code,
	Integer distanceKm,
	String directionHint,
	Boolean correct,
	Integer awardedScore,
	Integer totalScore,
	Integer clearedStageCount,
	Integer livesRemaining,
	Integer nextStageNumber,
	String nextDifficultyLabel,
	GameSessionStatus gameStatus,
	LocationGameAnswerOutcome outcome,
	String resultPageUrl
) {
}
