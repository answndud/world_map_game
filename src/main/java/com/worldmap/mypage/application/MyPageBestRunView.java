package com.worldmap.mypage.application;

public record MyPageBestRunView(
	String gameModeLabel,
	Integer totalScore,
	Integer bestRank,
	Integer clearedStageCount
) {
}
