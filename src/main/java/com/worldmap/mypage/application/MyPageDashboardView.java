package com.worldmap.mypage.application;

import java.util.List;

public record MyPageDashboardView(
	String nickname,
	long totalCompletedRuns,
	List<MyPageBestRunView> bestRuns,
	List<MyPageModePerformanceView> modePerformances,
	List<MyPageRecentPlayView> recentPlays
) {
}
