package com.worldmap.game.capital.application;

import com.worldmap.game.capital.domain.CapitalGameStageStatus;
import java.time.LocalDateTime;
import java.util.List;

public record CapitalGameStageResultView(
	Integer stageNumber,
	String targetCountryName,
	String correctCapitalCity,
	CapitalGameStageStatus status,
	Integer attemptCount,
	Integer awardedScore,
	LocalDateTime clearedAt,
	List<CapitalGameAttemptResultView> attempts
) {
}
