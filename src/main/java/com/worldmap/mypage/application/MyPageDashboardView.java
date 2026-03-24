package com.worldmap.mypage.application;

import java.util.List;

public record MyPageDashboardView(
	String nickname,
	long totalCompletedRuns,
	MyPageBestRunView locationBest,
	MyPageBestRunView populationBest,
	List<MyPageRecentPlayView> recentPlays
) {
}
