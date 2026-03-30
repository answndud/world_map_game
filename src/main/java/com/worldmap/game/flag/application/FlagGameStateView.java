package com.worldmap.game.flag.application;

import com.worldmap.game.common.domain.GameSessionStatus;
import java.util.List;
import java.util.UUID;

public record FlagGameStateView(
	UUID sessionId,
	Integer stageNumber,
	Long stageId,
	Integer expectedAttemptNumber,
	String difficultyLabel,
	String difficultyGuide,
	Integer clearedStageCount,
	Integer totalScore,
	Integer livesRemaining,
	String targetFlagRelativePath,
	List<FlagOptionView> options,
	GameSessionStatus gameStatus
) {
}
