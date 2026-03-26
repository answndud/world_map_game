package com.worldmap.mypage.application;

public record MyPageBestRunView(
	String gameModeLabel,
	Long completedRunCount,
	Integer totalScore,
	Integer bestRank,
	Integer clearedStageCount
) {
}
