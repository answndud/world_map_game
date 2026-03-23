package com.worldmap.game.location.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import java.util.UUID;

public record LocationGameAnswerView(
	UUID sessionId,
	Integer stageNumber,
	String targetCountryName,
	String selectedCountryName,
	String selectedCountryIso3Code,
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
