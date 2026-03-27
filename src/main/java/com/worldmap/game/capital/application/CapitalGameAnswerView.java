package com.worldmap.game.capital.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import java.util.UUID;

public record CapitalGameAnswerView(
	UUID sessionId,
	Integer stageNumber,
	String targetCountryName,
	Integer selectedOptionNumber,
	String selectedCapitalCity,
	Integer correctOptionNumber,
	String correctCapitalCity,
	Boolean correct,
	Integer awardedScore,
	Integer totalScore,
	Integer clearedStageCount,
	Integer livesRemaining,
	Integer nextStageNumber,
	String nextDifficultyLabel,
	GameSessionStatus gameStatus,
	CapitalGameAnswerOutcome outcome,
	String resultPageUrl
) {
}
