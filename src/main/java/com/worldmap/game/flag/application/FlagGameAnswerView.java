package com.worldmap.game.flag.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import java.util.UUID;

public record FlagGameAnswerView(
	UUID sessionId,
	Integer stageNumber,
	String targetFlagRelativePath,
	Integer selectedOptionNumber,
	String selectedCountryName,
	Integer correctOptionNumber,
	String correctCountryName,
	Boolean correct,
	Integer awardedScore,
	Integer totalScore,
	Integer clearedStageCount,
	Integer livesRemaining,
	Integer nextStageNumber,
	String nextDifficultyLabel,
	String nextDifficultyGuide,
	GameSessionStatus gameStatus,
	FlagGameAnswerOutcome outcome,
	String resultPageUrl
) {
}
