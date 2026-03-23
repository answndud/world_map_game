package com.worldmap.ranking.application;

import java.time.LocalDateTime;

public record LeaderboardEntryView(
	Integer rank,
	String playerNickname,
	Integer totalScore,
	Integer clearedStageCount,
	Integer totalAttemptCount,
	LocalDateTime finishedAt
) {
}
