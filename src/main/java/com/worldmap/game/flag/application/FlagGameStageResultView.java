package com.worldmap.game.flag.application;

import com.worldmap.game.flag.domain.FlagGameStageStatus;
import java.time.LocalDateTime;
import java.util.List;

public record FlagGameStageResultView(
	Integer stageNumber,
	String difficultyLabel,
	String targetFlagRelativePath,
	FlagGameStageStatus status,
	Integer attemptCount,
	Integer awardedScore,
	LocalDateTime clearedAt,
	List<FlagGameAttemptResultView> attempts
) {
}
