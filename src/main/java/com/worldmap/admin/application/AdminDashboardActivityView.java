package com.worldmap.admin.application;

public record AdminDashboardActivityView(
	long totalMemberCount,
	long todayActiveMemberCount,
	long todayActiveGuestCount,
	long todayStartedSessionCount,
	long todayCompletedRunCount,
	long todayLocationCompletedRunCount,
	long todayPopulationCompletedRunCount
) {
}
