package com.worldmap.mypage.application;

import java.time.LocalDateTime;

public record MyPageRecentPlayView(
	String gameModeLabel,
	Integer totalScore,
	Integer clearedStageCount,
	Integer totalAttemptCount,
	Integer rankAtRecordTime,
	LocalDateTime finishedAt,
	String nicknameSnapshot
) {
}
