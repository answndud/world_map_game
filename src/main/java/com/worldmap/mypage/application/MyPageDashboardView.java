package com.worldmap.mypage.application;

import java.util.List;

public record MyPageDashboardView(
	String nickname,
	long totalCompletedRuns,
	MyPageBestRunView locationBest,
	MyPageBestRunView populationBest,
	MyPageBestRunView locationLevel2Best,
	MyPageBestRunView populationLevel2Best,
	MyPageModePerformanceView locationPerformance,
	MyPageModePerformanceView populationPerformance,
	List<MyPageRecentPlayView> recentPlays
) {
}
