package com.worldmap.mypage.application;

public record MyPageModePerformanceView(
	String gameModeLabel,
	long completedRunCount,
	long clearedStageCount,
	String firstTryClearRateLabel,
	String averageAttemptsPerClearLabel
) {
}
